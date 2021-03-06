package agenor.org.agenorwallet;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.acra.ACRA;
//import org.acra.ReportingInteractionMode;
//import org.acra.annotation.ReportsCrashes;

import org.acra.data.CrashReportData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import global.ContextWrapper;
import global.WalletConfiguration;
import global.utils.Io;
import pivtrum.NetworkConf;
import pivtrum.PivtrumPeerData;
import agenor.org.agenorwallet.contacts.ContactsStore;
import agenor.org.agenorwallet.module.AgenorContext;
import agenor.org.agenorwallet.module.store.AccStoreDb;
import agenor.org.agenorwallet.module.store.MintsStoreDb;
import agenor.org.agenorwallet.module.wallet.WalletBackupHelper;
import global.AgenorModule;
import global.AgenorModuleImp;
import agenor.org.agenorwallet.module.WalletConfImp;
import agenor.org.agenorwallet.rate.db.RateDb;
import agenor.org.agenorwallet.service.IntentsConstants;
import agenor.org.agenorwallet.service.AgenorWalletService;
import agenor.org.agenorwallet.utils.AppConf;
import agenor.org.agenorwallet.utils.CentralFormats;
import agenor.org.agenorwallet.utils.CrashReporter;

import static agenor.org.agenorwallet.service.IntentsConstants.ACTION_RESET_BLOCKCHAIN;
import static agenor.org.agenorwallet.service.IntentsConstants.ACTION_RESET_BLOCKCHAIN_ROLLBACK_TO;
import static agenor.org.agenorwallet.utils.AndroidUtils.shareText;

/**
 * Created by mati on 18/04/17.
 */
//@ReportsCrashes(
//        mailTo = AgenorContext.REPORT_EMAIL, // my email here
//        mode = ReportingInteractionMode.TOAST,
//        resToastText = R.string.crash_toast_text)
public class AgenorApplication extends Application implements ContextWrapper {

    private static Logger log;

    /** Singleton */
    private static AgenorApplication instance;
    public static final long TIME_CREATE_APPLICATION = System.currentTimeMillis();
    private long lastTimeRequestBackup;

    private AgenorModule agenorModule;
    private AppConf appConf;
    private WalletConfiguration walletConfiguration;
    private NetworkConf networkConf;

    private CentralFormats centralFormats;

    private ActivityManager activityManager;
    private PackageInfo info;
    // flag that tells us if the core crashed for some unknown reason
    private AtomicBoolean hasCoreCrashed = new AtomicBoolean(false);

    public static AgenorApplication getInstance() {
        return instance;
    }

    private CrashReporter.CrashListener crashListener = new CrashReporter.CrashListener() {
        @Override
        public void onCrashOcurred(Thread thread, Throwable throwable) {
            log.error("crash occured..");

            try {
                // First remove every notification
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancelAll();
            }catch (Exception e){
                log.error("Exception cancelling notifications", e);
            }

            throwable.printStackTrace();
            String authorities = "agenor.org.agenorwallet.agenorwallet.myfileprovider";
            final File cacheDir = getCacheDir();
            // show error report dialog to send the crash
            final ArrayList<Uri> attachments = new ArrayList<>();
            try {
                final File logDir = getDir("log", Context.MODE_PRIVATE);

                for (final File logFile : logDir.listFiles()) {
                    final String logFileName = logFile.getName();
                    final File file;
                    if (logFileName.endsWith(".log.gz"))
                        file = File.createTempFile(logFileName.substring(0, logFileName.length() - 6), ".log.gz", cacheDir);
                    else if (logFileName.endsWith(".log"))
                        file = File.createTempFile(logFileName.substring(0, logFileName.length() - 3), ".log", cacheDir);
                    else
                        continue;

                    final InputStream is = new FileInputStream(logFile);
                    final OutputStream os = new FileOutputStream(file);

                    Io.copy(is, os);

                    os.close();
                    is.close();

                    attachments.add(FileProvider.getUriForFile(getApplicationContext(), authorities, file));
                }

                shareText(AgenorApplication.this,"Agenor wallet crash", "Unexpected crash", attachments);
            } catch (final IOException x) {
                log.info("problem writing attachment", x);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            initLogging();
            log = LoggerFactory.getLogger(AgenorApplication.class);
            PackageManager manager = getPackageManager();
            info = manager.getPackageInfo(this.getPackageName(), 0);
            activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            // The following line triggers the initialization of ACRA
            ACRA.init(this);
            //if (BuildConfig.DEBUG)
            //    new ANRWatchDog().start();
            CrashReporter.init(getCacheDir());
            CrashReporter.setCrashListener(crashListener);

            AgenorContext.CONTEXT.zerocoinContext.jniBridge = new AndroidJniBridge();
            AgenorContext.CONTEXT.accStore = new AccStoreDb(this);
            AgenorContext.CONTEXT.mintsStore = new MintsStoreDb(this);


            // Default network conf for localhost test
            networkConf = new NetworkConf();
            appConf = new AppConf(getSharedPreferences(AppConf.PREFERENCE_NAME, MODE_PRIVATE));
            centralFormats = new CentralFormats(appConf);
            walletConfiguration = new WalletConfImp(getSharedPreferences("agenor_wallet",MODE_PRIVATE));
            //todo: add this on the initial wizard..
            ContactsStore contactsStore = new ContactsStore(this);
            agenorModule = new AgenorModuleImp(this, walletConfiguration,contactsStore,new RateDb(this),new WalletBackupHelper());


            if (appConf.isAppInit()) {
                startCoreBackground();
            }
        } catch (Exception e){
            log.error("Exception on start",e);
            System.exit(1);
        }
    }

    public synchronized void startCoreBackground() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                startCore();
            } catch (Exception e) {
                log.error("Exception on core start shutting down app", e);

                hasCoreCrashed.set(true);
                // Notify every activity that the core crashed..
                Intent intent = new Intent(IntentsConstants.ACTION_APP_CORE_CRASH);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                // Exception
                String authorities = "agenor.org.agenorwallet.agenorwallet.myfileprovider";
                final File cacheDir = getCacheDir();
                // show error report dialog to send the crash
                final ArrayList<Uri> attachments = new ArrayList<>();
                try {
                    final File logDir = getDir("log", Context.MODE_PRIVATE);

                    for (final File logFile : logDir.listFiles()) {
                        final String logFileName = logFile.getName();
                        final File file;
                        if (logFileName.endsWith(".log.gz"))
                            file = File.createTempFile(logFileName.substring(0, logFileName.length() - 6), ".log.gz", cacheDir);
                        else if (logFileName.endsWith(".log"))
                            file = File.createTempFile(logFileName.substring(0, logFileName.length() - 3), ".log", cacheDir);
                        else
                            continue;

                        final InputStream is = new FileInputStream(logFile);
                        final OutputStream os = new FileOutputStream(file);

                        Io.copy(is, os);

                        os.close();
                        is.close();

                        attachments.add(FileProvider.getUriForFile(getApplicationContext(), authorities, file));
                    }

                    shareText(AgenorApplication.this,"Agenor wallet crash", "Unexpected crash", attachments);
                } catch (final IOException x) {
                    log.info("problem writing attachment", x);
                }
                throw new RuntimeException(e);

            }
        });
        executor.shutdown();
    }

    public synchronized void startCore() throws IOException {
        try {
            agenorModule.start();
        }catch (Exception e){
            log.error("Exception on start",e);
            throw e;
        }
    }

    public void startAgenorService() {
        Intent intent = new Intent(this,AgenorWalletService.class);
        startService(intent);
    }

    private void initLogging() {
        final File logDir = getDir("log", MODE_PRIVATE);
        final File logFile = new File(logDir, "app.log");
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final PatternLayoutEncoder filePattern = new PatternLayoutEncoder();
        filePattern.setContext(context);
        filePattern.setPattern("%d{HH:mm:ss,UTC} [%thread] %logger{0} - %msg%n");
        filePattern.start();

        final RollingFileAppender<ILoggingEvent > fileAppender = new RollingFileAppender<ILoggingEvent>();
        fileAppender.setContext(context);
        fileAppender.setFile(logFile.getAbsolutePath());

        final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(logDir.getAbsolutePath() + "/wallet.%d{yyyy-MM-dd,UTC}.log.gz");
        rollingPolicy.setMaxHistory(7);
        rollingPolicy.start();

        fileAppender.setEncoder(filePattern);
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();

        final PatternLayoutEncoder logcatTagPattern = new PatternLayoutEncoder();
        logcatTagPattern.setContext(context);
        logcatTagPattern.setPattern("%logger{0}");
        logcatTagPattern.start();

        final PatternLayoutEncoder logcatPattern = new PatternLayoutEncoder();
        logcatPattern.setContext(context);
        logcatPattern.setPattern("[%thread] %msg%n");
        logcatPattern.start();

        final LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(context);
        logcatAppender.setTagEncoder(logcatTagPattern);
        logcatAppender.setEncoder(logcatPattern);
        logcatAppender.start();

        final ch.qos.logback.classic.Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
        log.addAppender(fileAppender);
        log.addAppender(logcatAppender);
        log.setLevel(Level.INFO);
    }

    public AgenorModule getModule(){
        return agenorModule;
    }

    public AppConf getAppConf(){
        return appConf;
    }

    @Override
    public FileOutputStream openFileOutputPrivateMode(String name) throws FileNotFoundException {
        return openFileOutput(name,MODE_PRIVATE);
    }

    @Override
    public File getDirPrivateMode(String name) {
        return getDir(name,MODE_PRIVATE);
    }

    @Override
    public InputStream openAssestsStream(String name) throws IOException {
        return getAssets().open(name);
    }

    @Override
    public boolean isMemoryLow() {
        final int memoryClass = activityManager.getMemoryClass();
        return memoryClass<=agenorModule.getConf().getMinMemoryNeeded();
    }

    @Override
    public String getVersionName() {
        return info.versionName;
    }

    @Override
    public void stopBlockchain() {
        Intent intent = new Intent(this,AgenorWalletService.class);
        intent.setAction(ACTION_RESET_BLOCKCHAIN);
        startService(intent);
    }

    public void stopBlockchainAndRollBackitTo(int height) {
        Intent intent = new Intent(this,AgenorWalletService.class);
        intent.setAction(ACTION_RESET_BLOCKCHAIN_ROLLBACK_TO);
        intent.putExtra("height", height);
        startService(intent);
    }

    @Override
    public int getCurrentVersionNumber() {
        return info.versionCode;
    }

    public NetworkConf getNetworkConf() {
        return networkConf;
    }

    /**
     *
     * @param trustedServer
     */
    public void setTrustedServer(PivtrumPeerData trustedServer) {
        networkConf.setTrustedServer(trustedServer);
        if (trustedServer == null){
            agenorModule.getConf().cleanTrustedNode();
            appConf.cleanTrustedNode();
        }else {
            agenorModule.getConf().saveTrustedNode(trustedServer.getHost(), trustedServer.getTcpPort());
            appConf.saveTrustedNode(trustedServer);
        }
    }

    public CentralFormats getCentralFormats() {
        return centralFormats;
    }

    public PackageInfo getPackageInfo() {
        return info;
    }

    public WalletConfiguration getWalletConfiguration() {
        return walletConfiguration;
    }

    public static long getTimeCreateApplication() {
        return TIME_CREATE_APPLICATION;
    }

    public long getLastTimeRequestedBackup() {
        return lastTimeRequestBackup;
    }

    public void setLastTimeBackupRequested(long lastTimeBackupRequested) {
        this.lastTimeRequestBackup = lastTimeBackupRequested;
    }

    public boolean isCoreStarted() {
        return agenorModule.isStarted();
    }

    public boolean isCoreStarting() {
        return agenorModule.isStarting();
    }

    public boolean hasCoreCrashed() {
        return hasCoreCrashed.get();
    }
}

package agenor.org.agenorwallet.ui.wallet_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.pivxj.core.Coin;
import org.pivxj.core.Transaction;
import org.pivxj.uri.AgenorURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import chain.BlockchainState;
import de.schildbach.wallet.ui.scan.ScanActivity;
import agenor.org.agenorwallet.R;
import global.exceptions.NoPeerConnectedException;
import global.AgenorRate;
import agenor.org.agenorwallet.module.AgenorContext;
import agenor.org.agenorwallet.ui.base.BaseDrawerActivity;
import agenor.org.agenorwallet.ui.base.dialogs.SimpleTextDialog;
import agenor.org.agenorwallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import agenor.org.agenorwallet.ui.privacy.privacy_convert.ConvertActivity;
import agenor.org.agenorwallet.ui.qr_activity.QrActivity;
import agenor.org.agenorwallet.ui.settings.faq.FaqActivity;
import agenor.org.agenorwallet.ui.settings.settings_backup_activity.SettingsBackupActivity;
import agenor.org.agenorwallet.ui.transaction_request_activity.RequestActivity;
import agenor.org.agenorwallet.ui.transaction_send_activity.SendActivity;
import agenor.org.agenorwallet.ui.upgrade.UpgradeWalletActivity;
import agenor.org.agenorwallet.utils.AnimationUtils;
import agenor.org.agenorwallet.utils.DialogsUtil;

import static android.Manifest.permission.CAMERA;
import static de.schildbach.wallet.ui.scan.ScanActivity.INTENT_EXTRA_RESULT;
import static agenor.org.agenorwallet.service.IntentsConstants.ACTION_NOTIFICATION;
import static agenor.org.agenorwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static agenor.org.agenorwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static agenor.org.agenorwallet.ui.transaction_send_activity.SendActivity.INTENT_ADDRESS;
import static agenor.org.agenorwallet.ui.transaction_send_activity.SendActivity.INTENT_EXTRA_TOTAL_AMOUNT;
import static agenor.org.agenorwallet.ui.transaction_send_activity.SendActivity.INTENT_MEMO;

/**
 * Created by Neoperol on 5/11/17.
 */

public class WalletActivity extends BaseDrawerActivity {

    private static final Logger log = LoggerFactory.getLogger(WalletActivity.class);

    private static final int SCANNER_RESULT = 122;
    private static final int REQUEST_SEND = 300;
    private static final int REQUEST_QR = 301;

    private View root;
    private View container_txs;
    private RelativeLayout bg_balance;
    private TextView txt_value, text_value_bottom, text_value_bottom_local, txt_local_total, txt_unnavailable, txt_local_currency;
    private TextView txt_watch_only;
    private View view_background;
    private View container_syncing;
    private AgenorRate agenorRate;
    private TransactionsFragmentBase txsFragment;
    private Boolean isPrivate = false;
    private FloatingActionButton fab_request, fab_add, fab_convert ;
    private FloatingActionMenu floatingActionMenu;

    // Reminder dialog
    private SimpleTwoButtonsDialog reminderDialog;

    // Receiver
    private LocalBroadcastManager localBroadcastManager;

    private IntentFilter agenorServiceFilter = new IntentFilter(ACTION_NOTIFICATION);
    private BroadcastReceiver agenorServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_NOTIFICATION)){
                if(intent.getStringExtra(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_ON_COIN_RECEIVED)){
                    // Check if the app is on foreground to update the view.
                    if (!isOnForeground)return;
                    runOnUiThread(() -> {
                        updateBalance();
                        showOrHideSyncingContainer();
                        txsFragment.refresh();
                    });
                }
            }
        }
    };

    @Override
    protected void beforeCreate(){
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_wallet, container);
        View containerHeader = getLayoutInflater().inflate(R.layout.fragment_agenor_amount,header_container);
        header_container.setVisibility(View.VISIBLE);
        bg_balance = (RelativeLayout) containerHeader.findViewById(R.id.bg_balance);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("Private")) {
            isPrivate = intent.getBooleanExtra("Private",false);
        }

        fab_request = (FloatingActionButton) findViewById(R.id.fab_request);
        fab_add = (FloatingActionButton) findViewById(R.id.fab_add);
        fab_convert = (FloatingActionButton) findViewById(R.id.fab_convert);

        // Header amount values
        text_value_bottom =  (TextView) findViewById(R.id.text_value_bottom);
        text_value_bottom_local =  (TextView) findViewById(R.id.text_value_bottom_local);
        txt_local_total = (TextView) header_container.findViewById(R.id.txt_local_total);
        txt_value = (TextView) containerHeader.findViewById(R.id.pivValue);
        txt_unnavailable = (TextView) containerHeader.findViewById(R.id.txt_unnavailable);
        txt_local_currency = (TextView) containerHeader.findViewById(R.id.txt_local_currency);
        txt_watch_only = (TextView) containerHeader.findViewById(R.id.txt_watch_only);

        floatingActionMenu = (FloatingActionMenu) root.findViewById(R.id.fab_menu);
        container_txs = root.findViewById(R.id.container_txs);
        view_background = root.findViewById(R.id.view_background);
        container_syncing = root.findViewById(R.id.container_syncing);

        txsFragment = (TransactionsFragmentBase) getSupportFragmentManager().findFragmentById(R.id.transactions_fragment);

        initView();
    }

    private void initView() {
        if (isPrivate) {
            setTitle(R.string.title_privacy);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.darkPurple)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkPurple));
            }
            bg_balance.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.darkPurple));
            fab_add.setColorNormal(ContextCompat.getColor(getBaseContext(), R.color.darkPurple));
            fab_add.setLabelText(getResources().getString(R.string.btn_send_zpiv));
            fab_add.setImageDrawable(getDrawable(R.drawable.ic_fab_send));
            fab_request.setColorNormal(ContextCompat.getColor(getBaseContext(), R.color.darkPurple));
            fab_request.setVisibility(View.GONE);
            fab_convert.setVisibility(View.VISIBLE);
        } else {
            setTitle(R.string.my_wallet);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.bgPurple)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.bgPurple));
            }
            fab_add.setLabelText(getResources().getString(R.string.btn_send_piv));
            bg_balance.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.bgPurple));
            fab_convert.setVisibility(View.GONE);
            fab_request.setVisibility(View.VISIBLE);
            floatingActionMenu.setMenuButtonColorNormal(ContextCompat.getColor(this, R.color.bgPurple));
        }



        // Open Send
        root.findViewById(R.id.fab_add).setOnClickListener(v -> {
            Intent sendintent = new Intent(v.getContext(),SendActivity.class);
            sendintent.putExtra("Private",isPrivate);
            if (agenorModule.isWalletWatchOnly()){
                Toast.makeText(v.getContext(),R.string.error_watch_only_mode,Toast.LENGTH_SHORT).show();
                return;
            }
            floatingActionMenu.close(false);
            startActivityForResult(sendintent, REQUEST_SEND);
        });

        root.findViewById(R.id.fab_request).setOnClickListener(v -> {
            Intent requestIntent = new Intent(v.getContext(), RequestActivity.class);
            requestIntent.putExtra("Private",isPrivate);
            floatingActionMenu.close(false);
            startActivity(requestIntent);
        });

        // Convert
        root.findViewById(R.id.fab_convert).setOnClickListener(v -> {
            floatingActionMenu.close(false);
            startActivity(new Intent(v.getContext(), ConvertActivity.class));
        });


        // Floating menu
        floatingActionMenu.setOnMenuToggleListener(opened -> {
            touchFabActions(opened);
        });


        // Screen changes

        text_value_bottom.setOnClickListener(v -> {
            // not active
            if (AgenorContext.IS_ZEROCOIN_WALLET_ACTIVE) {
                isPrivate = !isPrivate;
                initView();
                updateBalance();
                showOrHideSyncingContainer();
                txsFragment.change(isPrivate);
            }
        });
    }


    @Override
    protected void onResume() {
        try {
            super.onResume();

            // to check current activity in the navigation drawer
            if (AgenorContext.IS_ZEROCOIN_WALLET_ACTIVE){
                setNavigationMenuItemChecked(isPrivate ? 2 : 0);
            }else {
                setNavigationMenuItemChecked(0);
            }


            // register
            try {
                localBroadcastManager.unregisterReceiver(agenorServiceReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (agenorApplication.isCoreStarted() && agenorModule.isStarted()) {
                init();

                localBroadcastManager.registerReceiver(agenorServiceReceiver, agenorServiceFilter);

                updateState();
                updateBalance();
                showOrHideSyncingContainer();
                txsFragment.refresh();

                // check if this wallet need an update:
                try {
                    if (agenorModule.isBip32Wallet() && agenorModule.isSyncWithNode()) {
                        if (!agenorModule.isWalletWatchOnly() && agenorModule.getAvailableBalanceCoin().isGreaterThan(Transaction.DEFAULT_TX_FEE)) {
                            Intent intent = UpgradeWalletActivity.createStartIntent(
                                    this,
                                    getString(R.string.upgrade_wallet),
                                    "An old wallet version with bip32 key was detected, in order to upgrade the wallet your coins are going to be sweeped" +
                                            " to a new wallet with bip44 account.\n\nThis means that your current mnemonic code and" +
                                            " backup file are not going to be valid anymore, please write the mnemonic code in paper " +
                                            "or export the backup file again to be able to backup your coins." +
                                            "\n\nPlease wait and not close this screen. The upgrade + blockchain sychronization could take a while."
                                            + "\n\nTip: If this screen is closed for user's mistake before the upgrade is finished you can find two backups files in the 'Download' folder" +
                                            " with prefix 'old' and 'upgrade' to be able to continue the restore manually."
                                            + "\n\nThanks!",
                                    "sweepBip32"
                            );
                            startActivity(intent);
                        }
                    }

                } catch (NoPeerConnectedException e) {
                    log.info("No peer connection on walletUpdate", e.getMessage());
                }
            } else {
                log.info("@@@@ This should open the loading screen first..");
            }
        }catch (Exception e){
            LoggerFactory.getLogger(WalletActivity.class).error("Error on resume",e);
        }
    }

    private void updateState() {
        txt_watch_only.setVisibility(agenorModule.isWalletWatchOnly()?View.VISIBLE:View.GONE);
    }

    private void init() {
        // Start service if it's not started.
        agenorApplication.startAgenorService();

        if (!agenorApplication.getAppConf().hasBackup()){
            long now = System.currentTimeMillis();
            if (agenorApplication.getLastTimeRequestedBackup()+1800000L<now) {
                agenorApplication.setLastTimeBackupRequested(now);
                if (reminderDialog == null) {
                    reminderDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                            this,
                            getString(R.string.reminder_backup),
                            getString(R.string.reminder_backup_body),
                            new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                                @Override
                                public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                                    startActivity(new Intent(WalletActivity.this, SettingsBackupActivity.class));
                                    dialog.dismiss();
                                }

                                @Override
                                public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                                    dialog.dismiss();
                                }
                            }
                    );
                    reminderDialog.setLeftBtnText(getString(R.string.button_dismiss));
                    reminderDialog.setLeftBtnTextColor(Color.BLACK);
                    reminderDialog.setRightBtnText(getString(R.string.button_ok));
                }
                reminderDialog.show();
            }
        }
    }

    @Override
    protected void onStop() {
        try {
            super.onStop();
            // unregister
            //localBroadcastManager.unregisterReceiver(localReceiver);
            localBroadcastManager.unregisterReceiver(agenorServiceReceiver);
        }catch (Exception e){
            LoggerFactory.getLogger(WalletActivity.class).error("Error on stop",e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!isPrivate) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.main_private, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_qr){
            Intent intent = new Intent(this, QrActivity.class);
            intent.putExtra("Private", isPrivate);
            startActivityForResult(intent, REQUEST_QR);
            return true;
        }else if (item.getItemId()==R.id.action_scan){
            if (!checkPermission(CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permsRequestCode = 200;
                    String[] perms = {"android.permission.CAMERA"};
                    requestPermissions(perms, permsRequestCode);
                }
            }
            startActivityForResult(new Intent(this, ScanActivity.class),SCANNER_RESULT);
            return true;
        } else if (item.getItemId() == R.id.action_faq){
            startActivity(new Intent(this, FaqActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT){
            if (resultCode == RESULT_OK) {
                try {
                    String address = data.getStringExtra(INTENT_EXTRA_RESULT);
                    final String usedAddress;
                    if (agenorModule.chechAddress(address)){
                        usedAddress = address;
                    }else {
                        AgenorURI agenorUri = new AgenorURI(address);
                        usedAddress = agenorUri.getAddress().toBase58();
                        final Coin amount = agenorUri.getAmount();
                        if (amount != null){
                            final String memo = agenorUri.getMessage();
                            StringBuilder text = new StringBuilder();
                            text.append(getString(R.string.amount)).append(": ").append(amount.toFriendlyString());
                            if (memo != null){
                                text.append("\n").append(getString(R.string.description)).append(": ").append(memo);
                            }

                            SimpleTextDialog dialogFragment = DialogsUtil.buildSimpleTextDialog(this,
                                    getString(R.string.payment_request_received),
                                    text.toString())
                                .setOkBtnClickListener(v -> {
                                    Intent intent = new Intent(v.getContext(), SendActivity.class);
                                    intent.putExtra(INTENT_ADDRESS,usedAddress);
                                    intent.putExtra(INTENT_EXTRA_TOTAL_AMOUNT,amount);
                                    intent.putExtra(INTENT_MEMO,memo);
                                    startActivity(intent);
                                });
                            dialogFragment.setImgAlertRes(R.drawable.ic_fab_send);
                            dialogFragment.setAlignBody(SimpleTextDialog.Align.LEFT);
                            dialogFragment.setImgAlertRes(R.drawable.ic_fab_recieve);
                            dialogFragment.show(getFragmentManager(),"payment_request_dialog");
                            return;
                        }

                    }
                    DialogsUtil.showCreateAddressLabelDialog(this,usedAddress);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this,"Bad address",Toast.LENGTH_LONG).show();
                }
            }
        }else if (REQUEST_SEND == requestCode){
            if(floatingActionMenu.isOpened()){
                touchFabActions(true);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),permission);

        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void updateBalance() {

        Coin availableBalance = agenorModule.getAvailableBalanceCoin();
        Coin unnavailableBalance = agenorModule.getUnnavailableBalanceCoin();
        Coin zAvailableBalance = agenorModule.getZpivAvailableBalanceCoin();
        Coin zUnspendable = agenorModule.getZpivUnnavailableBalanceCoin();
        if (!isPrivate) {
            updateBalanceViews(
                    availableBalance,
                    unnavailableBalance,
                    "AGE",
                    zAvailableBalance,
                    zUnspendable,
                    "zAGE"
            );
        }else {
            updateBalanceViews(
                    zAvailableBalance,
                    zUnspendable,
                    "zAGE",
                    availableBalance,
                    unnavailableBalance,
                    "AGE"
            );
        }

        Coin sum = availableBalance.add(zAvailableBalance);
        if (agenorRate != null) {
            txt_local_total.setText(
                    agenorApplication.getCentralFormats().format(
                            new BigDecimal(sum.getValue() * agenorRate.getRate().doubleValue()).movePointLeft(8)
                    )
                            + " " + agenorRate.getCode()
            );
        } else {
            txt_local_total.setText("0.00 USD");
        }

    }

    private void updateBalanceViews(Coin topBalance,Coin topUnspendableBalance, String topDen, Coin bottomBalance, Coin bottomUnspendable, String bottomDen){
        txt_value.setText(!topBalance.isZero() ? topBalance.toPlainString() + " " + topDen : "0 " + topDen);
        txt_unnavailable.setText(!topUnspendableBalance.isZero() ? topUnspendableBalance.toPlainString() + " " + topDen : "0 " + topDen);

        text_value_bottom.setText(!bottomBalance.isZero() ? bottomBalance.toPlainString() + " " + bottomDen : "0 " + bottomDen);

        if (agenorRate == null)
            agenorRate = agenorModule.getRate(agenorApplication.getAppConf().getSelectedRateCoin());
        if (agenorRate != null) {
            txt_local_currency.setText(
                    agenorApplication.getCentralFormats().format(
                            new BigDecimal(topBalance.getValue() * agenorRate.getRate().doubleValue()).movePointLeft(8)
                    )
                            + " " + agenorRate.getCode()
            );
            text_value_bottom_local.setText(
                    agenorApplication.getCentralFormats().format(
                            new BigDecimal(bottomBalance.getValue() * agenorRate.getRate().doubleValue()).movePointLeft(8)
                    )
                            + " " + agenorRate.getCode()
            );
        } else {
            txt_local_currency.setText("0.00 USD");
        }
    }

    public void touchFabActions(boolean isOpened){
        if (isOpened){
            AnimationUtils.fadeInView(view_background,200);
        }else {
            AnimationUtils.fadeOutGoneView(view_background,200);
        }
    }

    @Override
    protected void onBlockchainStateChange(){
        showOrHideSyncingContainer();
    }

    private void showOrHideSyncingContainer(){
        if (blockchainState == BlockchainState.SYNCING){
            AnimationUtils.fadeInView(container_syncing,500);
        }else if (blockchainState == BlockchainState.SYNC){
            AnimationUtils.fadeOutGoneView(container_syncing,500);
        }else if (blockchainState == BlockchainState.NOT_CONNECTION){
            AnimationUtils.fadeInView(container_syncing,500);
        }
    }
}

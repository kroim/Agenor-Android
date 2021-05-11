package agenor.org.agenorwallet.ui.loading;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Window;
import android.view.WindowManager;

import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import agenor.org.agenorwallet.AgenorApplication;
import agenor.org.agenorwallet.R;
import agenor.org.agenorwallet.ui.start_activity.StartActivity;
import agenor.org.agenorwallet.ui.wallet_activity.WalletActivity;

import static agenor.org.agenorwallet.ui.backup_mnemonic_activity.MnemonicActivity.INTENT_EXTRA_INIT_VIEW;

public class LoadingActivity extends AppCompatActivity {

    boolean isInit = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // remove title
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(INTENT_EXTRA_INIT_VIEW)){
            isInit = intent.getBooleanExtra(INTENT_EXTRA_INIT_VIEW,false);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_loading);

//        LottieAnimationView mAnimationView = (LottieAnimationView) findViewById(R.id.animation_view);
//        LottieComposition.Factory.fromAssetFileName(this, "loading_animation.json", new OnCompositionLoadedListener() {
//            @Override
//            public void onCompositionLoaded(LottieComposition composition) {
//                mAnimationView.loop(true);
//                mAnimationView.playAnimation();
//            }
//        });


        start(TimeUnit.SECONDS.toMillis(4));
    }

    private void start(long millis){
        new Handler().postDelayed(
                () -> {
                    AgenorApplication app = AgenorApplication.getInstance();
                    if(app.isCoreStarted()) {
                        runOnUiThread(() -> {
                            if (isInit){
                                setResult(RESULT_OK);
                            }else {
                                if (AgenorApplication.getInstance().getAppConf().isAppInit()) {
                                    Intent intent = new Intent(this, WalletActivity.class);
                                    startActivity(intent);
                                } else {
                                    // Jump to your Next Activity or MainActivity
                                    Intent intent = new Intent(this, StartActivity.class);
                                    startActivity(intent);
                                }
                            }
                            finish();
                        });
                    }else {
                        if (!app.isCoreStarting()){
                            app.startCoreBackground();
                        }
                        if (app.hasCoreCrashed()){
                            // Do something here maybe..
                            LoggerFactory.getLogger(LoadingActivity.class).info("Core crashed, finishing loading..");
                            finish();
                        }
                        start(TimeUnit.SECONDS.toMillis(1));
                    }
                }, millis
        );
    }
}

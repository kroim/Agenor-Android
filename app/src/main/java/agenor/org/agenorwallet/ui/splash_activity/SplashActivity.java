package agenor.org.agenorwallet.ui.splash_activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import agenor.org.agenorwallet.AgenorApplication;
import agenor.org.agenorwallet.R;
import agenor.org.agenorwallet.ui.loading.LoadingActivity;
import agenor.org.agenorwallet.ui.start_activity.StartActivity;
import agenor.org.agenorwallet.ui.wallet_activity.WalletActivity;

/**
 * Created by Neoperol on 6/13/17.
 */

public class SplashActivity extends AppCompatActivity {
    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2500;
    private boolean ispaused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(this::jump, SPLASH_DISPLAY_LENGTH);
    }


    private void jump() {

        AgenorApplication app = AgenorApplication.getInstance();
        if (!app.hasCoreCrashed()) {
            if (app.isCoreStarting()){
                Intent intent = new Intent(this, LoadingActivity.class);
                startActivity(intent);
            }else {
                if (app.getAppConf().isAppInit()) {
                    Intent intent = new Intent(this, WalletActivity.class);
                    startActivity(intent);
                } else {
                    // Jump to your Next Activity or MainActivity
                    Intent intent = new Intent(this, StartActivity.class);
                    startActivity(intent);
                }
            }
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ispaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ispaused) {
            jump();
        }

    }
}

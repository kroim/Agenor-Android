package agenor.org.agenorwallet.ui.initial;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import agenor.org.agenorwallet.AgenorApplication;
import agenor.org.agenorwallet.ui.splash_activity.SplashActivity;
import agenor.org.agenorwallet.ui.wallet_activity.WalletActivity;
import agenor.org.agenorwallet.utils.AppConf;

/**
 * Created by furszy on 8/19/17.
 */

public class InitialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AgenorApplication agenorApplication = AgenorApplication.getInstance();
        AppConf appConf = agenorApplication.getAppConf();
        // show report dialog if something happen with the previous process
        Intent intent;
        if (!appConf.isAppInit() || appConf.isSplashSoundEnabled()){
            intent = new Intent(this, SplashActivity.class);
        }else {
            intent = new Intent(this, WalletActivity.class);
        }
        startActivity(intent);
        finish();
    }
}

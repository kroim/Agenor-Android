package agenor.org.agenorwallet.utils;

import android.app.Activity;
import android.content.Intent;

import agenor.org.agenorwallet.ui.wallet_activity.WalletActivity;

/**
 * Created by furszy on 10/19/17.
 */

public class NavigationUtils {

    public static void goBackToHome(Activity activity){
        Intent upIntent = new Intent(activity,WalletActivity.class);
        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(upIntent);
        activity.finish();
    }

}

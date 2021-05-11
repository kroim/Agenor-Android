package agenor.org.agenorwallet.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import agenor.org.agenorwallet.AgenorApplication;
import agenor.org.agenorwallet.R;
import global.AgenorModule;
import agenor.org.agenorwallet.ui.base.dialogs.SimpleTextDialog;
import agenor.org.agenorwallet.ui.loading.LoadingActivity;
import agenor.org.agenorwallet.utils.DialogsUtil;

import static agenor.org.agenorwallet.service.IntentsConstants.ACTION_STORED_BLOCKCHAIN_ERROR;
import static agenor.org.agenorwallet.service.IntentsConstants.ACTION_TRUSTED_PEER_CONNECTION_FAIL;

/**
 * Created by furszy on 6/8/17.
 */

public class AgenorActivity extends AppCompatActivity {

    protected AgenorApplication agenorApplication;
    protected AgenorModule agenorModule;

    protected LocalBroadcastManager localBroadcastManager;
    private static final IntentFilter intentFilter = new IntentFilter(ACTION_TRUSTED_PEER_CONNECTION_FAIL);
    private static final IntentFilter errorIntentFilter = new IntentFilter(ACTION_STORED_BLOCKCHAIN_ERROR);

    protected boolean isOnForeground = false;

    private BroadcastReceiver trustedPeerConnectionDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_TRUSTED_PEER_CONNECTION_FAIL)) {
                SimpleTextDialog simpleTextDialog = DialogsUtil.buildSimpleErrorTextDialog(context,R.string.title_no_trusted_peer_connection,R.string.message_no_trusted_peer_connection);
                simpleTextDialog.show(getFragmentManager(),"fail_node_connection_dialog");
            }else if (action.equals(ACTION_STORED_BLOCKCHAIN_ERROR)){
                SimpleTextDialog simpleTextDialog = DialogsUtil.buildSimpleErrorTextDialog(context,R.string.title_blockstore_error,R.string.message_blockstore_error);
                simpleTextDialog.show(getFragmentManager(),"blockstore_error_dialog");
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        agenorApplication = AgenorApplication.getInstance();
        agenorModule = agenorApplication.getModule();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isCoreNeeded()) {
            if (!agenorApplication.isCoreStarted()) {
                Intent intent = new Intent(this, LoadingActivity.class);
                startActivity(intent);
                finish();
            } else {
                isOnForeground = true;
                localBroadcastManager.registerReceiver(trustedPeerConnectionDownReceiver, intentFilter);
                localBroadcastManager.registerReceiver(trustedPeerConnectionDownReceiver, errorIntentFilter);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isOnForeground = false;
        localBroadcastManager.unregisterReceiver(trustedPeerConnectionDownReceiver);
    }

    public boolean isCoreNeeded(){
        return true;
    }
}

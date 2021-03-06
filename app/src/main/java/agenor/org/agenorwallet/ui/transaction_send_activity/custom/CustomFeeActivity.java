package agenor.org.agenorwallet.ui.transaction_send_activity.custom;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import agenor.org.agenorwallet.R;
import agenor.org.agenorwallet.ui.base.BaseActivity;
import agenor.org.agenorwallet.utils.DialogsUtil;

import static agenor.org.agenorwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_CLEAR;
import static agenor.org.agenorwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_FEE;
import static agenor.org.agenorwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_FEE_PER_KB;
import static agenor.org.agenorwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_MINIMUM_FEE;
import static agenor.org.agenorwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_TOTAL_FEE;

/**
 * Created by furszy on 8/3/17.
 */

public class CustomFeeActivity extends BaseActivity {
    private Button btnSave;
    private View root;
    private CustomFeeFragment customFeeFragment;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.custom_fee_main, container);
        setTitle("Custom fee");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        customFeeFragment = (CustomFeeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_custom_fee);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    CustomFeeFragment.FeeSelector feeSelector = customFeeFragment.getFee();
                    intent.putExtra(INTENT_EXTRA_IS_FEE_PER_KB,feeSelector.isFeePerKbSelected());
                    intent.putExtra(INTENT_EXTRA_IS_TOTAL_FEE,!feeSelector.isFeePerKbSelected());
                    intent.putExtra(INTENT_EXTRA_IS_MINIMUM_FEE,feeSelector.isPayMinimum());
                    intent.putExtra(INTENT_EXTRA_FEE,feeSelector.getAmount());
                    setResult(RESULT_OK,intent);
                    finish();
                } catch (InvalidFeeException e) {
                    e.printStackTrace();
                    DialogsUtil.buildSimpleErrorTextDialog(getBaseContext(),getString(R.string.invalid_inputs),e.getMessage()).show(getFragmentManager(),"custom_fee_invalid_inputs");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu_default,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_default){
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_CLEAR,true);
            setResult(RESULT_OK,intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}

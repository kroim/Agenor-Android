package agenor.org.agenorwallet.ui.transaction_detail_activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.zerocoinj.core.ZCoin;

import org.json.JSONArray;
import org.pivxj.core.TransactionOutput;

import global.wrappers.TransactionWrapper;
import agenor.org.agenorwallet.R;
import agenor.org.agenorwallet.ui.base.BaseActivity;
import agenor.org.agenorwallet.ui.export_zpiv.ExportZpiv;

import static agenor.org.agenorwallet.ui.transaction_detail_activity.FragmentTxDetail.IS_DETAIL;
import static agenor.org.agenorwallet.ui.transaction_detail_activity.FragmentTxDetail.TX_WRAPPER;

/**
 * Created by Neoperol on 6/9/17.
 */

public class TransactionDetailActivity extends BaseActivity {

    private TransactionWrapper transactionWrapper;

    private static final int ITEM_EXPORT_ZCOIN = 400;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.transaction_detail_main, container);
        setTitle(R.string.title_transaction_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        if (intent != null){
            transactionWrapper = (TransactionWrapper) intent.getSerializableExtra(TX_WRAPPER);
            if (intent.hasExtra(IS_DETAIL)){
                transactionWrapper.setTransaction(agenorModule.getTx(transactionWrapper.getTxId()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*MenuItem menuItem = menu.add(0,0,0,R.string.explorer);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);*/
        if (transactionWrapper.isZcMint()){
            MenuItem menuItem = menu.add(0,ITEM_EXPORT_ZCOIN,0,R.string.export_zcoin);
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ITEM_EXPORT_ZCOIN:
                Intent intent = new Intent(this, ExportZpiv.class);
                JSONArray json = new JSONArray();
                for (TransactionOutput transactionOutput : transactionWrapper.getTransaction().getOutputs()) {
                    if (transactionOutput.isZcMint()){
                        ZCoin zCoin = agenorModule.getAssociatedCoin(transactionOutput.getScriptPubKey().getCommitmentValue());
                        if (zCoin != null){
                            json.put(zCoin.toJsonString());
                        }
                    }
                }
                intent.putExtra(ExportZpiv.INTENT_EXTRA_EXPORT_ZCOINS,json.toString());
                startActivity(intent);
                return true;
            //case 0:
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com"));
                //startActivity(browserIntent);
            //    return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = getIntent();
        setResult(RESULT_OK,intent);
        finish();
    }
}

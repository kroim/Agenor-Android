package agenor.org.agenorwallet.ui.transaction_send_activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.pivxj.core.Coin;

import java.math.BigDecimal;

import agenor.org.agenorwallet.R;
import global.AgenorRate;
import agenor.org.agenorwallet.ui.base.BaseFragment;

/**
 * Created by furszy on 2/9/18.
 */

public class AmountInputFragment extends BaseFragment implements View.OnClickListener {

    private View root;

    private EditText edit_amount, editCurrency;
    private TextView title_local_currency, txtShowPiv,txt_local_currency, title_amount_piv;
    private ImageButton btnSwap;
    private ViewFlipper amountSwap;
    private AgenorRate agenorRate;
    private boolean inPivs = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.amount_input,container,false);
        edit_amount = (EditText) root.findViewById(R.id.edit_amount);
        //Sending amount currency
        editCurrency = (EditText) root.findViewById(R.id.edit_amount_currency);
        title_local_currency = (TextView) root.findViewById(R.id.title_local_currency);
        title_amount_piv = (TextView) root.findViewById(R.id.title_amount_piv);
        title_amount_piv.setText(getText(R.string.title_amount));
        txt_local_currency = (TextView) root.findViewById(R.id.txt_local_currency);

        txtShowPiv = (TextView) root.findViewById(R.id.txt_show_piv) ;
        //Swap type of ammounts
        amountSwap = (ViewFlipper) root.findViewById( R.id.viewFlipper );
        amountSwap.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.slide_in_left));
        amountSwap.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.slide_out_right));
        btnSwap = (ImageButton) root.findViewById(R.id.btn_swap);
        btnSwap.setOnClickListener(this);

        agenorRate = agenorModule.getRate(agenorApplication.getAppConf().getSelectedRateCoin());

        if (agenorRate != null) {
            txt_local_currency.setText("0 " + agenorRate.getCode());
            editCurrency.setHint(agenorRate.getCode() + " " + getText(R.string.title_equivalent));
            title_local_currency.setText(getText(R.string.amount) + "  (" + agenorRate.getCode() + " " + getText(R.string.title_equivalent)+ ")");
        }
        else {
            txt_local_currency.setText("0");
        }
        editCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (agenorRate != null) {
                    if (s.length() > 0) {
                        String valueStr = s.toString();
                        if (valueStr.charAt(0) == '.') {
                            valueStr = "0" + valueStr;
                        }
                        BigDecimal result = new BigDecimal(valueStr).divide(agenorRate.getRate(), 6, BigDecimal.ROUND_DOWN);
                        txtShowPiv.setText(result.toPlainString() + " AGE");
                    } else {
                        txtShowPiv.setText("0 " + agenorRate.getCode());
                    }
                }else {
                    txtShowPiv.setText(R.string.no_rate);
                }
            }
        });

        edit_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0) {
                    if (agenorRate != null) {
                        String valueStr = s.toString();
                        if (valueStr.charAt(0) == '.') {
                            valueStr = "0" + valueStr;
                        }
                        Coin coin = Coin.parseCoin(valueStr);
                        txt_local_currency.setText(
                                agenorApplication.getCentralFormats().format(
                                        new BigDecimal(coin.getValue() * agenorRate.getRate().doubleValue()).movePointLeft(8)
                                )
                                        + " " + agenorRate.getCode()
                        );
                    }else {
                        // rate null -> no connection.
                        txt_local_currency.setText(R.string.no_rate);
                    }
                }else {
                    if (agenorRate!=null)
                        txt_local_currency.setText("0 "+agenorRate.getCode());
                    else
                        txt_local_currency.setText(R.string.no_rate);
                }
            }
        });

        return root;
    }

    public String getAmountStr() throws Exception {
        if (edit_amount == null && editCurrency == null){
            throw new Exception("Fragment is not attached");
        }
        String amountStr = "0";
        if (inPivs) {
            amountStr = edit_amount.getText().toString();
        }else {
            // the value is already converted
            String valueStr = txtShowPiv.getText().toString();
            amountStr = valueStr.replace(" AGE","");
            if(valueStr.length() > 0) {
                if (valueStr.charAt(0) == '.') {
                    amountStr = "0" + valueStr;
                }
            }
        }
        return amountStr;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btn_swap) {
            inPivs = !inPivs;
            amountSwap.showNext();
        }
    }
}

package agenor.org.agenorwallet.utils;

import org.pivxj.core.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import global.AddressLabel;
import global.AgenorModule;
import global.wrappers.TransactionWrapper;

/**
 * Created by furszy on 8/14/17.
 */

public class TxUtils {

    private static Logger logger = LoggerFactory.getLogger(TxUtils.class);

    public static String getAddressOrContact(AgenorModule agenorModule, TransactionWrapper data) {
        String text;
        if (data.getOutputLabels()!=null && !data.getOutputLabels().isEmpty()){
            Collection<AddressLabel> addressLabels = data.getOutputLabels().values();
            AddressLabel addressLabel = addressLabels.iterator().next();
            if (addressLabel !=null) {
                if (addressLabel.getName() != null)
                    text = addressLabel.getName();
                else
                    text = addressLabel.getAddresses().get(0);
            }else {
                try {
                    text = data.getTransaction().getOutput(0).getScriptPubKey().getToAddress(agenorModule.getConf().getNetworkParams(), true).toBase58();
                }catch (ScriptException e) {
                    try {
                        text = data.getTransaction().getOutput(1).getScriptPubKey().getToAddress(agenorModule.getConf().getNetworkParams(), true).toBase58();
                    }catch (Exception e1){
                        logger.error("######## ERROR THAT NEEDS TO BE CHANGED..",e1);
                        text = "Error";
                    }
                }
            }
        }else {
            text = "Error";
            logger.warn(data.toString());
        }
        return text;
    }

}

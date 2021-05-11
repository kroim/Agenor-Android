package agenor.org.agenorwallet.ui.wallet_activity;

import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import agenor.org.agenorwallet.R;
import agenor.org.agenorwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;

/**
 * Created by Neoperol on 5/3/17.
 */


public class TransactionViewHolderBase extends BaseRecyclerViewHolder {

    CardView cv;
    TextView title;
    TextView description;
    TextView amount;
    TextView amountLocal;
    ImageView imageView, img_pending;
    TextView txt_scale;

    public TransactionViewHolderBase(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.description);
        amount = (TextView) itemView.findViewById(R.id.amount);
        txt_scale = (TextView) itemView.findViewById(R.id.txt_scale);
        amountLocal = (TextView) itemView.findViewById(R.id.txt_local_currency);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
        img_pending = (ImageView) itemView.findViewById(R.id.img_pending);
    }

}

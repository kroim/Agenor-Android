package agenor.org.agenorwallet.ui.settings.faq;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;

import agenor.org.agenorwallet.R;

class ParentFaqHolder extends ParentViewHolder{

    public TextView textView;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public ParentFaqHolder(@NonNull View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.textView);
    }
}

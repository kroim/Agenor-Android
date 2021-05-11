package agenor.org.agenorwallet.ui.settings.settings_rates;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import agenor.org.agenorwallet.R;
import global.AgenorRate;
import agenor.org.agenorwallet.ui.base.BaseRecyclerFragment;
import agenor.org.agenorwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import agenor.org.agenorwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import agenor.org.agenorwallet.ui.base.tools.adapter.ListItemListeners;

/**
 * Created by furszy on 7/2/17.
 */

public class RatesFragment extends BaseRecyclerFragment<AgenorRate> implements ListItemListeners<AgenorRate> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setEmptyText("No rate available");
        setEmptyTextColor(Color.parseColor("#cccccc"));
        return view;
    }

    @Override
    protected List<AgenorRate> onLoading() {
        return agenorModule.listRates();
    }

    @Override
    protected BaseRecyclerAdapter<AgenorRate, ? extends AgenorRateHolder> initAdapter() {
        BaseRecyclerAdapter<AgenorRate, AgenorRateHolder> adapter = new BaseRecyclerAdapter<AgenorRate, AgenorRateHolder>(getActivity()) {
            @Override
            protected AgenorRateHolder createHolder(View itemView, int type) {
                return new AgenorRateHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.rate_row;
            }

            @Override
            protected void bindHolder(AgenorRateHolder holder, AgenorRate data, int position) {
                holder.txt_name.setText(data.getCode());
                if (list.get(0).getCode().equals(data.getCode()))
                    holder.view_line.setVisibility(View.GONE);
            }
        };
        adapter.setListEventListener(this);
        return adapter;
    }

    @Override
    public void onItemClickListener(AgenorRate data, int position) {
        agenorApplication.getAppConf().setSelectedRateCoin(data.getCode());
        Toast.makeText(getActivity(),R.string.rate_selected,Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }

    @Override
    public void onLongItemClickListener(AgenorRate data, int position) {

    }

    private  class AgenorRateHolder extends BaseRecyclerViewHolder{

        private TextView txt_name;
        private View view_line;

        protected AgenorRateHolder(View itemView, int holderType) {
            super(itemView, holderType);
            txt_name = (TextView) itemView.findViewById(R.id.txt_name);
            view_line = itemView.findViewById(R.id.view_line);
        }
    }
}

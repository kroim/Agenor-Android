package agenor.org.agenorwallet.ui.settings.faq;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import agenor.org.agenorwallet.R;
import agenor.org.agenorwallet.ui.base.BaseActivity;

public class FaqActivity extends BaseActivity{

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        super.onCreateView(savedInstanceState, container);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.settings_title_faq);
        View root = getLayoutInflater().inflate(R.layout.settings_faq,container);
        mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerview);
        ExpandableFaqAdapter adapter = new ExpandableFaqAdapter(
                this,
                Lists.newArrayList(
                        new StringParent("Why i cannot spend my minted zAGE?", "Mints minimum confirmation is 20 blocks, so you have to wait an average of 20 minutes to spend it"),
                        new StringParent("Why zAGE spends takes some time?", "Privacy takes time.."),
                        new StringParent("Are my zAGE private?", "Yes, they are"),
                        new StringParent("Is Mrs-X a guy or a girl?", "Ask him"),
                        new StringParent("Why i shouldn't store my life savings on the mobile wallet", "Are you telling me that your life savings are stored on a device that you are carrying everywhere?"),
                        new StringParent("Can i stake on the mobile wallet?", "Not yet"),
                        new StringParent("What is the mnemonic code?", "A long phrase..")
                )
        );
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(layoutManager);
    }
}

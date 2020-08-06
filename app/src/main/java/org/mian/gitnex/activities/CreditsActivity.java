package org.mian.gitnex.activities;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CreditsAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author M M Arif
 */

public class CreditsActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    final Context ctx = this;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_credits;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ImageView closeActivity = findViewById(R.id.close);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        Resources res = getResources();
        CharSequence[] creditsInfo = res.getTextArray(R.array.creditsInfo);

        List<CharSequence> creditsList = new ArrayList<>(Arrays.asList(creditsInfo));

        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        CreditsAdapter adapter = new CreditsAdapter(creditsList);
        mRecyclerView.setAdapter(adapter);

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }
}

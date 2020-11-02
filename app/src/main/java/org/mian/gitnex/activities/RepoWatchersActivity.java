package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoWatchersAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.viewmodels.RepoWatchersViewModel;

/**
 * Author M M Arif
 */

public class RepoWatchersActivity extends BaseActivity {

    private TextView noDataWatchers;
    private View.OnClickListener onClickListener;
    private RepoWatchersAdapter adapter;
    private GridView mGridView;
    private ProgressBar mProgressBar;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_repo_watchers;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ImageView closeActivity = findViewById(R.id.close);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        noDataWatchers = findViewById(R.id.noDataWatchers);
        mGridView = findViewById(R.id.gridView);
        mProgressBar = findViewById(R.id.progress_bar);

        String repoFullNameForWatchers = getIntent().getStringExtra("repoFullNameForWatchers");
        String[] parts = repoFullNameForWatchers.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        toolbarTitle.setText(R.string.repoWatchersInMenu);

        fetchDataAsync(Authorization.get(ctx), repoOwner, repoName);
    }

    private void fetchDataAsync(String instanceToken, String repoOwner, String repoName) {

        RepoWatchersViewModel repoWatchersModel = new ViewModelProvider(this).get(RepoWatchersViewModel.class);

        repoWatchersModel.getRepoWatchers(instanceToken, repoOwner, repoName, ctx).observe(this, watchersListMain -> {

            adapter = new RepoWatchersAdapter(ctx, watchersListMain);

            if(adapter.getCount() > 0) {

                mGridView.setAdapter(adapter);
                noDataWatchers.setVisibility(View.GONE);
            }
            else {

                adapter.notifyDataSetChanged();
                mGridView.setAdapter(adapter);
                noDataWatchers.setVisibility(View.VISIBLE);
            }

            mProgressBar.setVisibility(View.GONE);
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

}

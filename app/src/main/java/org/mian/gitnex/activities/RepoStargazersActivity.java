package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoStargazersAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.RepoStargazersViewModel;

/**
 * Author M M Arif
 */

public class RepoStargazersActivity extends BaseActivity {

    private TextView noDataStargazers;
    private View.OnClickListener onClickListener;
    private RepoStargazersAdapter adapter;
    private GridView mGridView;
    private ProgressBar mProgressBar;

    final Context ctx = this;
    private Context appCtx;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_repo_stargazers;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();

        TinyDB tinyDb = new TinyDB(appCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        ImageView closeActivity = findViewById(R.id.close);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        noDataStargazers = findViewById(R.id.noDataStargazers);
        mGridView = findViewById(R.id.gridView);
        mProgressBar = findViewById(R.id.progress_bar);

        String repoFullNameForStars = getIntent().getStringExtra("repoFullNameForStars");
        String[] parts = repoFullNameForStars.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        toolbarTitle.setText(R.string.repoStargazersInMenu);

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);
    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, String repoOwner, String repoName) {

        RepoStargazersViewModel repoStargazersModel = new ViewModelProvider(this).get(RepoStargazersViewModel.class);

        repoStargazersModel.getRepoStargazers(instanceUrl, instanceToken, repoOwner, repoName, ctx).observe(this, stargazersListMain -> {

            adapter = new RepoStargazersAdapter(ctx, stargazersListMain);

            if(adapter.getCount() > 0) {

                mGridView.setAdapter(adapter);
                noDataStargazers.setVisibility(View.GONE);
            }
            else {

                adapter.notifyDataSetChanged();
                mGridView.setAdapter(adapter);
                noDataStargazers.setVisibility(View.VISIBLE);
            }

            mProgressBar.setVisibility(View.GONE);
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

}

package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoWatchersAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.viewmodels.RepoWatchersViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepoWatchersActivity extends BaseActivity {

    private TextView noDataWatchers;
    private View.OnClickListener onClickListener;
    private RepoWatchersAdapter adapter;
    private GridView mGridView;
    private ProgressBar mProgressBar;

    final Context ctx = this;
    private Context appCtx;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_repo_watchers;
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

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);

    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, String repoOwner, String repoName) {

        RepoWatchersViewModel repoWatchersModel = new ViewModelProvider(this).get(RepoWatchersViewModel.class);

        repoWatchersModel.getRepoWatchers(instanceUrl, instanceToken, repoOwner, repoName, ctx).observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(@Nullable List<UserInfo> watchersListMain) {
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
            }
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

}

package org.mian.gitnex.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoWatchersAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.RepoWatchersViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepoWatchersActivity extends AppCompatActivity {

    private TextView noDataWatchers;
    private View.OnClickListener onClickListener;
    private RepoWatchersAdapter adapter;
    private GridView mGridView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_watchers);

        TinyDB tinyDb = new TinyDB(getApplicationContext());
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

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, String repoOwner, String repoName) {

        RepoWatchersViewModel repoWatchersModel = new ViewModelProvider(this).get(RepoWatchersViewModel.class);

        repoWatchersModel.getRepoWatchers(instanceUrl, instanceToken, repoOwner, repoName).observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(@Nullable List<UserInfo> watchersListMain) {
                adapter = new RepoWatchersAdapter(getApplicationContext(), watchersListMain);
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
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }

}

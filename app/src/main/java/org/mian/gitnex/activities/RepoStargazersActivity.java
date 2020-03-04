package org.mian.gitnex.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoStargazersAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.RepoStargazersViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepoStargazersActivity extends BaseActivity {

    private TextView noDataStargazers;
    private View.OnClickListener onClickListener;
    private RepoStargazersAdapter adapter;
    private GridView mGridView;
    private ProgressBar mProgressBar;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_repo_stargazers;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TinyDB tinyDb = new TinyDB(getApplicationContext());
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

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, String repoOwner, String repoName) {

        RepoStargazersViewModel repoStargazersModel = new ViewModelProvider(this).get(RepoStargazersViewModel.class);

        repoStargazersModel.getRepoStargazers(instanceUrl, instanceToken, repoOwner, repoName, getApplicationContext()).observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(@Nullable List<UserInfo> stargazersListMain) {
                adapter = new RepoStargazersAdapter(getApplicationContext(), stargazersListMain);
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

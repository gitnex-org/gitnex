package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.databinding.ActivityRepoStargazersBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.viewmodels.RepoStargazersViewModel;

/**
 * Author M M Arif
 */

public class RepoStargazersActivity extends BaseActivity {

    private TextView noDataStargazers;
    private View.OnClickListener onClickListener;
    private UserGridAdapter adapter;
    private GridView mGridView;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {

	    super.onCreate(savedInstanceState);

	    ActivityRepoStargazersBinding activityRepoStargazersBinding = ActivityRepoStargazersBinding.inflate(getLayoutInflater());
	    setContentView(activityRepoStargazersBinding.getRoot());

        ImageView closeActivity = activityRepoStargazersBinding.close;
        TextView toolbarTitle = activityRepoStargazersBinding.toolbarTitle;
        noDataStargazers = activityRepoStargazersBinding.noDataStargazers;
        mGridView = activityRepoStargazersBinding.gridView;
        mProgressBar = activityRepoStargazersBinding.progressBar;

        String repoFullNameForStars = getIntent().getStringExtra("repoFullNameForStars");
        String[] parts = repoFullNameForStars.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        toolbarTitle.setText(R.string.repoStargazersInMenu);

        fetchDataAsync(Authorization.get(ctx), repoOwner, repoName);
    }

    private void fetchDataAsync(String instanceToken, String repoOwner, String repoName) {

        RepoStargazersViewModel repoStargazersModel = new ViewModelProvider(this).get(RepoStargazersViewModel.class);

        repoStargazersModel.getRepoStargazers(instanceToken, repoOwner, repoName, ctx).observe(this, stargazersListMain -> {

            adapter = new UserGridAdapter(ctx, stargazersListMain);

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

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
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepoStargazersViewModel;

/**
 * @author M M Arif
 */

public class RepoStargazersActivity extends BaseActivity {

	private TextView noDataStargazers;
	private View.OnClickListener onClickListener;
	private UserGridAdapter adapter;
	private GridView mGridView;
	private ProgressBar mProgressBar;

	private RepositoryContext repository;

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

		repository = RepositoryContext.fromIntent(getIntent());
		final String repoOwner = repository.getOwner();
		final String repoName = repository.getName();

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		toolbarTitle.setText(R.string.repoStargazersInMenu);

		fetchDataAsync(repoOwner, repoName);
	}

	private void fetchDataAsync(String repoOwner, String repoName) {

		RepoStargazersViewModel repoStargazersModel = new ViewModelProvider(this).get(RepoStargazersViewModel.class);

		repoStargazersModel.getRepoStargazers(repoOwner, repoName, ctx).observe(this, stargazersListMain -> {

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

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}

}

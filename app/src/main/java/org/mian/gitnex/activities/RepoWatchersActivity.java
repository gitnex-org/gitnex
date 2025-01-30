package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.databinding.ActivityRepoWatchersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepoWatchersViewModel;

/**
 * @author M M Arif
 */
public class RepoWatchersActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private UserGridAdapter adapter;
	private ActivityRepoWatchersBinding activityRepoWatchersBinding;
	private RepositoryContext repository;
	private RepoWatchersViewModel repoWatchersModel;
	private int page = 1;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityRepoWatchersBinding = ActivityRepoWatchersBinding.inflate(getLayoutInflater());
		setContentView(activityRepoWatchersBinding.getRoot());

		setSupportActionBar(activityRepoWatchersBinding.toolbar);

		resultLimit = Constants.getCurrentResultLimit(ctx);
		repoWatchersModel = new ViewModelProvider(this).get(RepoWatchersViewModel.class);

		repository = RepositoryContext.fromIntent(getIntent());
		final String repoOwner = repository.getOwner();
		final String repoName = repository.getName();

		initCloseListener();
		activityRepoWatchersBinding.close.setOnClickListener(onClickListener);

		activityRepoWatchersBinding.toolbarTitle.setText(R.string.repoWatchersInMenu);

		fetchDataAsync(repoOwner, repoName);
	}

	private void fetchDataAsync(String repoOwner, String repoName) {

		repoWatchersModel
				.getRepoWatchers(repoOwner, repoName, ctx, page, resultLimit)
				.observe(
						RepoWatchersActivity.this,
						mainList -> {
							adapter = new UserGridAdapter(ctx, mainList);
							adapter.setLoadMoreListener(
									new UserGridAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											repoWatchersModel.loadMore(
													repoOwner,
													repoName,
													ctx,
													page,
													resultLimit,
													adapter,
													activityRepoWatchersBinding);
											activityRepoWatchersBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											activityRepoWatchersBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
							activityRepoWatchersBinding.gridView.setLayoutManager(layoutManager);

							if (adapter.getItemCount() > 0) {
								activityRepoWatchersBinding.gridView.setAdapter(adapter);
								activityRepoWatchersBinding.noDataWatchers.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								activityRepoWatchersBinding.gridView.setAdapter(adapter);
								activityRepoWatchersBinding.noDataWatchers.setVisibility(
										View.VISIBLE);
							}

							activityRepoWatchersBinding.progressBar.setVisibility(View.GONE);
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

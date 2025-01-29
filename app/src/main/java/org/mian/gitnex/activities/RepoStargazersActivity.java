package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.databinding.ActivityRepoStargazersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepoStargazersViewModel;

/**
 * @author M M Arif
 */
public class RepoStargazersActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private UserGridAdapter adapter;
	private RepositoryContext repository;
	private ActivityRepoStargazersBinding activityRepoStargazersBinding;
	private RepoStargazersViewModel repoStargazersModel;
	private List<User> dataList;
	private int page = 1;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityRepoStargazersBinding = ActivityRepoStargazersBinding.inflate(getLayoutInflater());
		setContentView(activityRepoStargazersBinding.getRoot());

		setSupportActionBar(activityRepoStargazersBinding.toolbar);

		resultLimit = Constants.getCurrentResultLimit(ctx);
		repoStargazersModel = new ViewModelProvider(this).get(RepoStargazersViewModel.class);

		dataList = new ArrayList<>();

		repository = RepositoryContext.fromIntent(getIntent());
		final String repoOwner = repository.getOwner();
		final String repoName = repository.getName();

		initCloseListener();
		activityRepoStargazersBinding.close.setOnClickListener(onClickListener);

		activityRepoStargazersBinding.toolbarTitle.setText(R.string.repoStargazersInMenu);

		fetchDataAsync(repoOwner, repoName);
	}

	private void fetchDataAsync(String repoOwner, String repoName) {

		repoStargazersModel
				.getRepoStargazers(repoOwner, repoName, ctx, page, resultLimit)
				.observe(
						RepoStargazersActivity.this,
						mainList -> {
							adapter = new UserGridAdapter(ctx, mainList);

							adapter.setLoadMoreListener(
									new UserGridAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											repoStargazersModel.loadMore(
													repoOwner,
													repoName,
													ctx,
													page,
													resultLimit,
													adapter,
													activityRepoStargazersBinding);
											activityRepoStargazersBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											activityRepoStargazersBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
							activityRepoStargazersBinding.gridView.setLayoutManager(layoutManager);

							if (adapter.getItemCount() > 0) {
								activityRepoStargazersBinding.gridView.setAdapter(adapter);
								activityRepoStargazersBinding.noDataStargazers.setVisibility(
										View.GONE);
								dataList.addAll(mainList);
							} else {
								adapter.notifyDataChanged();
								activityRepoStargazersBinding.gridView.setAdapter(adapter);
								activityRepoStargazersBinding.noDataStargazers.setVisibility(
										View.VISIBLE);
							}

							activityRepoStargazersBinding.progressBar.setVisibility(View.GONE);
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

	private void filter(String text) {

		List<User> arr = new ArrayList<>();

		for (User d : dataList) {
			if (d == null || d.getLogin() == null || d.getFullName() == null) {
				continue;
			}
			if (d.getLogin().toLowerCase().contains(text)
					|| d.getFullName().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.setMoreDataAvailable(false);
		adapter.updateList(arr);
	}
}

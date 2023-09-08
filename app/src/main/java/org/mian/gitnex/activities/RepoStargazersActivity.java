package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.databinding.ActivityRepoStargazersBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepoStargazersViewModel;

/**
 * @author M M Arif
 */
public class RepoStargazersActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private UserGridAdapter adapter;
	private Boolean searchFilter = false;
	private RepositoryContext repository;
	private ActivityRepoStargazersBinding activityRepoStargazersBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityRepoStargazersBinding = ActivityRepoStargazersBinding.inflate(getLayoutInflater());
		setContentView(activityRepoStargazersBinding.getRoot());

		setSupportActionBar(activityRepoStargazersBinding.toolbar);

		repository = RepositoryContext.fromIntent(getIntent());
		final String repoOwner = repository.getOwner();
		final String repoName = repository.getName();

		initCloseListener();
		activityRepoStargazersBinding.close.setOnClickListener(onClickListener);

		activityRepoStargazersBinding.toolbarTitle.setText(R.string.repoStargazersInMenu);

		fetchDataAsync(repoOwner, repoName);
	}

	private void fetchDataAsync(String repoOwner, String repoName) {

		RepoStargazersViewModel repoStargazersModel =
				new ViewModelProvider(this).get(RepoStargazersViewModel.class);

		repoStargazersModel
				.getRepoStargazers(repoOwner, repoName, ctx)
				.observe(
						this,
						stargazersListMain -> {
							adapter = new UserGridAdapter(ctx, stargazersListMain);

							if (adapter.getCount() > 0) {

								activityRepoStargazersBinding.gridView.setAdapter(adapter);
								activityRepoStargazersBinding.noDataStargazers.setVisibility(
										View.GONE);
								searchFilter = true;
							} else {

								adapter.notifyDataSetChanged();
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

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		final MenuInflater inflater = getMenuInflater();

		new Handler(Looper.getMainLooper())
				.postDelayed(
						() -> {
							if (searchFilter) {

								boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

								inflater.inflate(R.menu.search_menu, menu);

								MenuItem searchItem = menu.findItem(R.id.action_search);
								SearchView searchView = (SearchView) searchItem.getActionView();
								searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

								if (!connToInternet) {
									return;
								}

								searchView.setOnQueryTextListener(
										new androidx.appcompat.widget.SearchView
												.OnQueryTextListener() {

											@Override
											public boolean onQueryTextSubmit(String query) {
												return true;
											}

											@Override
											public boolean onQueryTextChange(String newText) {

												adapter.getFilter().filter(newText);
												return false;
											}
										});
							}
						},
						500);

		return true;
	}
}

package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoForksAdapter;
import org.mian.gitnex.databinding.ActivityRepoForksBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepositoryForksViewModel;

/**
 * @author M M Arif
 */
public class RepoForksActivity extends BaseActivity {

	private ActivityRepoForksBinding activityRepoForksBinding;
	private int pageSize = 1;
	private RepoForksAdapter adapter;
	private RepositoryContext repository;

	@SuppressLint("DefaultLocale")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityRepoForksBinding = ActivityRepoForksBinding.inflate(getLayoutInflater());
		setContentView(activityRepoForksBinding.getRoot());

		Toolbar toolbar = activityRepoForksBinding.toolbar;
		setSupportActionBar(toolbar);

		repository = RepositoryContext.fromIntent(getIntent());

		activityRepoForksBinding.toolbarTitle.setText(
				ctx.getResources().getString(R.string.infoTabRepoForksCount));

		activityRepoForksBinding.close.setOnClickListener(v -> finish());

		activityRepoForksBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											pageSize = 1;
											activityRepoForksBinding.pullToRefresh.setRefreshing(
													false);
											fetchData();
											activityRepoForksBinding.progressBar.setVisibility(
													View.VISIBLE);
										},
										150));

		activityRepoForksBinding.recyclerView.setHasFixedSize(true);
		activityRepoForksBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		fetchData();
	}

	private void fetchData() {

		RepositoryForksViewModel repositoryForksViewModel =
				new ViewModelProvider(this).get(RepositoryForksViewModel.class);

		repositoryForksViewModel
				.getForksList(repository.getOwner(), repository.getName(), ctx)
				.observe(
						this,
						forksListMain -> {
							adapter = new RepoForksAdapter(ctx, forksListMain);
							adapter.setLoadMoreListener(
									new RepoForksAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											pageSize += 1;
											repositoryForksViewModel.loadMore(
													repository.getOwner(),
													repository.getName(),
													pageSize,
													ctx,
													adapter);
											activityRepoForksBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											activityRepoForksBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								activityRepoForksBinding.recyclerView.setAdapter(adapter);
								activityRepoForksBinding.noData.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								activityRepoForksBinding.recyclerView.setAdapter(adapter);
								activityRepoForksBinding.noData.setVisibility(View.VISIBLE);
							}

							activityRepoForksBinding.progressBar.setVisibility(View.GONE);
						});
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView =
				(androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(
				new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

					@Override
					public boolean onQueryTextSubmit(String query) {
						return false;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						if (activityRepoForksBinding.recyclerView.getAdapter() != null) {
							adapter.getFilter().filter(newText);
						}
						return false;
					}
				});
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}

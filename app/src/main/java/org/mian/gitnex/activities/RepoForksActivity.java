package org.mian.gitnex.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.ActivityRepoForksBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class RepoForksActivity extends BaseActivity {

	private ActivityRepoForksBinding binding;
	private RepositoriesViewModel viewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;
	private RepositoryContext repository;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityRepoForksBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		resultLimit = Constants.getCurrentResultLimit(ctx);
		repository = RepositoryContext.fromIntent(getIntent());
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		setupToolbar();
		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();
		setupSearch();

		refreshData();
	}

	private void setupToolbar() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnSearch.setOnClickListener(v -> binding.searchView.show());
	}

	private void setupSearch() {
		binding.searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
		binding.searchResultsRecycler.setAdapter(adapter);

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								filter(s.toString());
							}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState
							== com.google.android.material.search.SearchView.TransitionState
									.HIDDEN) {
						binding.searchView.setText("");
						filter("");
						binding.recyclerView.scrollToPosition(0);
					}
				});
	}

	private void setupRecyclerView() {
		adapter = new ReposListAdapter(new ArrayList<>(), ctx);
		LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (binding.searchView.isShowing()) return;
						viewModel.fetchRepos(
								ctx,
								"forks",
								repository.getOwner(),
								repository.getName(),
								page,
								resultLimit,
								null,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getRepos()
				.observe(
						this,
						list -> {
							adapter.updateList(list);
							updateUiState();
						});

		viewModel.getHasLoadedOnce().observe(this, hasLoaded -> updateUiState());

		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getError()
				.observe(
						this,
						error -> {
							if (error != null) Toasty.show(ctx, error);
						});
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
	}

	private void refreshData() {
		if (scrollListener != null) scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchRepos(
				ctx,
				"forks",
				repository.getOwner(),
				repository.getName(),
				1,
				resultLimit,
				null,
				true);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	private void filter(String text) {
		List<Repository> originalList = viewModel.getRepos().getValue();
		if (originalList == null) return;

		if (text == null || text.isEmpty()) {
			adapter.updateList(originalList);
			return;
		}

		List<Repository> filtered = new ArrayList<>();
		String query = text.toLowerCase().trim();

		for (Repository repo : originalList) {
			String fullName = repo.getFullName() != null ? repo.getFullName().toLowerCase() : "";
			String description =
					repo.getDescription() != null ? repo.getDescription().toLowerCase() : "";

			if (fullName.contains(query) || description.contains(query)) {
				filtered.add(repo);
			}
		}
		adapter.updateList(filtered);
	}
}

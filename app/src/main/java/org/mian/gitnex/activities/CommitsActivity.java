package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Commit;
import org.mian.gitnex.adapters.CommitsAdapter;
import org.mian.gitnex.databinding.ActivityCommitsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.CommitsViewModel;

/**
 * @author mmarif
 */
public class CommitsActivity extends BaseActivity {

	private ActivityCommitsBinding binding;
	private CommitsViewModel viewModel;
	private CommitsAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private RepositoryContext repository;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityCommitsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		repository = RepositoryContext.fromIntent(getIntent());
		resultLimit = Constants.getCurrentResultLimit(ctx);
		viewModel = new ViewModelProvider(this).get(CommitsViewModel.class);

		setupToolbar();
		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();
		setupSearch();

		refreshData();
	}

	private void setupToolbar() {
		binding.toolbarTitle.setText(repository.getBranchRef());
		binding.close.setOnClickListener(v -> finish());
		binding.actionSearchDock.setOnClickListener(v -> binding.searchView.show());
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
					if (newState.toString().equals("HIDDEN")) {
						binding.searchView.setText("");
						filter("");
						binding.recyclerView.scrollToPosition(0);
					}
				});

		binding.searchView
				.getEditText()
				.setOnEditorActionListener(
						(v, actionId, event) -> {
							binding.searchView.hide();
							return false;
						});
	}

	private void setupRecyclerView() {
		adapter = new CommitsAdapter(this, new ArrayList<>(), this::navigateToCommitDetail);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (binding.searchView.isShowing()) return;
						viewModel.fetchCommits(ctx, repository, page, resultLimit, false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void observeViewModel() {
		viewModel
				.getCommits()
				.observe(
						this,
						list -> {
							adapter.updateList(list);
							updateEmptyState(list.isEmpty());
							binding.pullToRefresh.setRefreshing(false);
						});

		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (loading) {
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
								if (adapter.getItemCount() == 0) {
									binding.expressiveLoader.setVisibility(View.VISIBLE);
								}
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
								List<Commit> current = viewModel.getCommits().getValue();
								updateEmptyState(current == null || current.isEmpty());
							}
						});

		viewModel
				.getError()
				.observe(
						this,
						msg -> {
							Toasty.show(ctx, msg);
							binding.pullToRefresh.setRefreshing(false);
							binding.expressiveLoader.setVisibility(View.GONE);
						});
	}

	private void updateEmptyState(boolean isEmpty) {
		boolean loading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
		if (!loading) {
			binding.layoutEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
		}
	}

	private void refreshData() {
		scrollListener.resetState();
		binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		viewModel.fetchCommits(ctx, repository, 1, resultLimit, true);
	}

	private void navigateToCommitDetail(Commit commit) {
		Intent intent = repository.getIntent(this, CommitDetailActivity.class);
		intent.putExtra("sha", commit.getSha());
		startActivity(intent);
	}

	private void filter(String text) {
		List<Commit> originalList = viewModel.getCommits().getValue();
		if (originalList == null) return;

		if (text == null || text.isEmpty()) {
			adapter.updateList(originalList);
			return;
		}

		List<Commit> filtered = new ArrayList<>();
		String query = text.toLowerCase().trim();

		for (Commit c : originalList) {
			String message =
					(c.getCommit() != null && c.getCommit().getMessage() != null)
							? c.getCommit().getMessage().toLowerCase()
							: "";
			String sha = (c.getSha() != null) ? c.getSha().toLowerCase() : "";

			if (message.contains(query) || sha.contains(query)) {
				filtered.add(c);
			}
		}
		adapter.updateList(filtered);
	}
}

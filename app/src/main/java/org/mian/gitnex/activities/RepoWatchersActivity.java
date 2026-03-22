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
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.databinding.ActivityRepoWatchersStargazersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.UserListViewModel;

/**
 * @author mmarif
 */
public class RepoWatchersActivity extends BaseActivity {

	private ActivityRepoWatchersStargazersBinding binding;
	private UsersAdapter adapter;
	private UserListViewModel viewModel;
	private RepositoryContext repository;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityRepoWatchersStargazersBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		repository = RepositoryContext.fromIntent(getIntent());
		resultLimit = Constants.getCurrentResultLimit(ctx);
		viewModel = new ViewModelProvider(this).get(UserListViewModel.class);

		setupUI();
		setupSearch();
		observeViewModel();

		refreshData();
	}

	private void setupUI() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnSearch.setOnClickListener(v -> binding.searchView.show());

		adapter = new UsersAdapter(ctx, new ArrayList<>());

		LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (binding.searchView.isShowing()) return;
						viewModel.fetchUsers(
								ctx,
								"watchers",
								repository.getOwner(),
								repository.getName(),
								null,
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getUsers()
				.observe(
						this,
						list -> {
							adapter.updateList(list);
							updateUiState();
						});

		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
						});
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchUsers(
				ctx,
				"watchers",
				repository.getOwner(),
				repository.getName(),
				null,
				1,
				resultLimit,
				true);
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
	}

	private void setupSearch() {

		binding.searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
		binding.searchResultsRecycler.setAdapter(adapter);

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								String query = s.toString().trim();
								filter(query);

								boolean hasResults = adapter.getItemCount() > 0;
								binding.layoutEmpty
										.getRoot()
										.setVisibility(hasResults ? View.GONE : View.VISIBLE);
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState
							== com.google.android.material.search.SearchView.TransitionState
									.HIDDEN) {
						List<User> originalList = viewModel.getUsers().getValue();
						if (originalList != null) {
							adapter.updateList(originalList);
						}
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

	private void filter(String query) {
		List<User> originalList = viewModel.getUsers().getValue();
		if (originalList == null) return;

		if (query.isEmpty()) {
			adapter.updateList(originalList);
			return;
		}

		List<User> filtered = new ArrayList<>();
		String lowerCaseQuery = query.toLowerCase();

		for (User user : originalList) {
			String login = user.getLogin() != null ? user.getLogin().toLowerCase() : "";
			String name = user.getFullName() != null ? user.getFullName().toLowerCase() : "";

			if (login.contains(lowerCaseQuery) || name.contains(lowerCaseQuery)) {
				filtered.add(user);
			}
		}

		adapter.updateList(filtered);
	}
}

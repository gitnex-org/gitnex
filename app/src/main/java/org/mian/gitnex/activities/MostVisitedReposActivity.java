package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.MostVisitedReposAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.databinding.ActivityMostVisitedReposBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;

/**
 * @author mmarif
 */
public class MostVisitedReposActivity extends BaseActivity {

	private ActivityMostVisitedReposBinding binding;
	private MostVisitedReposAdapter adapter;
	private RepositoriesApi repositoriesApi;
	private final List<Repository> mostVisitedReposList = new ArrayList<>();
	private int currentActiveAccountId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMostVisitedReposBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		currentActiveAccountId = TinyDB.getInstance(this).getInt("currentActiveAccountId");
		repositoriesApi = BaseApi.getInstance(this, RepositoriesApi.class);

		setupRecyclerView();
		setupDockActions();
		setupSearchOverlay();
		setupRefreshLayout();

		fetchDataAsync(currentActiveAccountId);
	}

	private void setupRecyclerView() {
		adapter = new MostVisitedReposAdapter(this, mostVisitedReposList);
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setAdapter(adapter);
	}

	private void setupDockActions() {
		binding.close.setOnClickListener(v -> finish());

		binding.actionSearchDock.setOnClickListener(v -> binding.searchView.show());

		binding.deleteAll.setOnClickListener(v -> handleResetAction());
	}

	private void setupSearchOverlay() {
		binding.searchResultsRecycler.setAdapter(adapter);

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								filter(s.toString().trim());
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
						adapter.updateList(new ArrayList<>(mostVisitedReposList));
						binding.recyclerView.scrollToPosition(0);
						updateUiVisibility(false);
					}
				});
	}

	private void setupRefreshLayout() {
		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(() -> fetchDataAsync(currentActiveAccountId), 250));
	}

	private void fetchDataAsync(int accountId) {
		if (repositoriesApi == null) return;

		binding.expressiveLoader.setVisibility(View.VISIBLE);
		binding.layoutEmpty.getRoot().setVisibility(View.GONE);

		repositoriesApi
				.fetchAllMostVisited(accountId)
				.observe(
						this,
						repos -> {
							binding.pullToRefresh.setRefreshing(false);
							binding.expressiveLoader.setVisibility(View.GONE);

							mostVisitedReposList.clear();
							if (repos != null && !repos.isEmpty()) {
								mostVisitedReposList.addAll(repos);
								adapter.updateList(new ArrayList<>(mostVisitedReposList));
								updateUiVisibility(false);
							} else {
								adapter.updateList(new ArrayList<>());
								updateUiVisibility(true);
							}
						});
	}

	private void updateUiVisibility(boolean isEmpty) {
		binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
		binding.layoutEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
	}

	private void filter(String text) {
		List<Repository> filteredList = new ArrayList<>();
		String query = text.toLowerCase();

		for (Repository repo : mostVisitedReposList) {
			if (repo.getRepositoryOwner().toLowerCase().contains(query)
					|| repo.getRepositoryName().toLowerCase().contains(query)) {
				filteredList.add(repo);
			}
		}

		adapter.updateList(filteredList);
		updateUiVisibility(filteredList.isEmpty());
	}

	private void handleResetAction() {
		if (mostVisitedReposList.isEmpty()) {
			Toasty.show(this, getString(R.string.noDataFound));
			return;
		}

		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.reset)
				.setMessage(R.string.resetCounterAllDialogMessage)
				.setPositiveButton(R.string.reset, (dialog, which) -> resetAllCounters())
				.setNeutralButton(R.string.cancelButton, null)
				.show();
	}

	private void resetAllCounters() {
		if (repositoriesApi != null) {
			repositoriesApi.resetAllRepositoryMostVisited(currentActiveAccountId);

			mostVisitedReposList.clear();
			adapter.updateList(new ArrayList<>());
			updateUiVisibility(true);

			Toasty.show(this, getString(R.string.resetMostReposCounter));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		fetchDataAsync(currentActiveAccountId);
	}
}

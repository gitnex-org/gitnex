package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.search.SearchView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreatePullRequestActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.PullRequestsAdapter;
import org.mian.gitnex.databinding.FragmentPullRequestsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.RepositoryMenuItemModel;
import org.mian.gitnex.viewmodels.PullRequestsViewModel;

/**
 * @author mmarif
 */
public class PullRequestsFragment extends Fragment implements RepoDetailActivity.RepoHubProvider {

	private FragmentPullRequestsBinding binding;
	private PullRequestsViewModel viewModel;
	private PullRequestsAdapter adapter;
	private RepositoryContext repository;
	private int resultLimit;
	private EndlessRecyclerViewScrollListener scrollListener;
	private boolean isFirstLoad = true;

	public static PullRequestsFragment newInstance(RepositoryContext repository) {
		PullRequestsFragment f = new PullRequestsFragment();
		f.setArguments(repository.getBundle());
		return f;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);
		setupSearch();
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentPullRequestsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(PullRequestsViewModel.class);

		repository = RepositoryContext.fromBundle(requireArguments());
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupAdapters();
		setupListeners();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public List<RepositoryMenuItemModel> getRepoHubItems() {
		List<RepositoryMenuItemModel> items = new ArrayList<>();

		items.add(
				new RepositoryMenuItemModel(
						"PR_SEARCH",
						R.string.search,
						R.drawable.ic_search,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		boolean isCurrentlyOpen = repository.getPrState() == RepositoryContext.State.OPEN;

		items.add(
				new RepositoryMenuItemModel(
						"PR_STATE_TOGGLE",
						isCurrentlyOpen ? R.string.isClosed : R.string.isOpen,
						R.drawable.ic_filter,
						isCurrentlyOpen
								? R.attr.colorTertiaryContainer
								: R.attr.colorSurfaceVariant,
						isCurrentlyOpen
								? R.attr.colorOnTertiaryContainer
								: R.attr.colorOnSurfaceVariant));

		if (repository.getRepository().isHasPullRequests()
				&& !repository.getRepository().isArchived()) {
			items.add(
					new RepositoryMenuItemModel(
							"PR_CREATE_NEW",
							R.string.create_pr,
							R.drawable.ic_add,
							R.attr.colorPrimaryContainer,
							R.attr.colorOnPrimaryContainer));
		}

		return items;
	}

	@Override
	public void onHubActionSelected(String actionId) {
		switch (actionId) {
			case "PR_SEARCH":
				binding.searchView.show();
				break;

			case "PR_STATE_TOGGLE":
				RepositoryContext.State newState =
						(repository.getPrState() == RepositoryContext.State.OPEN)
								? RepositoryContext.State.CLOSED
								: RepositoryContext.State.OPEN;
				repository.setPrState(newState);
				refreshData();
				break;

			case "PR_CREATE_NEW":
				startActivity(
						repository.getIntent(requireContext(), CreatePullRequestActivity.class));
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad) {
			lazyLoad();
		}
	}

	private void lazyLoad() {
		isFirstLoad = false;
		refreshData();
	}

	private void setupAdapters() {
		adapter = new PullRequestsAdapter(requireContext(), new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setAdapter(adapter);

		binding.searchResultsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.searchResultsRecycler.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchPullRequests(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								repository.getPrState().toString(),
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupSearch() {
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
								filterLocal(s.toString());
							}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState == SearchView.TransitionState.HIDDEN) {
						resetToFullList();
					}
				});
	}

	private void filterLocal(String text) {
		List<PullRequest> fullList = viewModel.getPrList().getValue();
		if (fullList == null) return;

		if (text.isEmpty()) {
			adapter.updateList(fullList);
			return;
		}

		List<PullRequest> filtered = new ArrayList<>();
		String query = text.toLowerCase().trim();

		for (PullRequest pr : fullList) {
			if (pr.getTitle().toLowerCase().contains(query)
					|| String.valueOf(pr.getNumber()).contains(query)) {
				filtered.add(pr);
			}
		}
		adapter.updateList(filtered);
	}

	private void resetToFullList() {
		List<PullRequest> fullList = viewModel.getPrList().getValue();
		if (fullList != null) {
			adapter.updateList(fullList);
		}
	}

	private void observeViewModel() {
		viewModel
				.getPrList()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							updateUiState();
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (loading && adapter.getItemCount() == 0) {
								binding.expressiveLoader.setVisibility(View.VISIBLE);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
								binding.pullToRefresh.setRefreshing(false);
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						err -> {
							if (err != null) {
								Toasty.show(requireContext(), err);
								binding.expressiveLoader.setVisibility(View.GONE);
								binding.pullToRefresh.setRefreshing(false);
							}
						});
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean hasLoaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		boolean isLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());

		if (!isLoading && hasLoaded && isEmpty) {
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.VISIBLE);
		}
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();

		binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		binding.expressiveLoader.setVisibility(View.VISIBLE);

		viewModel.fetchPullRequests(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				repository.getPrState().toString(),
				1,
				resultLimit,
				true);
	}

	private void setupListeners() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

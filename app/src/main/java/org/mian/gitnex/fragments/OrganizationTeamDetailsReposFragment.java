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
import java.util.ArrayList;
import org.gitnex.tea4j.v2.models.Team;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationTeamDetailActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class OrganizationTeamDetailsReposFragment extends Fragment
		implements OrganizationTeamDetailActivity.OrgActionInterface {

	private FragmentRepositoriesBinding binding;
	private RepositoriesViewModel viewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private Team team;
	private String orgName;
	private int resultLimit;
	private boolean isSearching = false;
	private boolean isFirstLoad = true;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.recyclerView, binding.pullToRefresh, null);

		getChildFragmentManager()
				.setFragmentResultListener(
						"repo_changed",
						this,
						(requestKey, bundle) -> {
							if (bundle.getBoolean("shouldRefresh")) {
								refreshData();
							}
						});
	}

	public static OrganizationTeamDetailsReposFragment newInstance(Team team, String orgName) {
		OrganizationTeamDetailsReposFragment fragment = new OrganizationTeamDetailsReposFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("team", team);
		bundle.putString("orgName", orgName);
		bundle.putBoolean("showRepo", !team.isIncludesAllRepositories());
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentRepositoriesBinding.inflate(inflater, container, false);
		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		if (getArguments() != null) {
			team = (Team) getArguments().getSerializable("team");
			orgName = getArguments().getString("orgName");
		}

		setupRecyclerView();
		setupSwipeRefresh();
		setupSearch();
		observeViewModel();

		return binding.getRoot();
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
		if (!hidden && isFirstLoad) lazyLoad();
	}

	private void lazyLoad() {
		isFirstLoad = false;
		refreshData();
	}

	private void setupRecyclerView() {
		adapter = new ReposListAdapter(new ArrayList<>(), requireContext());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		binding.searchResultsRecycler.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (!isSearching && team != null) {
							fetchData(page, false);
						}
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
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								String query = s.toString();
								isSearching = !query.isEmpty();
								adapter.getFilter().filter(query);
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
						isSearching = false;
						adapter.getFilter().filter("");
					}
				});
	}

	private void observeViewModel() {
		viewModel
				.getRepos()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							binding.pullToRefresh.setRefreshing(false);
							updateUiState();
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
						});
	}

	private void fetchData(int page, boolean isRefresh) {
		viewModel.fetchRepos(
				requireContext(),
				"team",
				String.valueOf(team.getId()),
				null,
				page,
				resultLimit,
				null,
				isRefresh);
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		if (team != null) fetchData(1, true);
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	@Override
	public void onSearchTriggered() {
		binding.searchView.show();
	}

	@Override
	public void onAddRequested() {
		BottomSheetAddTeamRepo sheet = BottomSheetAddTeamRepo.newInstance(team.getId(), orgName);
		sheet.show(getChildFragmentManager(), "AddTeamRepoSheet");
	}

	@Override
	public boolean canAdd() {
		return !isSearching;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

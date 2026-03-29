package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateRepoActivity;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class OrganizationRepositoriesFragment extends Fragment
		implements OrganizationDetailActivity.OrgActionInterface {

	private FragmentRepositoriesBinding binding;
	private RepositoriesViewModel viewModel;
	private OrganizationsViewModel orgViewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private String orgName;
	private int resultLimit;
	private boolean isSearching = false;
	private boolean isFirstLoad = true;

	public static OrganizationRepositoriesFragment newInstance(String orgName) {
		OrganizationRepositoriesFragment fragment = new OrganizationRepositoriesFragment();
		Bundle args = new Bundle();
		args.putString("orgName", orgName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		int paddingTopPx = getResources().getDimensionPixelSize(R.dimen.dimen56dp);

		ViewCompat.setOnApplyWindowInsetsListener(
				view,
				(v, windowInsets) -> {
					Insets systemBars =
							windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
					binding.recyclerView.setPadding(
							binding.recyclerView.getPaddingLeft(),
							paddingTopPx,
							binding.recyclerView.getPaddingRight(),
							binding.recyclerView.getPaddingBottom());

					return windowInsets;
				});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			orgName = getArguments().getString("orgName");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentRepositoriesBinding.inflate(inflater, container, false);

		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(requireActivity()).get(RepositoriesViewModel.class);
		orgViewModel = new ViewModelProvider(requireActivity()).get(OrganizationsViewModel.class);

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
		adapter.isUserOrg = true;

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (!isSearching) {
							viewModel.fetchRepos(
									requireContext(),
									"org",
									"",
									orgName,
									page,
									resultLimit,
									null,
									false);
						}
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupSearch() {
		binding.searchResultsRecycler.setAdapter(adapter);

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								String query = s.toString();
								isSearching = !query.isEmpty();
								adapter.getFilter()
										.filter(
												query,
												count1 -> {
													if (isSearching
															&& adapter.getItemCount() == 0) {
														binding.layoutEmpty
																.getRoot()
																.setVisibility(View.VISIBLE);
													} else {
														updateUiState(
																Boolean.TRUE.equals(
																		viewModel
																				.getIsLoading()
																				.getValue()));
													}
												});
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
						updateUiState(false);
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
							updateUiState(Boolean.TRUE.equals(viewModel.getIsLoading().getValue()));
						});

		viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateUiState);

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});
	}

	private void updateUiState(boolean isLoading) {
		int count = (adapter != null) ? adapter.getItemCount() : 0;
		boolean hasData = count > 0;

		boolean loadedOnce = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);

		if (isLoading) {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		} else {
			if (!hasData && loadedOnce) {
				binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
				binding.pullToRefresh.setVisibility(View.GONE);
			} else {
				binding.layoutEmpty.getRoot().setVisibility(View.GONE);
				binding.pullToRefresh.setVisibility(View.VISIBLE);
			}
		}
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchRepos(requireContext(), "org", "", orgName, 1, resultLimit, null, true);
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
		Intent intent = new Intent(requireContext(), CreateRepoActivity.class);
		intent.putExtra("orgName", orgName);
		startActivity(intent);
	}

	@Override
	public boolean canAdd() {
		OrganizationPermissions perms = orgViewModel.getPermissions().getValue();
		return perms != null && (perms.isIsOwner() || perms.isCanCreateRepository());
	}
}

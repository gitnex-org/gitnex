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
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationMembersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;
import org.mian.gitnex.viewmodels.UserListViewModel;

/**
 * @author mmarif
 */
public class OrganizationMembersFragment extends Fragment
		implements OrganizationDetailActivity.OrgActionInterface {

	private FragmentOrganizationMembersBinding binding;
	private UserListViewModel viewModel;
	private OrganizationsViewModel orgViewModel;
	private UsersAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private static final String BUNDLE_ORG_NAME = "org_name";
	private String orgName;
	private int resultLimit;
	private boolean isSearching = false;
	private boolean isFirstLoad = true;

	public OrganizationMembersFragment() {}

	public static OrganizationMembersFragment newInstance(String orgName) {
		OrganizationMembersFragment fragment = new OrganizationMembersFragment();
		Bundle args = new Bundle();
		args.putString(BUNDLE_ORG_NAME, orgName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			orgName = getArguments().getString(BUNDLE_ORG_NAME);
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentOrganizationMembersBinding.inflate(inflater, container, false);

		viewModel = new ViewModelProvider(this).get(UserListViewModel.class);
		orgViewModel = new ViewModelProvider(requireActivity()).get(OrganizationsViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupRecyclerView();
		setupSearch();
		setupSwipeRefresh();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);

		adapter = new UsersAdapter(requireContext(), new ArrayList<>());
		binding.recyclerView.setAdapter(adapter);
		binding.searchResultsRecycler.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (!isSearching) {
							viewModel.fetchUsers(
									requireContext(),
									"org_members",
									orgName,
									null,
									null,
									page,
									resultLimit,
									false);
						}
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getUsers()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							updateUiVisibility(
									Boolean.TRUE.equals(viewModel.getIsLoading().getValue()));
						});

		viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateUiVisibility);

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});
	}

	private void updateUiVisibility(boolean isLoading) {
		boolean hasData = adapter != null && adapter.getItemCount() > 0;

		Boolean loadedOnceValue = viewModel.getHasLoadedOnce().getValue();
		boolean hasLoadedOnce = (loadedOnceValue != null && loadedOnceValue);

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);

		boolean showEmpty = !isLoading && hasLoadedOnce && !hasData;
		binding.layoutEmpty.getRoot().setVisibility(showEmpty ? View.VISIBLE : View.GONE);

		binding.pullToRefresh.setVisibility(showEmpty ? View.GONE : View.VISIBLE);

		if (showEmpty) {
			binding.layoutEmpty.getRoot().bringToFront();
		}
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
								adapter.getFilter()
										.filter(
												query,
												count1 -> {
													binding.layoutEmpty
															.getRoot()
															.setVisibility(
																	isSearching
																					&& adapter
																									.getItemCount()
																							== 0
																			? View.VISIBLE
																			: View.GONE);
												});
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchUsers(
				requireContext(), "org_members", orgName, null, null, 1, resultLimit, true);
	}

	private void lazyLoad() {
		isFirstLoad = false;
		if (viewModel.getUsers().getValue() == null || viewModel.getUsers().getValue().isEmpty()) {
			refreshData();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && (isFirstLoad)) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad) lazyLoad();
	}

	@Override
	public void onSearchTriggered() {
		binding.searchView.show();
	}

	@Override
	public void onAddRequested() {}

	@Override
	public boolean canAdd() {
		OrganizationPermissions perms = orgViewModel.getPermissions().getValue();
		return perms != null && perms.isIsOwner();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

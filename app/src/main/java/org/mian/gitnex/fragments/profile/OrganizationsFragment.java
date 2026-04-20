package org.mian.gitnex.fragments.profile;

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
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class OrganizationsFragment extends Fragment
		implements ProfileActivity.ProfileActionInterface {

	private static final String BUNDLE_USERNAME = "username";
	private FragmentOrganizationsBinding binding;
	private OrganizationsViewModel viewModel;
	private OrganizationsListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private String username;
	private int resultLimit;
	private boolean isSearching = false;
	private boolean isFirstLoad = true;
	private boolean isViewReady = false;
	private SearchView searchView;

	public static OrganizationsFragment newInstance(String username) {
		OrganizationsFragment fragment = new OrganizationsFragment();
		Bundle args = new Bundle();
		args.putString(BUNDLE_USERNAME, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) username = getArguments().getString(BUNDLE_USERNAME);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.recyclerView, binding.pullToRefresh, null);
		isViewReady = true;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentOrganizationsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(OrganizationsViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupUI();
		observeViewModel();
		setupSearch();

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad && isViewReady) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad && isViewReady) {
			lazyLoad();
		}
	}

	private void lazyLoad() {
		isFirstLoad = false;
		refreshData();
	}

	private void setupUI() {
		adapter = new OrganizationsListAdapter(requireContext(), new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchUserOrgs(
								requireContext(), username, page + 1, resultLimit, false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void observeViewModel() {

		viewModel
				.getOrgs()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							binding.pullToRefresh.setRefreshing(false);
							boolean isLoading =
									Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
							binding.layoutEmpty
									.getRoot()
									.setVisibility(
											!isLoading && list.isEmpty()
													? View.VISIBLE
													: View.GONE);
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;

							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);

							if (loading) {
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								boolean isEmpty = adapter.getItemCount() == 0;
								binding.layoutEmpty
										.getRoot()
										.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) Toasty.show(requireContext(), msg);
						});
	}

	private void refreshData() {
		if (scrollListener != null) scrollListener.resetState();
		if (viewModel == null) return;
		viewModel.fetchUserOrgs(requireContext(), username, 1, resultLimit, true);
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
	}

	private void setupSearch() {
		searchView = binding.searchView;
		RecyclerView searchRecycler = binding.searchResultsRecycler;

		searchRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
		searchRecycler.setAdapter(adapter);

		searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								String query = s.toString();
								isSearching = !query.isEmpty();

								adapter.getFilter()
										.filter(
												query,
												count1 -> {
													boolean isEmpty = adapter.getItemCount() == 0;
													binding.layoutEmpty
															.getRoot()
															.setVisibility(
																	isSearching && isEmpty
																			? View.VISIBLE
																			: View.GONE);
												});
							}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState
							== com.google.android.material.search.SearchView.TransitionState
									.HIDDEN) {
						isSearching = false;
						adapter.getFilter().filter("");
						updateUiState();
					}
				});
	}

	@Override
	public void onSearchTriggered() {
		if (searchView != null) {
			searchView.show();
		}
	}
}

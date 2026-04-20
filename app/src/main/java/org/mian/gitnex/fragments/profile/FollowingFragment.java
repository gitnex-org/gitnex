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
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.databinding.FragmentProfileFollowersFollowingBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.UserListViewModel;

/**
 * @author mmarif
 */
public class FollowingFragment extends Fragment implements ProfileActivity.ProfileActionInterface {

	private static final String BUNDLE_USERNAME = "username";
	private FragmentProfileFollowersFollowingBinding binding;
	private UserListViewModel viewModel;
	private UsersAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;
	private String username;
	private boolean isSearching = false;
	private boolean isFirstLoad = true;
	private boolean isViewReady = false;
	private SearchView searchView;

	public FollowingFragment() {}

	public static FollowingFragment newInstance(String username) {
		FollowingFragment fragment = new FollowingFragment();
		Bundle args = new Bundle();
		args.putString(BUNDLE_USERNAME, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			username = getArguments().getString(BUNDLE_USERNAME);
		}
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
		binding = FragmentProfileFollowersFollowingBinding.inflate(inflater, container, false);

		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(this).get(UserListViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupSearch();
		observeViewModel();

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
		if (viewModel.getUsers().getValue() == null || viewModel.getUsers().getValue().isEmpty()) {
			refreshData();
		}
	}

	private void setupRecyclerView() {
		adapter = new UsersAdapter(requireContext(), new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchUsers(
								requireContext(),
								"following",
								username,
								null,
								null,
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
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
		boolean hasData = adapter.getItemCount() > 0;
		boolean hasLoadedOnce = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);
		binding.layoutEmpty
				.getRoot()
				.setVisibility(!isLoading && !hasData && hasLoadedOnce ? View.VISIBLE : View.GONE);
		binding.pullToRefresh.setVisibility(
				!hasData && !isLoading && hasLoadedOnce ? View.GONE : View.VISIBLE);
	}

	private void refreshData() {
		if (scrollListener == null || viewModel == null) {
			return;
		}
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchUsers(
				requireContext(), "following", username, null, null, 1, resultLimit, true);
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
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								String query = s.toString();
								isSearching = !query.isEmpty();

								adapter.getFilter()
										.filter(
												query,
												count1 -> {
													if (isSearching) {
														boolean noResults =
																adapter.getItemCount() == 0;
														binding.layoutEmpty
																.getRoot()
																.setVisibility(
																		noResults
																				? View.VISIBLE
																				: View.GONE);
													}
												});
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

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
						updateUiVisibility(false);
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

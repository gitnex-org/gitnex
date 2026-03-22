package org.mian.gitnex.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.databinding.FragmentProfileFollowersFollowingBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.UserListViewModel;

/**
 * @author mmarif
 */
public class FollowingFragment extends Fragment {

	private static final String BUNDLE_USERNAME = "username";
	private FragmentProfileFollowersFollowingBinding binding;
	private UserListViewModel viewModel;
	private UsersAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;
	private String username;

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
		setupMenu();
		observeViewModel();

		if (viewModel.getUsers().getValue() == null || viewModel.getUsers().getValue().isEmpty()) {
			refreshData();
		}

		return binding.getRoot();
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
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchUsers(
				requireContext(), "following", username, null, null, 1, resultLimit, true);
	}

	private void setupMenu() {
		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menu.clear();
								menuInflater.inflate(R.menu.search_menu, menu);

								MenuItem searchItem = menu.findItem(R.id.action_search);
								androidx.appcompat.widget.SearchView searchView =
										(androidx.appcompat.widget.SearchView)
												searchItem.getActionView();

								if (searchView != null) {
									searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
									searchView.setOnQueryTextListener(
											new androidx.appcompat.widget.SearchView
													.OnQueryTextListener() {
												@Override
												public boolean onQueryTextSubmit(String query) {
													return false;
												}

												@Override
												public boolean onQueryTextChange(String newText) {
													filter(newText);
													return false;
												}
											});
								}
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);
	}

	private void filter(String text) {
		List<User> originalList = viewModel.getUsers().getValue();
		if (originalList == null) return;
		if (text.isEmpty()) {
			adapter.updateList(originalList);
			return;
		}

		String query = text.toLowerCase().trim();
		List<User> filtered = new ArrayList<>();
		for (User u : originalList) {
			if ((u.getLogin() != null && u.getLogin().toLowerCase().contains(query))
					|| (u.getFullName() != null && u.getFullName().toLowerCase().contains(query))) {
				filtered.add(u);
			}
		}
		adapter.updateList(filtered);
	}
}

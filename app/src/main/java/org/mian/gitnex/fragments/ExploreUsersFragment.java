package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.databinding.FragmentExploreUsersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.UserListViewModel;

/**
 * @author mmarif
 */
public class ExploreUsersFragment extends Fragment {

	private FragmentExploreUsersBinding binding;
	private UserListViewModel viewModel;
	private UsersAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;
	private String currentQuery = "";

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentExploreUsersBinding.inflate(inflater, container, false);
		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(this).get(UserListViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupMenu();
		observeViewModel();

		refreshData("");

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter = new UsersAdapter(requireContext(), new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerViewExploreUsers.setLayoutManager(layoutManager);
		binding.recyclerViewExploreUsers.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchUsers(
								requireContext(),
								"explore",
								null,
								null,
								currentQuery,
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerViewExploreUsers.addOnScrollListener(scrollListener);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData(currentQuery);
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

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);
		boolean hasLoadedOnce = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		binding.layoutEmpty
				.getRoot()
				.setVisibility(!isLoading && !hasData && hasLoadedOnce ? View.VISIBLE : View.GONE);
		binding.pullToRefresh.setVisibility(
				!hasData && !isLoading && hasLoadedOnce ? View.GONE : View.VISIBLE);
	}

	private void refreshData(String query) {
		currentQuery = query;
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchUsers(requireContext(), "explore", null, null, query, 1, resultLimit, true);
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
													refreshData(query);
													searchItem.collapseActionView();
													return true;
												}

												@Override
												public boolean onQueryTextChange(String newText) {
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
}

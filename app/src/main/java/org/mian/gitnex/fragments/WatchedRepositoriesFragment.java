package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateRepoActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class WatchedRepositoriesFragment extends Fragment {

	private FragmentRepositoriesBinding binding;
	private RepositoriesViewModel viewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;
	private boolean isSearching = false;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentRepositoriesBinding.inflate(inflater, container, false);
		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupMenu();
		observeViewModel();

		refreshData();
		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter = new ReposListAdapter(new ArrayList<>(), requireContext());
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
									"watched",
									"",
									null,
									page,
									resultLimit,
									null,
									false);
						}
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		binding.addNewRepo.setOnClickListener(
				v -> startActivity(new Intent(getContext(), CreateRepoActivity.class)));
	}

	private void setupMenu() {
		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.search_menu, menu);
								MenuItem searchItem = menu.findItem(R.id.action_search);
								SearchView searchView = (SearchView) searchItem.getActionView();
								if (searchView != null) {
									searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
									searchView.setOnQueryTextListener(
											new SearchView.OnQueryTextListener() {
												@Override
												public boolean onQueryTextSubmit(String query) {
													return false;
												}

												@Override
												public boolean onQueryTextChange(String newText) {
													isSearching = !newText.isEmpty();
													adapter.getFilter().filter(newText);
													return true;
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

	private void observeViewModel() {
		viewModel
				.getRepos()
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
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
						});

		viewModel.getHasLoadedOnce().observe(getViewLifecycleOwner(), hasLoaded -> updateUiState());
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
	}

	private void refreshData() {
		if (scrollListener != null) scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchRepos(requireContext(), "watched", "", null, 1, resultLimit, null, true);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (MainActivity.reloadRepos) {
			refreshData();
			MainActivity.reloadRepos = false;
		}
	}
}

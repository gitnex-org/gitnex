package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.databinding.FragmentSearchIssuesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author mmarif
 */
public class ExploreIssuesFragment extends Fragment {

	private IssuesViewModel issuesViewModel;
	private FragmentSearchIssuesBinding viewBinding;
	private IssuesAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private String currentQuery = "";
	private int resultLimit;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewBinding = FragmentSearchIssuesBinding.inflate(inflater, container, false);

		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupMenu();
		setupRecyclerView();
		observeViewModel();

		fetchDataAsync("");

		return viewBinding.getRoot();
	}

	private void setupRecyclerView() {
		adapter = new IssuesAdapter(requireContext(), new ArrayList<>(), "explore");

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
		viewBinding.recyclerViewSearchIssues.setHasFixedSize(true);
		viewBinding.recyclerViewSearchIssues.setLayoutManager(layoutManager);
		viewBinding.recyclerViewSearchIssues.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						issuesViewModel.fetchIssues(
								requireContext(),
								currentQuery,
								"open",
								null,
								null,
								null,
								null,
								page,
								resultLimit,
								false);
					}
				};
		viewBinding.recyclerViewSearchIssues.addOnScrollListener(scrollListener);

		viewBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											viewBinding.pullToRefresh.setRefreshing(false);
											fetchDataAsync(currentQuery);
										},
										50));
	}

	private void observeViewModel() {
		issuesViewModel
				.getIssues()
				.observe(
						getViewLifecycleOwner(),
						issues -> {
							adapter.updateList(issues);
							updateUiState();
						});

		issuesViewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (loading && adapter.getItemCount() == 0) {
								viewBinding.progressBar.setVisibility(View.VISIBLE);
							} else {
								viewBinding.progressBar.setVisibility(View.GONE);
							}
							updateUiState();
						});

		issuesViewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) {
								// Toasty.show(getContext(), error);
								updateUiState();
							}
						});
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean hasLoaded = Boolean.TRUE.equals(issuesViewModel.getHasLoadedOnce().getValue());
		boolean isLoading = Boolean.TRUE.equals(issuesViewModel.getIsLoading().getValue());

		if (hasLoaded && isEmpty && !isLoading) {
			viewBinding.noData.setVisibility(View.VISIBLE);
			viewBinding.recyclerViewSearchIssues.setVisibility(View.GONE);
		} else {
			viewBinding.noData.setVisibility(View.GONE);
			viewBinding.recyclerViewSearchIssues.setVisibility(View.VISIBLE);
		}
	}

	private void fetchDataAsync(String query) {
		this.currentQuery = query;

		viewBinding.noData.setVisibility(View.GONE);
		scrollListener.resetState();
		issuesViewModel.resetPagination();

		issuesViewModel.fetchIssues(
				requireContext(), query, "open", null, null, null, null, 1, resultLimit, true);
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
													fetchDataAsync(query);
													searchView.setQuery(null, false);
													searchItem.collapseActionView();
													return false;
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

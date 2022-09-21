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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ExploreIssuesAdapter;
import org.mian.gitnex.databinding.FragmentSearchIssuesBinding;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author M M Arif
 */
public class ExploreIssuesFragment extends Fragment {

	private IssuesViewModel issuesViewModel;
	private FragmentSearchIssuesBinding viewBinding;
	private ExploreIssuesAdapter adapter;
	private int page = 1;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentSearchIssuesBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);

		viewBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											viewBinding.pullToRefresh.setRefreshing(false);
											fetchDataAsync("");
											viewBinding.progressBar.setVisibility(View.VISIBLE);
										},
										50));

		viewBinding.recyclerViewSearchIssues.setHasFixedSize(true);
		viewBinding.recyclerViewSearchIssues.setLayoutManager(
				new LinearLayoutManager(requireActivity()));

		fetchDataAsync("");

		return viewBinding.getRoot();
	}

	private void fetchDataAsync(String searchKeyword) {

		issuesViewModel
				.getIssuesList(searchKeyword, "issues", null, "open", null, getContext())
				.observe(
						getViewLifecycleOwner(),
						issuesListMain -> {
							adapter = new ExploreIssuesAdapter(issuesListMain, getContext());
							adapter.setLoadMoreListener(
									new ExploreIssuesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											issuesViewModel.loadMoreIssues(
													searchKeyword,
													"issues",
													null,
													"open",
													page,
													null,
													getContext(),
													adapter);
											viewBinding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											viewBinding.progressBar.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								viewBinding.recyclerViewSearchIssues.setAdapter(adapter);
								viewBinding.noData.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								viewBinding.recyclerViewSearchIssues.setAdapter(adapter);
								viewBinding.noData.setVisibility(View.VISIBLE);
							}

							viewBinding.progressBar.setVisibility(View.GONE);
						});
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		menu.clear();
		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView =
				(androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(
				new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

					@Override
					public boolean onQueryTextSubmit(String query) {
						viewBinding.progressBar.setVisibility(View.VISIBLE);
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

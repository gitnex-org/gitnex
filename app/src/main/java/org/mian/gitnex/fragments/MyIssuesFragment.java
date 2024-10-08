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
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ExploreIssuesAdapter;
import org.mian.gitnex.databinding.FragmentIssuesBinding;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author M M Arif
 */
public class MyIssuesFragment extends Fragment {

	public String state = "open";
	public boolean assignedToMe = false;
	private IssuesViewModel issuesViewModel;
	private FragmentIssuesBinding fragmentIssuesBinding;
	private ExploreIssuesAdapter adapter;
	private int page = 1;
	private Menu menu;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentIssuesBinding = FragmentIssuesBinding.inflate(inflater, container, false);

		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu1, @NonNull MenuInflater menuInflater) {

								menu = menu1;
								menuInflater.inflate(R.menu.search_menu, menu1);
								menuInflater.inflate(R.menu.filter_menu, menu1);

								MenuItem searchItem = menu1.findItem(R.id.action_search);
								androidx.appcompat.widget.SearchView searchView =
										(androidx.appcompat.widget.SearchView)
												searchItem.getActionView();
								assert searchView != null;
								searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

								searchView.setOnQueryTextListener(
										new androidx.appcompat.widget.SearchView
												.OnQueryTextListener() {

											@Override
											public boolean onQueryTextSubmit(String query) {
												fetchDataAsync(query, state, assignedToMe);
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

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);

		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);

		fragmentIssuesBinding.recyclerView.setHasFixedSize(true);
		fragmentIssuesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		fragmentIssuesBinding.createNewIssue.setVisibility(View.GONE);

		((MainActivity) requireActivity())
				.setFragmentRefreshListenerMyIssues(
						myIssues -> {
							state = myIssues;
							if (state.equals("closed")) {
								menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
							} else {
								menu.getItem(1).setIcon(R.drawable.ic_filter);
							}

							assignedToMe = state.equals("assignedToMe");

							fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
							fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);

							fetchDataAsync(null, state, assignedToMe);
						});

		fragmentIssuesBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											fragmentIssuesBinding.pullToRefresh.setRefreshing(
													false);
											issuesViewModel.loadIssuesList(
													null,
													"issues",
													true,
													state,
													assignedToMe,
													getContext());
											fragmentIssuesBinding.progressBar.setVisibility(
													View.VISIBLE);
										},
										50));

		fetchDataAsync(null, state, assignedToMe);

		return fragmentIssuesBinding.getRoot();
	}

	private void fetchDataAsync(String query, String state, boolean assignedToMe) {

		issuesViewModel
				.getIssuesList(query, "issues", true, state, assignedToMe, getContext())
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
													query,
													"issues",
													true,
													state,
													page,
													assignedToMe,
													getContext(),
													adapter);
											fragmentIssuesBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											fragmentIssuesBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								fragmentIssuesBinding.recyclerView.setAdapter(adapter);
								fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								fragmentIssuesBinding.recyclerView.setAdapter(adapter);
								fragmentIssuesBinding.noDataIssues.setVisibility(View.VISIBLE);
							}

							fragmentIssuesBinding.progressBar.setVisibility(View.GONE);
						});
	}
}

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
import androidx.annotation.Nullable;
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

	private FragmentIssuesBinding fragmentIssuesBinding;
	private IssuesViewModel issuesViewModel;
	private ExploreIssuesAdapter adapter;
	private Menu menu;
	private String state = "open";
	private boolean assignedToMe = false;
	private boolean createdByMe = true;
	private int page = 1;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		fragmentIssuesBinding = FragmentIssuesBinding.inflate(inflater, container, false);
		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);

		fragmentIssuesBinding.recyclerView.setHasFixedSize(true);
		fragmentIssuesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		fragmentIssuesBinding.createNewIssue.setVisibility(View.GONE);

		setupMenu();

		((MainActivity) requireActivity())
				.setFragmentRefreshListenerMyIssues(
						myIssues -> {
							updateFilterState(myIssues);
							fetchDataAsync(null);
						});

		fragmentIssuesBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											fragmentIssuesBinding.pullToRefresh.setRefreshing(
													false);
											fetchDataAsync(null);
										},
										50));

		fetchDataAsync(null);

		return fragmentIssuesBinding.getRoot();
	}

	private void setupMenu() {
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

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

								if (menuItem.getItemId() == R.id.filter) {

									String currentFilter =
											state
													+ "_"
													+ (assignedToMe
															? "assignedToMe"
															: "created_by_me");
									BottomSheetMyIssuesFilterFragment bottomSheet =
											BottomSheetMyIssuesFilterFragment.newInstance(
													currentFilter);
									bottomSheet.show(getParentFragmentManager(), "myIssuesFilter");

									return true;
								}
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);
	}

	public void updateFilterState(String filter) {

		String[] parts = filter.split("_", 2);
		String stateValue = parts[0];
		String filterValue = parts[1];

		state = stateValue;
		menu.getItem(1)
				.setIcon(
						state.equals("closed")
								? R.drawable.ic_filter_closed
								: R.drawable.ic_filter);

		if (filterValue.equals("created_by_me")) {
			createdByMe = true;
			assignedToMe = false;
		} else if (filterValue.equals("assignedToMe")) {
			createdByMe = false;
			assignedToMe = true;
		}

		fetchDataAsync(null);
	}

	public String getCurrentFilter() {
		return state + "_" + (assignedToMe ? "assignedToMe" : "created_by_me");
	}

	private void fetchDataAsync(String query) {

		fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
		fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);

		issuesViewModel
				.getIssuesList(query, "issues", createdByMe, state, assignedToMe, getContext())
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
													createdByMe,
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

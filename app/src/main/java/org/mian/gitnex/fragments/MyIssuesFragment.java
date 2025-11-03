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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ExploreIssuesAdapter;
import org.mian.gitnex.databinding.BottomSheetMyIssuesFilterBinding;
import org.mian.gitnex.databinding.FragmentIssuesBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author mmarif
 */
public class MyIssuesFragment extends Fragment {

	private FragmentIssuesBinding binding;
	private IssuesViewModel issuesViewModel;
	private ExploreIssuesAdapter adapter;
	private TinyDB tinyDB;
	private String state = "open";
	private boolean assignedToMe = false;
	private boolean createdByMe = true;
	private int page = 1;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			Bundle savedInstanceState) {
		binding = FragmentIssuesBinding.inflate(inflater, container, false);
		tinyDB = TinyDB.getInstance(requireContext());
		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);

		String savedFilter = tinyDB.getString("myIssuesFilter", "open_created_by_me");
		updateFilterState(savedFilter);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.createNewIssue.setVisibility(View.GONE);

		setupMenu();

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											binding.pullToRefresh.setRefreshing(false);
											fetchDataAsync(null);
										},
										50));

		fetchDataAsync(null);

		return binding.getRoot();
	}

	private void setupMenu() {
		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.search_menu, menu);
								menuInflater.inflate(R.menu.generic_nav_dotted_menu, menu);

								MenuItem searchItem = menu.findItem(R.id.action_search);
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
								if (menuItem.getItemId() == R.id.genericMenu) {
									new FilterBottomSheetDialogFragment()
											.show(getChildFragmentManager(), "MyIssuesFilter");
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
		state = parts[0];
		String filterValue = parts.length > 1 ? parts[1] : "created_by_me";

		createdByMe = filterValue.equals("created_by_me");
		assignedToMe = filterValue.equals("assigned_to_me");

		tinyDB.putString("myIssuesFilter", getCurrentFilter());
		page = 1;
	}

	public String getCurrentFilter() {
		return state + "_" + (assignedToMe ? "assigned_to_me" : "created_by_me");
	}

	private void fetchDataAsync(String query) {
		binding.progressBar.setVisibility(View.VISIBLE);
		binding.noDataIssues.setVisibility(View.GONE);

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
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {
											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								binding.recyclerView.setAdapter(adapter);
								binding.noDataIssues.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								binding.recyclerView.setAdapter(adapter);
								binding.noDataIssues.setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	public static class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

		private String selectedState = "open";
		private String selectedFilter = "created_by_me";

		@Override
		public View onCreateView(
				@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			BottomSheetMyIssuesFilterBinding binding =
					BottomSheetMyIssuesFilterBinding.inflate(inflater, container, false);

			// Initialize based on parent fragment's current filter
			MyIssuesFragment parent = (MyIssuesFragment) requireParentFragment();
			String currentFilter = parent.getCurrentFilter();
			String[] parts = currentFilter.split("_", 2);
			selectedState = parts[0];
			selectedFilter = parts.length > 1 ? parts[1] : "created_by_me";

			binding.chipOpen.setChecked(selectedState.equals("open"));
			binding.chipClosed.setChecked(selectedState.equals("closed"));
			binding.chipCreatedByMe.setChecked(selectedFilter.equals("created_by_me"));
			binding.chipAssignedToMe.setChecked(selectedFilter.equals("assigned_to_me"));

			binding.stateChipGroup.setOnCheckedStateChangeListener(
					(group, checkedIds) -> {
						if (!checkedIds.isEmpty()) {
							int checkedId = checkedIds.get(0);
							selectedState =
									checkedId == binding.chipOpen.getId() ? "open" : "closed";
							applyFilter(parent);
						}
					});

			binding.filterChipGroup.setOnCheckedStateChangeListener(
					(group, checkedIds) -> {
						if (!checkedIds.isEmpty()) {
							int checkedId = checkedIds.get(0);
							selectedFilter =
									checkedId == binding.chipCreatedByMe.getId()
											? "created_by_me"
											: "assigned_to_me";
							applyFilter(parent);
						}
					});

			return binding.getRoot();
		}

		private void applyFilter(MyIssuesFragment parent) {
			String result = selectedState + "_" + selectedFilter;
			parent.updateFilterState(result);
			parent.fetchDataAsync(null);
			dismiss();
		}
	}
}

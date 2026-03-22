package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.adapters.ExploreIssuesAdapter;
import org.mian.gitnex.databinding.ActivityMyIssuesBinding;
import org.mian.gitnex.databinding.BottomSheetMyIssuesFilterBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author mmarif
 */
public class MyIssuesActivity extends BaseActivity {

	private ActivityMyIssuesBinding binding;
	private IssuesViewModel issuesViewModel;
	private ExploreIssuesAdapter adapter;
	private TinyDB tinyDB;
	private String state = "open";
	private boolean assignedToMe = false;
	private boolean createdByMe = true;
	private int page = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMyIssuesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		tinyDB = TinyDB.getInstance(this);
		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);

		String savedFilter = tinyDB.getString("myIssuesFilter", "open_created_by_me");
		updateFilterState(savedFilter);

		setupUI();
		setupSearch();
		fetchDataAsync(null);
	}

	private void setupUI() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnSearch.setOnClickListener(v -> binding.searchView.show());
		binding.btnMore.setOnClickListener(
				v ->
						new FilterBottomSheetDialogFragment()
								.show(getSupportFragmentManager(), "MyIssuesFilter"));

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
	}

	private void setupSearch() {
		binding.searchView
				.getEditText()
				.setOnEditorActionListener(
						(v, actionId, event) -> {
							String query = binding.searchView.getText().toString().trim();
							fetchDataAsync(query);
							binding.searchView.hide();
							return true;
						});
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
		binding.expressiveLoader.setVisibility(View.VISIBLE);
		binding.layoutEmpty.getRoot().setVisibility(View.GONE);

		issuesViewModel
				.getIssuesList(query, "issues", createdByMe, state, assignedToMe, this)
				.observe(
						this,
						issuesListMain -> {
							adapter = new ExploreIssuesAdapter(issuesListMain, this);
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
													MyIssuesActivity.this,
													adapter);
											binding.expressiveLoader.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {
											binding.expressiveLoader.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								binding.recyclerView.setAdapter(adapter);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								binding.recyclerView.setAdapter(adapter);
								binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
							}
							binding.expressiveLoader.setVisibility(View.GONE);
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
			MyIssuesActivity activity = (MyIssuesActivity) requireActivity();

			String currentFilter = activity.getCurrentFilter();
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
							selectedState =
									checkedIds.get(0) == binding.chipOpen.getId()
											? "open"
											: "closed";
							applyFilter(activity);
						}
					});

			binding.filterChipGroup.setOnCheckedStateChangeListener(
					(group, checkedIds) -> {
						if (!checkedIds.isEmpty()) {
							selectedFilter =
									checkedIds.get(0) == binding.chipCreatedByMe.getId()
											? "created_by_me"
											: "assigned_to_me";
							applyFilter(activity);
						}
					});

			return binding.getRoot();
		}

		private void applyFilter(MyIssuesActivity activity) {
			activity.updateFilterState(selectedState + "_" + selectedFilter);
			activity.fetchDataAsync(null);
			dismiss();
		}
	}
}

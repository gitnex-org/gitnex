package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.search.SearchView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.databinding.ActivityMyIssuesBinding;
import org.mian.gitnex.databinding.BottomSheetMyIssuesFilterBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author mmarif
 */
public class MyIssuesActivity extends BaseActivity {

	private ActivityMyIssuesBinding binding;
	private IssuesViewModel issuesViewModel;
	private IssuesAdapter adapter;
	private TinyDB tinyDB;
	private EndlessRecyclerViewScrollListener scrollListener;
	private String state = "open";
	private boolean assignedToMe = false;
	private boolean createdByMe = true;
	private String currentQuery = null;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMyIssuesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		tinyDB = TinyDB.getInstance(this);
		resultLimit = Constants.getCurrentResultLimit(this);
		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);

		String savedFilter = tinyDB.getString("myIssuesFilter", "open_created_by_me");
		updateFilterState(savedFilter);

		setupUI();
		setupSearch();
		observeViewModel();

		fetchData(null);
	}

	private void setupUI() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnSearch.setOnClickListener(v -> binding.searchView.show());
		binding.btnMore.setOnClickListener(
				v ->
						new FilterBottomSheetDialogFragment()
								.show(getSupportFragmentManager(), "MyIssuesFilter"));

		adapter = new IssuesAdapter(this, new ArrayList<>(), "my_issues");
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						issuesViewModel.fetchIssues(
								MyIssuesActivity.this,
								currentQuery,
								state,
								null,
								null,
								assignedToMe,
								createdByMe,
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() -> {
										binding.pullToRefresh.setRefreshing(false);
										fetchData(currentQuery);
									},
									50);
				});
	}

	private void observeViewModel() {
		issuesViewModel
				.getIssues()
				.observe(
						this,
						issues -> {
							adapter.updateList(issues);
							updateUiState();
						});

		issuesViewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (loading && adapter.getItemCount() == 0) {
								binding.expressiveLoader.setVisibility(View.VISIBLE);
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
							}
							updateUiState();
						});

		issuesViewModel
				.getError()
				.observe(
						this,
						error -> {
							if (error != null) {
								Toasty.show(this, error);
								updateUiState();
							}
						});
	}

	private void setupSearch() {

		binding.searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
		binding.searchResultsRecycler.setAdapter(adapter);

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								filter(s.toString());
							}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchView
				.getEditText()
				.setOnEditorActionListener(
						(v, actionId, event) -> {
							currentQuery = binding.searchView.getText().toString().trim();
							fetchData(currentQuery);
							binding.searchView.hide();
							return true;
						});

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState == SearchView.TransitionState.HIDDEN) {
						List<Issue> originalList = issuesViewModel.getIssues().getValue();
						if (originalList != null) {
							adapter.updateList(originalList);
						}
						updateUiState();
					}
				});
	}

	private void filter(String text) {
		List<Issue> originalList = issuesViewModel.getIssues().getValue();
		if (originalList == null) return;

		if (text.isEmpty()) {
			adapter.updateList(originalList);
			updateUiState();
			return;
		}

		List<Issue> filtered = new ArrayList<>();
		String query = text.toLowerCase().trim();

		for (Issue issue : originalList) {
			String title = (issue.getTitle() != null) ? issue.getTitle().toLowerCase() : "";
			String body = (issue.getBody() != null) ? issue.getBody().toLowerCase() : "";
			String number = String.valueOf(issue.getId());

			if (title.contains(query) || body.contains(query) || number.contains(query)) {
				filtered.add(issue);
			}
		}

		adapter.updateList(filtered);

		boolean isEmpty = filtered.isEmpty();
		binding.layoutEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
		binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
	}

	public void updateFilterState(String filter) {
		String[] parts = filter.split("_", 2);
		state = parts[0];
		String filterValue = parts.length > 1 ? parts[1] : "created_by_me";

		createdByMe = filterValue.equals("created_by_me");
		assignedToMe = filterValue.equals("assigned_to_me");

		tinyDB.putString("myIssuesFilter", getCurrentFilter());
	}

	public String getCurrentFilter() {
		return state + "_" + (assignedToMe ? "assigned_to_me" : "created_by_me");
	}

	private void fetchData(String query) {
		currentQuery = (query != null && !query.isEmpty()) ? query : null;

		binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		scrollListener.resetState();
		issuesViewModel.resetPagination();

		issuesViewModel.fetchIssues(
				this,
				currentQuery,
				state,
				null,
				null,
				assignedToMe,
				createdByMe,
				1,
				resultLimit,
				true);
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean hasLoaded = Boolean.TRUE.equals(issuesViewModel.getHasLoadedOnce().getValue());
		boolean isLoading = Boolean.TRUE.equals(issuesViewModel.getIsLoading().getValue());

		if (hasLoaded && isEmpty && !isLoading) {
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.VISIBLE);
		}
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
			activity.fetchData(activity.currentQuery);
			dismiss();
		}
	}
}

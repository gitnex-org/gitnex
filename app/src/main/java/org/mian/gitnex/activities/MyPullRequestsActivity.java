package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import org.mian.gitnex.adapters.MyPullRequestsAdapter;
import org.mian.gitnex.databinding.ActivityRepositoriesBinding;
import org.mian.gitnex.databinding.BottomsheetMyPrsFilterBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.PullRequestsViewModel;

/**
 * @author mmarif
 */
public class MyPullRequestsActivity extends BaseActivity {

	private ActivityRepositoriesBinding binding;
	private PullRequestsViewModel viewModel;
	private MyPullRequestsAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;

	private String searchQuery = "";
	private String state = "open";
	private String activeFilter = "assigned";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityRepositoriesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		resultLimit = Constants.getCurrentResultLimit(this);
		viewModel = new ViewModelProvider(this).get(PullRequestsViewModel.class);

		setupUI();
		observeViewModel();
		refreshData();
	}

	private void setupUI() {
		ViewGroup dockParent = (ViewGroup) binding.btnMore.getParent();
		dockParent.removeView(binding.btnMore);
		dockParent.removeView(binding.btnNewRepository);

		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnSearch.setOnClickListener(v -> showFilterBottomSheet());

		adapter = new MyPullRequestsAdapter(this, new ArrayList<>());
		adapter.setOnPrClickListener(
				issue -> {
					Intent intent = new Intent(this, PullRequestDetailActivity.class);
					intent.putExtra("source", "my_prs_activity");
					intent.putExtra("prIssue", issue);
					startActivity(intent);
				});

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						performSearch(page, false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	private void observeViewModel() {
		viewModel
				.getMyPrList()
				.observe(
						this,
						list -> {
							adapter.updateList(list);
							updateUiState();
						});

		viewModel.getHasLoadedOnce().observe(this, hasLoaded -> updateUiState());

		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
							if (!loading) binding.pullToRefresh.setRefreshing(false);
						});

		viewModel
				.getError()
				.observe(
						this,
						error -> {
							if (error != null) Toasty.show(this, error);
						});
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
	}

	private void refreshData() {
		if (scrollListener != null) scrollListener.resetState();
		viewModel.resetPagination();
		performSearch(1, true);
	}

	private void performSearch(int page, boolean isRefresh) {
		Boolean assigned = "assigned".equals(activeFilter) ? true : null;
		Boolean created = "created".equals(activeFilter) ? true : null;
		Boolean mentioned = "mentioned".equals(activeFilter) ? true : null;
		Boolean reviewRequested = "review_requested".equals(activeFilter) ? true : null;
		Boolean reviewed = "reviewed".equals(activeFilter) ? true : null;

		viewModel.searchPullRequests(
				this,
				state,
				null,
				null,
				searchQuery,
				"pulls",
				null,
				null,
				assigned,
				created,
				mentioned,
				reviewRequested,
				reviewed,
				null,
				null,
				page,
				resultLimit,
				isRefresh);
	}

	private void showFilterBottomSheet() {
		FilterBottomSheet sheet = FilterBottomSheet.newInstance(searchQuery, state, activeFilter);
		sheet.setFilterListener(
				(query, newState, newActiveFilter) -> {
					searchQuery = query;
					state = newState;
					activeFilter = newActiveFilter;
					refreshData();
				});
		sheet.show(getSupportFragmentManager(), "FILTER_SHEET");
	}

	public static class FilterBottomSheet extends BottomSheetDialogFragment {

		private BottomsheetMyPrsFilterBinding binding;
		private FilterListener listener;

		private String searchQuery;
		private String state;
		private String activeFilter;

		public interface FilterListener {
			void onFiltersApplied(String query, String state, String activeFilter);
		}

		public static FilterBottomSheet newInstance(
				String query, String state, String activeFilter) {
			FilterBottomSheet fragment = new FilterBottomSheet();
			Bundle args = new Bundle();
			args.putString("query", query);
			args.putString("state", state);
			args.putString("activeFilter", activeFilter);
			fragment.setArguments(args);
			return fragment;
		}

		public void setFilterListener(FilterListener listener) {
			this.listener = listener;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if (getArguments() != null) {
				searchQuery = getArguments().getString("query", "");
				state = getArguments().getString("state", "open");
				activeFilter = getArguments().getString("activeFilter", "assigned");
			}
		}

		@Override
		public View onCreateView(
				@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			binding = BottomsheetMyPrsFilterBinding.inflate(inflater, container, false);
			return binding.getRoot();
		}

		@Override
		public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			binding.searchQueryEdit.setText(searchQuery);

			if ("closed".equals(state)) {
				binding.stateClosed.setChecked(true);
			} else {
				binding.stateOpen.setChecked(true);
			}

			switch (activeFilter) {
				case "created" -> binding.createdChip.setChecked(true);
				case "mentioned" -> binding.mentionedChip.setChecked(true);
				case "review_requested" -> binding.reviewRequestedChip.setChecked(true);
				case "reviewed" -> binding.reviewedChip.setChecked(true);
				default -> binding.assignedChip.setChecked(true);
			}

			binding.btnClear.setOnClickListener(v -> resetFilters());
			binding.btnApply.setOnClickListener(v -> applyFilters());
		}

		private void resetFilters() {
			binding.searchQueryEdit.setText("");
			binding.stateOpen.setChecked(true);
			binding.assignedChip.setChecked(true);
		}

		private void applyFilters() {
			if (listener != null) {
				String query =
						binding.searchQueryEdit.getText() != null
								? binding.searchQueryEdit.getText().toString().trim()
								: "";
				String newState = binding.stateClosed.isChecked() ? "closed" : "open";

				String newActiveFilter = "assigned";
				if (binding.createdChip.isChecked()) newActiveFilter = "created";
				else if (binding.mentionedChip.isChecked()) newActiveFilter = "mentioned";
				else if (binding.reviewRequestedChip.isChecked())
					newActiveFilter = "review_requested";
				else if (binding.reviewedChip.isChecked()) newActiveFilter = "reviewed";

				listener.onFiltersApplied(query, newState, newActiveFilter);
			}
			dismiss();
		}

		@Override
		public void onStart() {
			super.onStart();
			if (getDialog() instanceof BottomSheetDialog) {
				AppUtil.applySheetStyle((BottomSheetDialog) getDialog(), true);
			}
		}

		@Override
		public void onDestroyView() {
			super.onDestroyView();
			binding = null;
		}
	}

	@Override
	protected void onGlobalRefresh() {
		refreshData();
	}
}

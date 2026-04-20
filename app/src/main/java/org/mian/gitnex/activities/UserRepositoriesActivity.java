package org.mian.gitnex.activities;

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
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.ActivityRepositoriesBinding;
import org.mian.gitnex.databinding.BottomsheetUserRepoSearchFilterBinding;
import org.mian.gitnex.fragments.BottomSheetCreateRepo;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class UserRepositoriesActivity extends BaseActivity {

	private ActivityRepositoriesBinding binding;
	private RepositoriesViewModel viewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;
	private long uid;

	private String searchQuery = "";
	private boolean includeTopic = false;
	private boolean includeDescription = false;
	private boolean includeTemplate = false;
	private boolean onlyArchived = false;
	private String currentSort = "updated";
	private String currentMode = "source";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityRepositoriesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		uid = getIntent().getLongExtra("uid", -1);
		if (uid == -1) {
			Toasty.show(this, R.string.invalid_user);
			finish();
			return;
		}

		resultLimit = Constants.getCurrentResultLimit(this);
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		setupUI();
		observeViewModel();
		refreshData();
	}

	private void setupUI() {
		ViewGroup dockParent = (ViewGroup) binding.btnMore.getParent();
		dockParent.removeView(binding.btnMore);

		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnSearch.setOnClickListener(v -> showFilterBottomSheet());
		binding.btnNewRepository.setOnClickListener(
				v ->
						BottomSheetCreateRepo.newInstance(null, false)
								.show(getSupportFragmentManager(), "CREATE_REPO"));

		adapter = new ReposListAdapter(new ArrayList<>(), this);
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
				.getSearchResults()
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
		viewModel.searchRepos(
				this,
				searchQuery,
				includeTopic ? Boolean.TRUE : Boolean.FALSE,
				includeDescription ? Boolean.TRUE : Boolean.FALSE,
				uid,
				uid,
				null,
				null,
				true,
				null,
				includeTemplate ? Boolean.TRUE : Boolean.FALSE,
				onlyArchived ? Boolean.TRUE : Boolean.FALSE,
				currentMode,
				null,
				currentSort,
				"desc",
				page,
				resultLimit,
				isRefresh);
	}

	private void showFilterBottomSheet() {
		FilterBottomSheet sheet =
				FilterBottomSheet.newInstance(
						searchQuery,
						includeTopic,
						includeDescription,
						includeTemplate,
						onlyArchived,
						currentSort,
						currentMode);
		sheet.setFilterListener(
				(query, topic, desc, template, archived, sort, mode) -> {
					searchQuery = query;
					includeTopic = topic;
					includeDescription = desc;
					includeTemplate = template;
					onlyArchived = archived;
					currentSort = sort;
					currentMode = mode;
					refreshData();
				});
		sheet.show(getSupportFragmentManager(), "FILTER_SHEET");
	}

	public static class FilterBottomSheet extends BottomSheetDialogFragment {

		private BottomsheetUserRepoSearchFilterBinding binding;
		private FilterListener listener;

		private String searchQuery;
		private boolean includeTopic;
		private boolean includeDescription;
		private boolean includeTemplate;
		private boolean onlyArchived;
		private String currentSort;
		private String currentMode;

		public interface FilterListener {
			void onFiltersApplied(
					String query,
					boolean topic,
					boolean desc,
					boolean template,
					boolean archived,
					String sort,
					String mode);
		}

		public static FilterBottomSheet newInstance(
				String query,
				boolean topic,
				boolean desc,
				boolean template,
				boolean archived,
				String sort,
				String mode) {
			FilterBottomSheet fragment = new FilterBottomSheet();
			Bundle args = new Bundle();
			args.putString("query", query);
			args.putBoolean("topic", topic);
			args.putBoolean("desc", desc);
			args.putBoolean("template", template);
			args.putBoolean("archived", archived);
			args.putString("sort", sort);
			args.putString("mode", mode);
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
				includeTopic = getArguments().getBoolean("topic", false);
				includeDescription = getArguments().getBoolean("desc", false);
				includeTemplate = getArguments().getBoolean("template", false);
				onlyArchived = getArguments().getBoolean("archived", false);
				currentSort = getArguments().getString("sort", "updated");
				currentMode = getArguments().getString("mode", "source");
			}
		}

		@Override
		public View onCreateView(
				@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			binding = BottomsheetUserRepoSearchFilterBinding.inflate(inflater, container, false);
			return binding.getRoot();
		}

		@Override
		public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			binding.searchQueryEdit.setText(searchQuery);
			binding.includeTopicChip.setChecked(includeTopic);
			binding.includeDescChip.setChecked(includeDescription);
			binding.includeTemplateChip.setChecked(includeTemplate);
			binding.onlyArchivedChip.setChecked(onlyArchived);

			setupSortSelection(currentSort);
			setupModeSelection(currentMode);

			binding.btnClear.setOnClickListener(v -> resetFilters());
			binding.btnApply.setOnClickListener(v -> applyFilters());
		}

		private void setupSortSelection(String sort) {
			if ("created".equals(sort)) binding.sortCreated.setChecked(true);
			else if ("stars".equals(sort)) binding.sortStars.setChecked(true);
			else if ("forks".equals(sort)) binding.sortForks.setChecked(true);
			else if ("size".equals(sort)) binding.sortSize.setChecked(true);
			else binding.sortUpdated.setChecked(true);
		}

		private void setupModeSelection(String mode) {
			if ("fork".equals(mode)) binding.modeFork.setChecked(true);
			else if ("mirror".equals(mode)) binding.modeMirror.setChecked(true);
			else if ("collaborative".equals(mode)) binding.modeCollaborative.setChecked(true);
			else binding.modeSource.setChecked(true);
		}

		private void resetFilters() {
			binding.searchQueryEdit.setText("");
			binding.includeTopicChip.setChecked(false);
			binding.includeDescChip.setChecked(false);
			binding.includeTemplateChip.setChecked(false);
			binding.onlyArchivedChip.setChecked(false);
			binding.sortUpdated.setChecked(true);
			binding.modeSource.setChecked(true);
		}

		private void applyFilters() {
			if (listener != null) {
				String query =
						binding.searchQueryEdit.getText() != null
								? binding.searchQueryEdit.getText().toString().trim()
								: "";
				String sort = getSelectedSort();
				String mode = getSelectedMode();

				listener.onFiltersApplied(
						query,
						binding.includeTopicChip.isChecked(),
						binding.includeDescChip.isChecked(),
						binding.includeTemplateChip.isChecked(),
						binding.onlyArchivedChip.isChecked(),
						sort,
						mode);
			}
			dismiss();
		}

		private String getSelectedSort() {
			int id = binding.sortChipGroup.getCheckedChipId();
			if (id == R.id.sortCreated) return "created";
			if (id == R.id.sortStars) return "stars";
			if (id == R.id.sortForks) return "forks";
			if (id == R.id.sortSize) return "size";
			return "updated";
		}

		private String getSelectedMode() {
			int id = binding.modeChipGroup.getCheckedChipId();
			if (id == R.id.modeFork) return "fork";
			if (id == R.id.modeMirror) return "mirror";
			if (id == R.id.modeCollaborative) return "collaborative";
			return "source";
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
}

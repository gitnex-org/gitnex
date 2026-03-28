package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ExploreActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.BottomsheetExploreReposSearchBinding;
import org.mian.gitnex.databinding.FragmentExploreRepoBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class ExploreRepositoriesFragment extends Fragment
		implements ExploreActivity.ExploreActionInterface {

	private FragmentExploreRepoBinding viewBinding;
	private RepositoriesViewModel viewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;
	private String searchQuery = "";
	private boolean includeTopic = false;
	private boolean includeDescription = false;
	private boolean includeTemplate = false;
	private boolean onlyArchived = false;
	private String currentSort = "updated";
	private boolean isFirstLoad = true;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewBinding = FragmentExploreRepoBinding.inflate(inflater, container, false);
		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();

		return viewBinding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ViewCompat.setOnApplyWindowInsetsListener(
				view,
				(v, windowInsets) -> {
					Insets systemBars =
							windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
					viewBinding.recyclerView.setPadding(0, systemBars.top, 0, 0);
					return windowInsets;
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad) {
			lazyLoad();
		}
	}

	private void lazyLoad() {
		isFirstLoad = false;
		refreshData();
	}

	@Override
	public void onSearchTriggered() {
		showSearchBottomSheet();
	}

	private void setupRecyclerView() {
		adapter = new ReposListAdapter(new ArrayList<>(), requireContext());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		viewBinding.recyclerView.setLayoutManager(layoutManager);
		viewBinding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.searchExploreRepos(
								requireContext(),
								searchQuery,
								includeTopic,
								includeDescription,
								includeTemplate,
								onlyArchived,
								page,
								resultLimit,
								currentSort,
								false);
					}
				};
		viewBinding.recyclerView.addOnScrollListener(scrollListener);
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
							if (loading && adapter.getItemCount() == 0) {
								viewBinding.expressiveLoader.setVisibility(View.VISIBLE);
								viewBinding.recyclerView.setVisibility(View.GONE);
								viewBinding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								viewBinding.expressiveLoader.setVisibility(View.GONE);
								viewBinding.recyclerView.setVisibility(View.VISIBLE);
								updateUiState();
							}
						});

		viewModel.getHasLoadedOnce().observe(getViewLifecycleOwner(), hasLoaded -> updateUiState());

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		viewBinding
				.layoutEmpty
				.getRoot()
				.setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
	}

	private void refreshData() {
		if (scrollListener != null) scrollListener.resetState();
		viewModel.resetPagination();

		adapter.updateList(new ArrayList<>());
		viewBinding.recyclerView.setVisibility(View.GONE);
		viewBinding.expressiveLoader.setVisibility(View.VISIBLE);

		viewModel.searchExploreRepos(
				requireContext(),
				searchQuery,
				includeTopic,
				includeDescription,
				includeTemplate,
				onlyArchived,
				1,
				resultLimit,
				currentSort,
				true);
	}

	private void setupSwipeRefresh() {
		viewBinding.pullToRefresh.setOnRefreshListener(
				() -> {
					viewBinding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	private void showSearchBottomSheet() {
		SearchBottomSheet sheet =
				SearchBottomSheet.newInstance(
						searchQuery,
						includeTopic,
						includeDescription,
						includeTemplate,
						onlyArchived,
						currentSort,
						new SearchBottomSheet.SearchCallback() {
							@Override
							public void onSearchApplied(
									String q,
									boolean t,
									boolean d,
									boolean temp,
									boolean arch,
									String sort) {
								searchQuery = q;
								includeTopic = t;
								includeDescription = d;
								includeTemplate = temp;
								onlyArchived = arch;
								currentSort = sort;
								refreshData();
							}

							@Override
							public void onReset() {
								searchQuery = "";
								includeTopic = false;
								includeDescription = false;
								includeTemplate = false;
								onlyArchived = false;
								currentSort = "updated";
								refreshData();
							}
						});
		sheet.show(getChildFragmentManager(), "ExploreSearchSheet");
	}

	public static class SearchBottomSheet extends BottomSheetDialogFragment {
		private SearchCallback callback;
		private BottomsheetExploreReposSearchBinding sheet;

		@NonNull @Override
		public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
			BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
			AppUtil.applySheetStyle(dialog, true);
			return dialog;
		}

		public static SearchBottomSheet newInstance(
				String query,
				boolean topic,
				boolean desc,
				boolean template,
				boolean archived,
				String sort,
				SearchCallback cb) {
			SearchBottomSheet fragment = new SearchBottomSheet();
			Bundle args = new Bundle();
			args.putString("q", query);
			args.putBoolean("t", topic);
			args.putBoolean("d", desc);
			args.putBoolean("temp", template);
			args.putBoolean("arch", archived);
			args.putString("sort", sort);
			fragment.setArguments(args);
			fragment.callback = cb;
			return fragment;
		}

		@Nullable @Override
		public View onCreateView(
				@NonNull LayoutInflater inflater,
				@Nullable ViewGroup container,
				@Nullable Bundle savedInstanceState) {
			sheet = BottomsheetExploreReposSearchBinding.inflate(inflater, container, false);

			Bundle args = getArguments();
			if (args != null) {
				sheet.searchQueryEdit.setText(args.getString("q"));
				sheet.includeTopicChip.setChecked(args.getBoolean("t"));
				sheet.includeDescChip.setChecked(args.getBoolean("d"));
				sheet.includeTemplateChip.setChecked(args.getBoolean("temp"));
				sheet.onlyArchivedChip.setChecked(args.getBoolean("arch"));

				String sort = args.getString("sort", "updated");
				setupSortSelection(sort);
			}

			setupValidation();

			sheet.btnApply.setOnClickListener(
					v -> {
						if (callback != null) {
							callback.onSearchApplied(
									Objects.requireNonNull(sheet.searchQueryEdit.getText())
											.toString()
											.trim(),
									sheet.includeTopicChip.isChecked(),
									sheet.includeDescChip.isChecked(),
									sheet.includeTemplateChip.isChecked(),
									sheet.onlyArchivedChip.isChecked(),
									getSelectedSort());
						}
						dismiss();
					});

			sheet.btnClear.setOnClickListener(
					v -> {
						if (callback != null) {
							callback.onReset();
						}
						dismiss();
					});

			return sheet.getRoot();
		}

		private void setupSortSelection(String sort) {
			if ("created".equals(sort)) sheet.sortCreated.setChecked(true);
			else if ("stars".equals(sort)) sheet.sortStars.setChecked(true);
			else if ("forks".equals(sort)) sheet.sortForks.setChecked(true);
			else if ("size".equals(sort)) sheet.sortSize.setChecked(true);
			else sheet.sortUpdated.setChecked(true);
		}

		private String getSelectedSort() {
			int id = sheet.sortChipGroup.getCheckedChipId();
			if (id == R.id.sortCreated) return "created";
			if (id == R.id.sortStars) return "stars";
			if (id == R.id.sortForks) return "forks";
			if (id == R.id.sortSize) return "size";
			return "updated";
		}

		private void setupValidation() {
			validate();

			sheet.searchQueryEdit.addTextChangedListener(
					new TextWatcher() {
						@Override
						public void beforeTextChanged(
								CharSequence s, int start, int count, int after) {}

						@Override
						public void onTextChanged(
								CharSequence s, int start, int before, int count) {
							validate();
						}

						@Override
						public void afterTextChanged(Editable s) {}
					});

			sheet.filterChipGroup.setOnCheckedStateChangeListener(
					(group, checkedIds) -> validate());
			sheet.sortChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> validate());
		}

		private void validate() {
			String query =
					Objects.requireNonNull(sheet.searchQueryEdit.getText()).toString().trim();

			boolean hasFilterChips =
					sheet.includeTopicChip.isChecked()
							|| sheet.includeDescChip.isChecked()
							|| sheet.includeTemplateChip.isChecked()
							|| sheet.onlyArchivedChip.isChecked();

			boolean isSortChanged = !getSelectedSort().equals("updated");

			sheet.btnApply.setEnabled(!query.isEmpty() || hasFilterChips || isSortChanged);
		}

		public interface SearchCallback {
			void onSearchApplied(
					String query,
					boolean topic,
					boolean desc,
					boolean template,
					boolean archived,
					String sort);

			void onReset();
		}
	}
}

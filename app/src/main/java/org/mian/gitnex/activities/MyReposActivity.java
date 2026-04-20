package org.mian.gitnex.activities;

import android.os.Bundle;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.search.SearchView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.ActivityRepositoriesBinding;
import org.mian.gitnex.fragments.BottomSheetCreateRepo;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class MyReposActivity extends BaseActivity {

	private ActivityRepositoriesBinding binding;
	private RepositoriesViewModel viewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private String currentSort = "recentupdate";
	private int resultLimit;
	private String userLogin;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityRepositoriesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		if (getAccount() != null && getAccount().getAccount() != null) {
			userLogin = getAccount().getAccount().getUserName();
		}

		resultLimit = Constants.getCurrentResultLimit(this);
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		getSupportFragmentManager()
				.setFragmentResultListener(
						"repo_created",
						this,
						(requestKey, bundle) -> {
							boolean shouldRefresh = bundle.getBoolean("refresh");
							if (shouldRefresh) {
								refreshData();
							}
						});

		setupUI();
		setupSearch();
		observeViewModel();

		refreshData();
	}

	private void setupUI() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnSearch.setOnClickListener(v -> binding.searchView.show());
		binding.btnNewRepository.setOnClickListener(
				v ->
						BottomSheetCreateRepo.newInstance(null, false)
								.show(getSupportFragmentManager(), "create_repo"));

		binding.btnMore.setOnClickListener(
				v -> {
					SortBottomSheetDialogFragment bottomSheet = new SortBottomSheetDialogFragment();
					bottomSheet.setSortListener(
							sort -> {
								currentSort = sort;
								refreshData();
							});
					bottomSheet.show(getSupportFragmentManager(), "SortBottomSheet");
				});

		adapter = new ReposListAdapter(new ArrayList<>(), this);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (binding.searchView.isShowing()) return;
						viewModel.fetchRepos(
								MyReposActivity.this,
								"myRepos",
								userLogin,
								null,
								page,
								resultLimit,
								currentSort,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	private void setupSearch() {
		binding.searchResultsRecycler.setAdapter(adapter);
		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								adapter.getFilter().filter(s.toString().trim());
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState == SearchView.TransitionState.HIDDEN) {
						List<Repository> originalList = viewModel.getRepos().getValue();
						if (originalList != null) adapter.updateList(originalList);
						binding.recyclerView.scrollToPosition(0);
					}
				});
	}

	@Override
	protected void onGlobalRefresh() {
		refreshData();
	}

	private void observeViewModel() {
		viewModel
				.getRepos()
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
		if (scrollListener != null) {
			scrollListener.resetState();
		}
		if (adapter != null) {
			adapter.updateList(new ArrayList<>());
		}
		viewModel.resetPagination();
		viewModel.fetchRepos(this, "myRepos", userLogin, null, 1, resultLimit, currentSort, true);
	}

	public static class SortBottomSheetDialogFragment extends BottomSheetDialogFragment {
		private SortListener sortListener;

		public interface SortListener {
			void onSortSelected(String sort);
		}

		public void setSortListener(SortListener listener) {
			this.sortListener = listener;
		}

		@Override
		public View onCreateView(
				@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.bottomsheet_repositories_sort, container, false);
			ChipGroup chipGroup = view.findViewById(R.id.sort_chip_group);

			String[] sorts = {
				"name",
				"id",
				"newest",
				"oldest",
				"recentupdate",
				"leastupdate",
				"reversealphabetically",
				"alphabetically",
				"reversesize",
				"size",
				"moststars",
				"feweststars",
				"mostforks",
				"fewestforks"
			};

			String currentSort = ((MyReposActivity) requireActivity()).currentSort;

			for (String sort : sorts) {
				Chip chip = new Chip(requireContext());
				chip.setText(formatSortLabel(sort));
				chip.setCheckable(true);
				chip.setChecked(sort.equals(currentSort));
				chip.setOnClickListener(
						v -> {
							if (sortListener != null) sortListener.onSortSelected(sort);
							dismiss();
						});
				chipGroup.addView(chip);
			}
			chipGroup.setSingleSelection(true);
			return view;
		}

		private String formatSortLabel(String sort) {

			String[][] compoundMappings = {
				{"recentupdate", getString(R.string.recent_update)},
				{"leastupdate", getString(R.string.least_update)},
				{"reversealphabetically", getString(R.string.reverse_alphabetically)},
				{"reversesize", getString(R.string.reverse_size)},
				{"moststars", getString(R.string.most_stars)},
				{"feweststars", getString(R.string.fewest_stars)},
				{"mostforks", getString(R.string.most_forks)},
				{"fewestforks", getString(R.string.fewest_forks)}
			};

			for (String[] mapping : compoundMappings) {
				if (mapping[0].equals(sort)) {
					return mapping[1];
				}
			}

			return switch (sort) {
				case "name" -> getString(R.string.name);
				case "id" -> getString(R.string.id);
				case "newest" -> getString(R.string.newest);
				case "oldest" -> getString(R.string.oldest);
				case "size" -> getString(R.string.size);
				default -> sort;
			};
		}
	}
}

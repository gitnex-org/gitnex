package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class RepositoriesFragment extends Fragment {

	private FragmentRepositoriesBinding binding;
	private RepositoriesViewModel viewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private String currentSort = "recentupdate";
	private int resultLimit;
	private String type = "repos";
	private String userLogin = null;
	private String orgName = null;
	private boolean isSearching = false;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);

		getChildFragmentManager()
				.setFragmentResultListener(
						"repo_created",
						getViewLifecycleOwner(),
						(requestKey, bundle) -> {
							if (bundle.getBoolean("refresh")) {
								refreshData();
							}
						});
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentRepositoriesBinding.inflate(inflater, container, false);
		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		if (getArguments() != null) {
			type = getArguments().getString("type", "repos");
			userLogin = getArguments().getString("userLogin");
			orgName = getArguments().getString("orgName");
		}

		setupRecyclerView();
		setupSwipeRefresh();
		setupSearch();
		observeViewModel();

		refreshData();
		return binding.getRoot();
	}

	public void refreshFromGlobal() {
		refreshData();
	}

	private void setupRecyclerView() {
		adapter = new ReposListAdapter(new ArrayList<>(), requireContext());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (isSearching) return;

						viewModel.fetchRepos(
								requireContext(),
								type,
								userLogin,
								orgName,
								page,
								resultLimit,
								currentSort,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
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

		viewModel.getHasLoadedOnce().observe(getViewLifecycleOwner(), hasLoaded -> updateUiState());

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
						});

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
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
	}

	public void refreshData() {
		if (scrollListener != null) scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchRepos(
				requireContext(), type, userLogin, orgName, 1, resultLimit, currentSort, true);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (MainActivity.reloadRepos) {
			refreshData();
			MainActivity.reloadRepos = false;
		}
	}

	public void toggleSearch() {
		if (binding.searchView.isShowing()) {
			binding.searchView.hide();
		} else {
			binding.searchView.show();
		}
	}

	private void setupSearch() {
		binding.searchResultsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
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

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState.toString().equals("HIDDEN")) {
						binding.searchView.setText("");
						filter("");
						isSearching = false;
						binding.recyclerView.scrollToPosition(0);
					} else if (newState.toString().equals("SHOWN")) {
						isSearching = true;
					}
				});

		binding.searchView
				.getEditText()
				.setOnEditorActionListener(
						(v, actionId, event) -> {
							binding.searchView.hide();
							return false;
						});
	}

	private void filter(String text) {
		if (adapter != null && adapter.getFilter() != null) {
			adapter.getFilter().filter(text);
		}
	}

	public void openSortMenu() {
		SortBottomSheetDialogFragment bottomSheet = new SortBottomSheetDialogFragment();
		bottomSheet.setSortListener(
				sort -> {
					currentSort = sort;
					refreshData();
				});
		bottomSheet.show(getChildFragmentManager(), "SortBottomSheet");
	}

	public void createNewRepo() {
		BottomSheetCreateRepo.newInstance(null, false)
				.show(getChildFragmentManager(), "create_repo");
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

			View view = inflater.inflate(R.layout.bottom_sheet_repositories_sort, container, false);
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
			String currentSort = ((RepositoriesFragment) requireParentFragment()).currentSort;

			for (String sort : sorts) {
				Chip chip = new Chip(requireContext());
				chip.setText(formatSortLabel(sort));
				chip.setCheckable(true);
				if (sort.equals(currentSort)) {
					chip.setChecked(true);
				}
				chip.setOnClickListener(
						v -> {
							if (sortListener != null) {
								sortListener.onSortSelected(sort);
							}
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

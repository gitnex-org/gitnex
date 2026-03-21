package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.CreateRepoActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class MyRepositoriesFragment extends Fragment {

	private FragmentRepositoriesBinding binding;
	private RepositoriesViewModel viewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private String currentSort = "recentupdate";
	private int resultLimit;
	private String userLogin;
	private boolean isSearching = false;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentRepositoriesBinding.inflate(inflater, container, false);

		if (requireActivity() instanceof BaseActivity) {
			userLogin = ((BaseActivity) requireActivity()).getAccount().getAccount().getUserName();
		}

		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupMenu();
		observeViewModel();

		refreshData();
		return binding.getRoot();
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
						if (!isSearching) {
							viewModel.fetchRepos(
									requireContext(),
									"myRepos",
									userLogin,
									null,
									page,
									resultLimit,
									currentSort,
									false);
						}
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		binding.addNewRepo.setOnClickListener(
				v -> startActivity(new Intent(getContext(), CreateRepoActivity.class)));
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
								SearchView searchView = (SearchView) searchItem.getActionView();
								if (searchView != null) {
									searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
									searchView.setOnQueryTextListener(
											new SearchView.OnQueryTextListener() {
												@Override
												public boolean onQueryTextSubmit(String query) {
													return false;
												}

												@Override
												public boolean onQueryTextChange(String newText) {
													isSearching = !newText.isEmpty();
													adapter.getFilter().filter(newText);
													return true;
												}
											});
								}
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								if (menuItem.getItemId() == R.id.genericMenu) {
									SortBottomSheetDialogFragment bottomSheet =
											new SortBottomSheetDialogFragment();
									bottomSheet.setSortListener(
											sort -> {
												currentSort = sort;
												refreshData();
											});
									bottomSheet.show(getChildFragmentManager(), "SortBottomSheet");
									return true;
								}
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);
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

	private void refreshData() {
		if (scrollListener != null) scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchRepos(
				requireContext(), "myRepos", userLogin, null, 1, resultLimit, currentSort, true);
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
			String currentSort = ((MyRepositoriesFragment) requireParentFragment()).currentSort;

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

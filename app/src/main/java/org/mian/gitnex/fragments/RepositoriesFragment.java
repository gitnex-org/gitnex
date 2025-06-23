package org.mian.gitnex.fragments;

import android.content.Intent;
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
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateRepoActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author M M Arif
 */
public class RepositoriesFragment extends Fragment {

	private RepositoriesViewModel repositoriesViewModel;
	private FragmentRepositoriesBinding fragmentRepositoriesBinding;
	private ReposListAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private String currentSort = "recentupdate";

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentRepositoriesBinding =
				FragmentRepositoriesBinding.inflate(inflater, container, false);

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
												return false;
											}

											@Override
											public boolean onQueryTextChange(String newText) {
												if (fragmentRepositoriesBinding.recyclerView
																.getAdapter()
														!= null) {
													adapter.getFilter().filter(newText);
												}
												return false;
											}
										});
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								if (menuItem.getItemId() == R.id.genericMenu) {
									SortBottomSheetDialogFragment bottomSheet =
											new SortBottomSheetDialogFragment();
									bottomSheet.setSortListener(
											sort -> {
												currentSort = sort;
												page = 1;
												fragmentRepositoriesBinding.progressBar
														.setVisibility(View.VISIBLE);
												fetchDataAsync();
											});
									bottomSheet.show(getChildFragmentManager(), "SortBottomSheet");
									return true;
								}
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);

		repositoriesViewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(getContext());

		fragmentRepositoriesBinding.addNewRepo.setOnClickListener(
				view -> {
					Intent intent = new Intent(view.getContext(), CreateRepoActivity.class);
					startActivity(intent);
				});

		fragmentRepositoriesBinding.recyclerView.setHasFixedSize(true);
		fragmentRepositoriesBinding.recyclerView.setLayoutManager(
				new LinearLayoutManager(getContext()));

		fragmentRepositoriesBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											fragmentRepositoriesBinding.pullToRefresh.setRefreshing(
													false);
											fetchDataAsync();
											fragmentRepositoriesBinding.progressBar.setVisibility(
													View.VISIBLE);
										},
										50));

		fetchDataAsync();

		return fragmentRepositoriesBinding.getRoot();
	}

	private void fetchDataAsync() {

		repositoriesViewModel
				.getRepositories(
						page,
						resultLimit,
						null,
						"repos",
						null,
						getContext(),
						fragmentRepositoriesBinding,
						currentSort)
				.observe(
						getViewLifecycleOwner(),
						reposListMain -> {
							adapter = new ReposListAdapter(reposListMain, getContext());
							adapter.setLoadMoreListener(
									new ReposListAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											repositoriesViewModel.loadMoreRepos(
													page,
													resultLimit,
													null,
													"repos",
													null,
													getContext(),
													adapter,
													currentSort);
											fragmentRepositoriesBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											fragmentRepositoriesBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
								fragmentRepositoriesBinding.noData.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
								fragmentRepositoriesBinding.noData.setVisibility(View.VISIBLE);
							}

							fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
						});
	}

	@Override
	public void onResume() {
		super.onResume();

		if (MainActivity.reloadRepos) {
			page = 1;
			fetchDataAsync();
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

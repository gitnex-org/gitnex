package org.mian.gitnex.fragments;

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
import java.util.ArrayList;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.BottomSheetExploreFiltersBinding;
import org.mian.gitnex.databinding.FragmentExploreRepoBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class ExploreRepositoriesFragment extends Fragment {

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

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewBinding = FragmentExploreRepoBinding.inflate(inflater, container, false);
		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupMenu();
		observeViewModel();

		refreshData();
		return viewBinding.getRoot();
	}

	private void setupRecyclerView() {
		adapter = new ReposListAdapter(new ArrayList<>(), requireContext());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		viewBinding.recyclerViewReposSearch.setLayoutManager(layoutManager);
		viewBinding.recyclerViewReposSearch.setAdapter(adapter);

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
								false);
					}
				};
		viewBinding.recyclerViewReposSearch.addOnScrollListener(scrollListener);
	}

	private void setupMenu() {
		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menu.clear();
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
													searchQuery = query;
													refreshData();
													searchView.setQuery(null, false);
													searchItem.collapseActionView();
													return true;
												}

												@Override
												public boolean onQueryTextChange(String newText) {
													return false;
												}
											});
								}
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								if (menuItem.getItemId() == R.id.genericMenu) {
									showFilterBottomSheet();
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

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							viewBinding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
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
		viewModel.searchExploreRepos(
				requireContext(),
				searchQuery,
				includeTopic,
				includeDescription,
				includeTemplate,
				onlyArchived,
				1,
				resultLimit,
				true);
	}

	private void setupSwipeRefresh() {
		viewBinding.pullToRefresh.setOnRefreshListener(
				() -> {
					viewBinding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	private void showFilterBottomSheet() {
		BottomSheetFilterFragment bottomSheet =
				BottomSheetFilterFragment.newInstance(
						includeTopic,
						includeDescription,
						includeTemplate,
						onlyArchived,
						(topic, desc, template, archived) -> {
							includeTopic = topic;
							includeDescription = desc;
							includeTemplate = template;
							onlyArchived = archived;
							refreshData();
						});
		bottomSheet.show(getChildFragmentManager(), "exploreFiltersBottomSheet");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (MainActivity.reloadRepos) {
			refreshData();
			MainActivity.reloadRepos = false;
		}
	}

	public static class BottomSheetFilterFragment extends BottomSheetDialogFragment {

		private static final String ARG_INCLUDE_TOPIC = "includeTopic";
		private static final String ARG_INCLUDE_DESC = "includeDescription";
		private static final String ARG_INCLUDE_TEMPLATE = "includeTemplate";
		private static final String ARG_ONLY_ARCHIVED = "onlyArchived";
		private FilterCallback callback;

		public static BottomSheetFilterFragment newInstance(
				boolean includeTopic,
				boolean includeDescription,
				boolean includeTemplate,
				boolean onlyArchived,
				FilterCallback callback) {
			BottomSheetFilterFragment fragment = new BottomSheetFilterFragment();
			Bundle args = new Bundle();
			args.putBoolean(ARG_INCLUDE_TOPIC, includeTopic);
			args.putBoolean(ARG_INCLUDE_DESC, includeDescription);
			args.putBoolean(ARG_INCLUDE_TEMPLATE, includeTemplate);
			args.putBoolean(ARG_ONLY_ARCHIVED, onlyArchived);
			fragment.setArguments(args);
			fragment.callback = callback;
			return fragment;
		}

		@Override
		public View onCreateView(
				@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			BottomSheetExploreFiltersBinding binding =
					BottomSheetExploreFiltersBinding.inflate(inflater, container, false);

			Bundle args = getArguments();
			boolean includeTopic = args != null && args.getBoolean(ARG_INCLUDE_TOPIC, false);
			boolean includeDescription = args != null && args.getBoolean(ARG_INCLUDE_DESC, false);
			boolean includeTemplate = args != null && args.getBoolean(ARG_INCLUDE_TEMPLATE, false);
			boolean onlyArchived = args != null && args.getBoolean(ARG_ONLY_ARCHIVED, false);

			binding.includeTopicChip.setChecked(includeTopic);
			binding.includeDescChip.setChecked(includeDescription);
			binding.includeTemplateChip.setChecked(includeTemplate);
			binding.onlyArchivedChip.setChecked(onlyArchived);

			binding.filterChipGroup.setOnCheckedStateChangeListener(
					(group, checkedIds) -> {
						boolean newIncludeTopic = checkedIds.contains(R.id.includeTopicChip);
						boolean newIncludeDescription = checkedIds.contains(R.id.includeDescChip);
						boolean newIncludeTemplate = checkedIds.contains(R.id.includeTemplateChip);
						boolean newOnlyArchived = checkedIds.contains(R.id.onlyArchivedChip);
						if (callback != null) {
							callback.onFiltersApplied(
									newIncludeTopic,
									newIncludeDescription,
									newIncludeTemplate,
									newOnlyArchived);
						}
					});

			return binding.getRoot();
		}

		public interface FilterCallback {
			void onFiltersApplied(
					boolean includeTopic,
					boolean includeDescription,
					boolean includeTemplate,
					boolean onlyArchived);
		}
	}
}

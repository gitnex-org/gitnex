package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateIssueActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.databinding.FragmentIssuesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.IssueFilterState;
import org.mian.gitnex.models.RepositoryMenuItemModel;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author mmarif
 */
public class IssuesFragment extends Fragment implements RepoDetailActivity.RepoHubProvider {

	public static boolean resumeIssues = false;
	private final String requestType = Constants.issuesRequestType;
	private FragmentIssuesBinding binding;
	private IssuesViewModel viewModel;
	private IssuesAdapter adapter;
	private IssuesAdapter adapterPinned;
	private RepositoryContext repository;
	private int resultLimit;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int systemTopInset = 0;
	private boolean isFirstLoad = true;

	public static IssuesFragment newInstance(RepositoryContext repository) {
		IssuesFragment f = new IssuesFragment();
		f.setArguments(repository.getBundle());
		return f;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ViewCompat.setOnApplyWindowInsetsListener(
				binding.insetContainer,
				(v, insets) -> {
					Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
					systemTopInset = systemBars.top;
					refreshPaddingLogic();

					return insets;
				});
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentIssuesBinding.inflate(inflater, container, false);

		viewModel = new ViewModelProvider(requireActivity()).get(IssuesViewModel.class);

		repository = RepositoryContext.fromBundle(requireArguments());
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupAdapters();
		setupRepoListeners();
		observeRepoViewModel();

		return binding.getRoot();
	}

	@Override
	public List<RepositoryMenuItemModel> getRepoHubItems() {
		List<RepositoryMenuItemModel> items = new ArrayList<>();

		items.add(
				new RepositoryMenuItemModel(
						"ISSUES_SEARCH",
						R.string.search_filter,
						R.drawable.ic_search,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		if (repository.getRepository().isHasIssues() && !repository.getRepository().isArchived()) {
			items.add(
					new RepositoryMenuItemModel(
							"ISSUE_CREATE_NEW",
							R.string.create_issue,
							R.drawable.ic_add,
							R.attr.colorPrimaryContainer,
							R.attr.colorOnPrimaryContainer));
		}

		return items;
	}

	@Override
	public void onHubActionSelected(String actionId) {
		switch (actionId) {
			case "ISSUES_SEARCH":
				BottomSheetIssuesFilter.newInstance(repository)
						.show(getChildFragmentManager(), "issues_filter");
				break;

			case "ISSUE_CREATE_NEW":
				startActivity(repository.getIntent(requireContext(), CreateIssueActivity.class));
				break;
		}
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
		viewModel.fetchPinnedIssues(requireContext(), repository.getOwner(), repository.getName());
	}

	private void setupAdapters() {

		adapter = new IssuesAdapter(requireContext(), new ArrayList<>(), "");
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						IssueFilterState state = viewModel.getFilterState();
						String labelsParam =
								state.selectedLabels.isEmpty()
										? null
										: String.join(",", state.selectedLabels);

						viewModel.fetchRepoIssues(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								state.state,
								labelsParam,
								state.query,
								requestType,
								state.milestoneTitle,
								state.mentionedBy,
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		adapterPinned = new IssuesAdapter(requireContext(), new ArrayList<>(), "pinned");
		binding.rvPinnedIssues.setLayoutManager(
				new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
		binding.rvPinnedIssues.setHasFixedSize(true);
		binding.rvPinnedIssues.setNestedScrollingEnabled(false);
		binding.rvPinnedIssues.setAdapter(adapterPinned);

		binding.rvPinnedIssues.addOnItemTouchListener(
				new RecyclerView.OnItemTouchListener() {
					@Override
					public boolean onInterceptTouchEvent(
							@NonNull RecyclerView rv, @NonNull MotionEvent e) {
						if (e.getAction() == MotionEvent.ACTION_DOWN
								|| e.getAction() == MotionEvent.ACTION_MOVE) {
							rv.getParent().requestDisallowInterceptTouchEvent(true);
						}
						return false;
					}

					@Override
					public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

					@Override
					public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
				});
	}

	private void refreshPaddingLogic() {
		if (binding == null) return;

		int dimen12 = getResources().getDimensionPixelSize(R.dimen.dimen12dp);
		if (dimen12 == 0) dimen12 = 36;

		List<Issue> pinnedList = viewModel.getPinnedIssues().getValue();
		boolean hasPinned = pinnedList != null && !pinnedList.isEmpty();

		ViewGroup.MarginLayoutParams params =
				(ViewGroup.MarginLayoutParams) binding.recyclerView.getLayoutParams();

		if (hasPinned) {
			binding.rvPinnedIssues.setPadding(0, systemTopInset, 0, 0);
			params.topMargin = 0;
			binding.recyclerView.setPadding(dimen12, 0, dimen12, dimen12);
		} else {
			binding.rvPinnedIssues.setPadding(0, 0, 0, 0);
			params.topMargin = systemTopInset + dimen12;
			binding.recyclerView.setPadding(dimen12, 0, dimen12, dimen12);
		}

		binding.recyclerView.setLayoutParams(params);
	}

	private void observeRepoViewModel() {
		viewModel
				.getRepoIssues()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list != null ? list : new ArrayList<>());
							updateUiState();
						});

		viewModel
				.getPinnedIssues()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							binding.pinnedIssuesFrame.setVisibility(
									list.isEmpty() ? View.GONE : View.VISIBLE);
							adapterPinned.updateList(list);

							refreshPaddingLogic();
							updateUiState();
						});

		viewModel
				.getIsRepoLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (loading) {
								if (adapter.getItemCount() == 0) {
									binding.expressiveLoader.setVisibility(View.VISIBLE);
									binding.layoutEmpty.getRoot().setVisibility(View.GONE);
									binding.recyclerView.setVisibility(View.GONE);
								}
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
								binding.pullToRefresh.setRefreshing(false);
								updateUiState();
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						err -> {
							if (err != null) {
								Toasty.show(requireContext(), err);
								binding.expressiveLoader.setVisibility(View.GONE);
								binding.pullToRefresh.setRefreshing(false);
							}
						});
	}

	private void updateUiState() {
		boolean isMainListEmpty = adapter.getItemCount() == 0;
		boolean hasLoaded = Boolean.TRUE.equals(viewModel.getHasRepoLoadedOnce().getValue());
		boolean isLoading = Boolean.TRUE.equals(viewModel.getIsRepoLoading().getValue());

		if (!isLoading && hasLoaded && isMainListEmpty) {
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.VISIBLE);
		}
	}

	private void refreshData() {
		scrollListener.resetState();

		adapter.updateList(new ArrayList<>());

		binding.recyclerView.setVisibility(View.GONE);
		binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		binding.expressiveLoader.setVisibility(View.VISIBLE);

		viewModel.applyFilters(
				requireContext(), repository.getOwner(), repository.getName(), resultLimit);
		viewModel.fetchPinnedIssues(requireContext(), repository.getOwner(), repository.getName());
	}

	private void setupRepoListeners() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

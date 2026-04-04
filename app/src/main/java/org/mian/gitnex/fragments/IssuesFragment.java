package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.databinding.FragmentIssuesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author mmarif
 */
public class IssuesFragment extends Fragment {

	public static boolean resumeIssues = false;
	private final String requestType = Constants.issuesRequestType;
	private FragmentIssuesBinding binding;
	private IssuesViewModel viewModel;
	private IssuesAdapter adapter;
	private IssuesAdapter adapterPinned;
	private RepositoryContext repository;
	private String selectedLabels = null;
	private String mentionedBy;
	private int resultLimit;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int systemTopInset = 0;

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

		viewModel = new ViewModelProvider(this).get(IssuesViewModel.class);

		repository = RepositoryContext.fromBundle(requireArguments());
		mentionedBy = repository.getMentionedBy();
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupAdapters();
		setupRepoListeners();
		observeRepoViewModel();

		refreshData(null);
		viewModel.fetchPinnedIssues(requireContext(), repository.getOwner(), repository.getName());

		handleArchivedState();
		setupMenu();

		return binding.getRoot();
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
						viewModel.fetchRepoIssues(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								repository.getIssueState().toString(),
								selectedLabels,
								null,
								requestType,
								repository.getIssueMilestoneFilterName(),
								mentionedBy,
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

		if (hasPinned) {
			binding.rvPinnedIssues.setPadding(0, systemTopInset, 0, 0);
			binding.recyclerView.setPadding(dimen12, 0, dimen12, dimen12);
		} else {
			binding.recyclerView.setPadding(dimen12, systemTopInset, dimen12, dimen12);
			binding.rvPinnedIssues.setPadding(0, 0, 0, 0);
		}
	}

	private void observeRepoViewModel() {
		viewModel
				.getRepoIssues()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
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
							if (loading && adapter.getItemCount() == 0) {
								binding.expressiveLoader.setVisibility(View.VISIBLE);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
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
							}
						});
	}

	private void updateUiState() {
		boolean isMainListEmpty = adapter.getItemCount() == 0;
		boolean isPinnedListEmpty = adapterPinned.getItemCount() == 0;
		boolean hasLoaded = Boolean.TRUE.equals(viewModel.getHasRepoLoadedOnce().getValue());
		boolean isLoading = Boolean.TRUE.equals(viewModel.getIsRepoLoading().getValue());

		if (!isLoading && hasLoaded && isMainListEmpty && isPinnedListEmpty) {
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.VISIBLE);
		}
	}

	private void refreshData(String query) {
		scrollListener.resetState();
		viewModel.resetRepoPagination();

		binding.layoutEmpty.getRoot().setVisibility(View.GONE);

		viewModel.fetchRepoIssues(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				repository.getIssueState().toString(),
				selectedLabels,
				query,
				requestType,
				repository.getIssueMilestoneFilterName(),
				mentionedBy,
				1,
				resultLimit,
				true);

		viewModel.fetchPinnedIssues(requireContext(), repository.getOwner(), repository.getName());
	}

	private void setupRepoListeners() {
		binding.pullToRefresh.setOnRefreshListener(() -> refreshData(null));

		RepoDetailActivity activity = (RepoDetailActivity) requireActivity();

		activity.setFragmentRefreshListener(
				state -> {
					repository.setIssueState(RepositoryContext.State.valueOf(state.toUpperCase()));
					refreshData(null);
				});

		activity.setFragmentRefreshListenerFilterIssuesByMilestone(
				milestone -> {
					repository.setIssueMilestoneFilterName(milestone);
					refreshData(null);
				});

		activity.setFragmentRefreshListenerFilterIssuesByLabels(
				labels -> {
					selectedLabels = labels;
					refreshData(null);
				});

		activity.setFragmentRefreshListenerFilterIssuesByMentions(
				username -> {
					mentionedBy = username;
					repository.setMentionedBy(username);
					refreshData(null);
				});
	}

	private void handleArchivedState() {
		boolean archived = repository.getRepository().isArchived();
		/*if (repository.getRepository().isHasIssues() && !archived) {
			binding.createNewIssue.setVisibility(View.VISIBLE);
			binding.createNewIssue.setOnClickListener(
					v ->
							((RepoDetailActivity) requireActivity())
									.createIssueLauncher.launch(
											repository.getIntent(
													getContext(), CreateIssueActivity.class)));
		} else {
			binding.createNewIssue.setVisibility(View.GONE);
		}*/
	}

	private void setupMenu() {
		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.search_menu, menu);
								menuInflater.inflate(R.menu.filter_menu, menu);

								if (repository.getIssueState().toString().equals("closed")) {
									menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
								}

								MenuItem searchItem = menu.findItem(R.id.action_search);
								androidx.appcompat.widget.SearchView searchView =
										(androidx.appcompat.widget.SearchView)
												searchItem.getActionView();
								if (searchView != null) {
									searchView.setOnQueryTextListener(
											new androidx.appcompat.widget.SearchView
													.OnQueryTextListener() {
												@Override
												public boolean onQueryTextSubmit(String query) {
													refreshData(query);
													searchItem.collapseActionView();
													return false;
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
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (resumeIssues) {
			refreshData(null);
			viewModel.fetchPinnedIssues(
					requireContext(), repository.getOwner(), repository.getName());
			resumeIssues = false;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

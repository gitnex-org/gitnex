package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Commit;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CommitDetailActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.CommitsAdapter;
import org.mian.gitnex.databinding.FragmentCommitsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.RepositoryMenuItemModel;
import org.mian.gitnex.viewmodels.CommitsViewModel;

/**
 * @author mmarif
 */
public class CommitsFragment extends Fragment implements RepoDetailActivity.RepoHubProvider {

	private FragmentCommitsBinding binding;
	private CommitsViewModel viewModel;
	private CommitsAdapter adapter;
	private RepositoryContext repository;
	private int resultLimit;
	private EndlessRecyclerViewScrollListener scrollListener;
	private boolean isFirstLoad = true;

	public static CommitsFragment newInstance(RepositoryContext repository) {
		CommitsFragment fragment = new CommitsFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Nullable
	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentCommitsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(CommitsViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupAdapters();
		setupListeners();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.commitsScroll, binding.pullToRefresh, null);

		String branch = repository.getBranchRef();
		if (branch == null || branch.isEmpty()) {
			branch = repository.getRepository().getDefaultBranch();
		}
		if (branch != null) {
			binding.branchLabel.setText(branch);
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
		initDataFetch();
	}

	public void refreshFromGlobal() {
		refreshData();
	}

	private void setupAdapters() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(layoutManager);

		adapter = new CommitsAdapter(requireContext(), new ArrayList<>(), this::navigateToCommitDetail);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchCommits(
								requireContext(), repository, page, resultLimit, false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupListeners() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void observeViewModel() {
		viewModel
				.getCommits()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (list == null) return;
							adapter.updateList(list);
							updateEmptyState(list.isEmpty());
							binding.pullToRefresh.setRefreshing(false);
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
								if (adapter.getItemCount() == 0) {
									binding.expressiveLoader.setVisibility(View.VISIBLE);
								}
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
								List<Commit> current = viewModel.getCommits().getValue();
								updateEmptyState(current == null || current.isEmpty());
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) {
								Toasty.show(requireContext(), msg);
								binding.pullToRefresh.setRefreshing(false);
								binding.expressiveLoader.setVisibility(View.GONE);
							}
						});
	}

	private void initDataFetch() {
		scrollListener.resetState();
		binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		viewModel.fetchCommits(requireContext(), repository, 1, resultLimit, true);
	}

	private void refreshData() {
		scrollListener.resetState();
		binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		viewModel.fetchCommits(requireContext(), repository, 1, resultLimit, true);
	}

	private void updateEmptyState(boolean isEmpty) {
		boolean loading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
		if (!loading) {
			binding.layoutEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
		}
	}

	private void navigateToCommitDetail(Commit commit) {
		Intent intent = repository.getIntent(requireContext(), CommitDetailActivity.class);
		intent.putExtra("owner", repository.getOwner());
		intent.putExtra("repo", repository.getName());
		intent.putExtra("sha", commit.getSha());
		startActivity(intent);
	}

	@Override
	public List<RepositoryMenuItemModel> getRepoHubItems() {
		List<RepositoryMenuItemModel> items = new ArrayList<>();
		items.add(
				new RepositoryMenuItemModel(
						"COMMITS_OPEN_FULL",
						R.string.commits,
						R.drawable.ic_commit,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));
		return items;
	}

	@Override
	public void onHubActionSelected(String actionId) {
		// No-op for now; hub menu opens the dedicated CommitsActivity via the dock route
		// in a follow-up. Keep this stub so RepoHubProvider stays non-abstract.
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

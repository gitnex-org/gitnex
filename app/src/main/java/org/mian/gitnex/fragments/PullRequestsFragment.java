package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.PullRequestsAdapter;
import org.mian.gitnex.databinding.FragmentPullRequestsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.PullRequestsViewModel;

/**
 * @author mmarif
 */
public class PullRequestsFragment extends Fragment {

	public static boolean resumePullRequests = false;
	private FragmentPullRequestsBinding binding;
	private PullRequestsViewModel viewModel;
	private PullRequestsAdapter adapter;
	private RepositoryContext repository;
	private int resultLimit;
	private EndlessRecyclerViewScrollListener scrollListener;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);
	}

	public static PullRequestsFragment newInstance(RepositoryContext repository) {
		PullRequestsFragment f = new PullRequestsFragment();
		f.setArguments(repository.getBundle());
		return f;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentPullRequestsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(PullRequestsViewModel.class);

		repository = RepositoryContext.fromBundle(requireArguments());
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupAdapter();
		setupListeners();
		observeViewModel();

		refreshData();
		handleArchivedState();
		setupMenu();

		return binding.getRoot();
	}

	private void setupAdapter() {
		adapter = new PullRequestsAdapter(requireContext(), new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchPullRequests(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								repository.getPrState().toString(),
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getPrList()
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
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean hasLoaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		boolean isLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());

		if (!isLoading && hasLoaded && isEmpty) {
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.VISIBLE);
		}
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();

		binding.layoutEmpty.getRoot().setVisibility(View.GONE);

		viewModel.fetchPullRequests(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				repository.getPrState().toString(),
				1,
				resultLimit,
				true);
	}

	private void setupListeners() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);

		((RepoDetailActivity) requireActivity())
				.setFragmentRefreshListenerPr(
						state -> {
							repository.setPrState(
									RepositoryContext.State.valueOf(state.toUpperCase()));
							refreshData();
						});
	}

	private void handleArchivedState() {
		boolean archived = repository.getRepository().isArchived();
		if (repository.getRepository().isHasPullRequests() && !archived) {
			// binding.createPullRequest.setVisibility(View.VISIBLE);
			// binding.createPullRequest.setOnClickListener(v ->
			// ((RepoDetailActivity) requireActivity()).createPrLauncher.launch(
			//	repository.getIntent(requireContext(), CreatePullRequestActivity.class)));
		} else {
			// binding.createPullRequest.setVisibility(View.GONE);
		}
	}

	private void setupMenu() {
		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.search_menu, menu);
								menuInflater.inflate(R.menu.filter_menu_pr, menu);

								if (repository.getPrState().toString().equals("closed")) {
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
													return false;
												}

												@Override
												public boolean onQueryTextChange(String newText) {
													filterLocal(newText);
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

	private void filterLocal(String text) {
		List<PullRequest> fullList = viewModel.getPrList().getValue();
		if (fullList == null) return;

		List<PullRequest> filtered = new ArrayList<>();
		for (PullRequest pr : fullList) {
			if (pr.getTitle().toLowerCase().contains(text.toLowerCase())
					|| String.valueOf(pr.getNumber()).startsWith(text)) {
				filtered.add(pr);
			}
		}
		adapter.updateList(filtered);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (resumePullRequests) {
			refreshData();
			resumePullRequests = false;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

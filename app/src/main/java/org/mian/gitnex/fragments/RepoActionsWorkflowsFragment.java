package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import org.mian.gitnex.adapters.RepoActionsWorkflowsAdapter;
import org.mian.gitnex.databinding.FragmentRepositoryActionsBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepositoryActionsViewModel;

/**
 * @author mmarif
 */
public class RepoActionsWorkflowsFragment extends Fragment {

	private FragmentRepositoryActionsBinding binding;
	private RepositoryActionsViewModel viewModel;
	private RepoActionsWorkflowsAdapter adapter;
	private RepositoryContext repository;

	public static RepoActionsWorkflowsFragment newInstance(RepositoryContext repository) {
		RepoActionsWorkflowsFragment fragment = new RepoActionsWorkflowsFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentRepositoryActionsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(RepositoryActionsViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();

		if (Boolean.FALSE.equals(viewModel.getHasLoadedWorkflowsOnce().getValue())) {
			refreshData();
		}

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter = new RepoActionsWorkflowsAdapter(requireContext(), new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);
	}

	private void observeViewModel() {
		viewModel
				.getWorkflows()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							updateUiState();
						});

		viewModel
				.getHasLoadedWorkflowsOnce()
				.observe(getViewLifecycleOwner(), hasLoaded -> updateUiState());

		viewModel
				.getIsLoadingWorkflows()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
							if (!loading) binding.pullToRefresh.setRefreshing(false);
						});

		viewModel
				.getWorkflowsError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedWorkflowsOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
		binding.recyclerView.setVisibility(loaded && isEmpty ? View.GONE : View.VISIBLE);
	}

	private void refreshData() {
		viewModel.fetchWorkflows(
				requireContext(), repository.getOwner(), repository.getName(), true);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

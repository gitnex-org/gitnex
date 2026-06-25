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
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ForgejoActionsVariablesAdapter;
import org.mian.gitnex.databinding.FragmentRepositoryActionsBinding;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepositoryForgejoActionsViewModel;

/**
 * @author mmarif
 */
public class RepositoryForgejoActionsVariablesFragment extends Fragment {

	private FragmentRepositoryActionsBinding binding;
	private RepositoryForgejoActionsViewModel viewModel;
	private ForgejoActionsVariablesAdapter adapter;
	private RepositoryContext repository;
	private EndlessRecyclerViewScrollListener scrollListener;
	private boolean isFirstLoad = true;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.recyclerView, binding.pullToRefresh, null);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentRepositoryActionsBinding.inflate(inflater, container, false);
		viewModel =
				new ViewModelProvider(requireActivity())
						.get(RepositoryForgejoActionsViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();

		return binding.getRoot();
	}

	public void refreshFromGlobal() {
		refreshData();
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
		if (Boolean.FALSE.equals(viewModel.getHasLoadedVariablesOnce().getValue())) {
			refreshData();
		}
	}

	private void setupRecyclerView() {
		adapter =
				new ForgejoActionsVariablesAdapter(
						requireContext(),
						new ArrayList<>(),
						(variable, position) -> {
							viewModel.deleteVariable(
									requireContext(),
									repository.getOwner(),
									repository.getName(),
									variable.getName(),
									position);
						});
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchVariables(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								page,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getVariables()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							updateUiState();
						});

		viewModel
				.getHasLoadedVariablesOnce()
				.observe(getViewLifecycleOwner(), hasLoaded -> updateUiState());

		viewModel
				.getIsLoadingVariables()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
							if (!loading) binding.pullToRefresh.setRefreshing(false);
						});

		viewModel
				.getVariablesError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});

		viewModel
				.getResult()
				.observe(
						getViewLifecycleOwner(),
						code -> {
							if (code != null && code == 204) {
								Toasty.show(requireContext(), R.string.variable_deleted);
								refreshData();
								viewModel.resetResult();
							}
						});
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedVariablesOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
		binding.recyclerView.setVisibility(loaded && isEmpty ? View.GONE : View.VISIBLE);
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetVariablesPagination();
		viewModel.fetchVariables(
				requireContext(), repository.getOwner(), repository.getName(), 1, true);
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

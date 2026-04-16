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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import org.gitnex.tea4j.v2.models.ActionVariable;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoActionsVariablesAdapter;
import org.mian.gitnex.databinding.FragmentRepositoryActionsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepositoryActionsViewModel;

/**
 * @author mmarif
 */
public class RepoActionsVariablesFragment extends Fragment {

	private FragmentRepositoryActionsBinding binding;
	private RepositoryActionsViewModel viewModel;
	private RepoActionsVariablesAdapter adapter;
	private RepositoryContext repository;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;

	public static RepoActionsVariablesFragment newInstance(RepositoryContext repository) {
		RepoActionsVariablesFragment fragment = new RepoActionsVariablesFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
		resultLimit = Constants.getCurrentResultLimit(requireContext());
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

		if (Boolean.FALSE.equals(viewModel.getHasLoadedVariablesOnce().getValue())) {
			refreshData();
		}

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter =
				new RepoActionsVariablesAdapter(
						requireContext(), new ArrayList<>(), this::showDeleteConfirmation);
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
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void showDeleteConfirmation(ActionVariable variable, int position) {
		new MaterialAlertDialogBuilder(requireContext())
				.setTitle(getString(R.string.deleteGenericTitle, variable.getName()))
				.setMessage(R.string.deleteVariableConfirmation)
				.setPositiveButton(
						R.string.menuDeleteText,
						(dialog, which) ->
								viewModel.deleteVariable(
										requireContext(),
										repository.getOwner(),
										repository.getName(),
										variable.getName(),
										position,
										resultLimit))
				.setNegativeButton(R.string.cancelButton, null)
				.show();
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
							if (code == null || code == -1) return;

							if (code == 200 || code == 201 || code == 204) {
								if (code == 204) {
									Toasty.show(requireContext(), R.string.variable_deleted);
								}
								refreshData();
							} else {
								Toasty.show(requireContext(), R.string.genericError);
							}

							viewModel.resetResults();
						});
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedVariablesOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
		binding.recyclerView.setVisibility(loaded && isEmpty ? View.GONE : View.VISIBLE);
	}

	public void refreshData() {
		if (scrollListener != null) scrollListener.resetState();
		viewModel.resetVariablesPagination();
		viewModel.fetchVariables(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				1,
				resultLimit,
				true);
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

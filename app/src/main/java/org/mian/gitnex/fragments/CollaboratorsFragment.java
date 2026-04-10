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
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.CollaboratorsAdapter;
import org.mian.gitnex.databinding.FragmentCollaboratorsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.RepositoryMenuItemModel;
import org.mian.gitnex.viewmodels.CollaboratorsViewModel;

/**
 * @author mmarif
 */
public class CollaboratorsFragment extends Fragment implements RepoDetailActivity.RepoHubProvider {

	private FragmentCollaboratorsBinding binding;
	private CollaboratorsViewModel viewModel;
	private CollaboratorsAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private RepositoryContext repository;
	private int resultLimit;
	private boolean isFirstLoad = true;

	public static CollaboratorsFragment newInstance(RepositoryContext repository) {
		CollaboratorsFragment fragment = new CollaboratorsFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentCollaboratorsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(CollaboratorsViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public List<RepositoryMenuItemModel> getRepoHubItems() {
		List<RepositoryMenuItemModel> items = new ArrayList<>();

		if (repository.getPermissions().isAdmin() && !repository.getRepository().isArchived()) {
			items.add(
					new RepositoryMenuItemModel(
							"COLLABORATOR_ADD_NEW",
							R.string.addButton,
							R.drawable.ic_add,
							R.attr.colorPrimaryContainer,
							R.attr.colorOnPrimaryContainer));
		}

		return items;
	}

	@Override
	public void onHubActionSelected(String actionId) {
		if (actionId.equals("COLLABORATOR_ADD_NEW")) {
			BottomSheetAddCollaborator.newInstance(repository)
					.show(getChildFragmentManager(), "AddCollaborator");
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
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchCollaborators(
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

	private void observeViewModel() {
		viewModel
				.getCollaborators()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (adapter == null) {
								adapter = new CollaboratorsAdapter(requireContext(), list);
								binding.recyclerView.setAdapter(adapter);
							} else {
								adapter.updateList(list);
							}
							binding.pullToRefresh.setRefreshing(false);
							updateUiVisibility(
									Boolean.TRUE.equals(viewModel.getIsLoading().getValue()));
						});

		viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateUiVisibility);
		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						err -> {
							if (err != null) Toasty.show(requireContext(), err);
						});
	}

	private void updateUiVisibility(boolean isLoading) {
		boolean hasData = adapter != null && adapter.getItemCount() > 0;
		boolean hasLoadedOnce = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);

		if (isLoading) {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		} else {
			binding.layoutEmpty
					.getRoot()
					.setVisibility(!hasData && hasLoadedOnce ? View.VISIBLE : View.GONE);
		}

		boolean showEmpty = !isLoading && !hasData && hasLoadedOnce;
		binding.pullToRefresh.setVisibility(showEmpty ? View.GONE : View.VISIBLE);
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchCollaborators(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				1,
				resultLimit,
				true);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

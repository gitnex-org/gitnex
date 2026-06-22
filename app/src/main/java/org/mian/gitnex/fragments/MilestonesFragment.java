package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.List;
import org.gitnex.tea4j.v2.models.EditMilestoneOption;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.MilestonesAdapter;
import org.mian.gitnex.bottomsheets.CreateMilestoneBottomSheet;
import org.mian.gitnex.bottomsheets.GenericMenuBottomSheet;
import org.mian.gitnex.databinding.FragmentMilestonesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.BookmarkHelper;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.GenericMenuItemModel;
import org.mian.gitnex.models.RepositoryMenuItemModel;
import org.mian.gitnex.viewmodels.MilestonesViewModel;

/**
 * @author mmarif
 */
public class MilestonesFragment extends Fragment implements RepoDetailActivity.RepoHubProvider {

	private FragmentMilestonesBinding binding;
	private MilestonesViewModel viewModel;
	private MilestonesAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private RepositoryContext repository;
	private int resultLimit;
	private String currentState = "open";
	private String milestoneIdToScroll;
	private boolean isFirstLoad = true;

	public static MilestonesFragment newInstance(RepositoryContext repository) {
		MilestonesFragment fragment = new MilestonesFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.recyclerView, binding.pullToRefresh, null);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());

		milestoneIdToScroll = requireActivity().getIntent().getStringExtra("milestoneId");
		requireActivity().getIntent().removeExtra("milestoneId");
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentMilestonesBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(MilestonesViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public List<RepositoryMenuItemModel> getRepoHubItems() {
		List<RepositoryMenuItemModel> items = new ArrayList<>();

		boolean isBookmarked =
				BookmarkHelper.isBookmarked(
						requireContext(),
						"repo",
						repository.getOwner(),
						repository.getName(),
						"milestones",
						null);
		boolean isCurrentlyOpen = "open".equals(currentState);

		items.add(
				new RepositoryMenuItemModel(
						"MILESTONE_FILTER_TOGGLE",
						isCurrentlyOpen ? R.string.isClosed : R.string.isOpen,
						R.drawable.ic_filter,
						isCurrentlyOpen
								? R.attr.colorTertiaryContainer
								: R.attr.colorSurfaceVariant,
						isCurrentlyOpen
								? R.attr.colorOnTertiaryContainer
								: R.attr.colorOnSurfaceVariant));

		if (repository.getPermissions().isAdmin() && !repository.getRepository().isArchived()) {
			items.add(
					new RepositoryMenuItemModel(
							"MILESTONE_ADD_NEW",
							R.string.pageTitleCreateMilestone,
							R.drawable.ic_add,
							R.attr.colorPrimaryContainer,
							R.attr.colorOnPrimaryContainer));
		}
		items.add(
				new RepositoryMenuItemModel(
						"BOOKMARK_TAB",
						isBookmarked ? R.string.bookmark_remove : R.string.bookmark_add,
						isBookmarked ? R.drawable.ic_bookmark_remove : R.drawable.ic_bookmark_add,
						isBookmarked ? R.attr.colorErrorContainer : R.attr.colorPrimarySurface,
						isBookmarked
								? R.attr.colorOnErrorContainer
								: R.attr.colorOnPrimarySurface));
		items.add(
				new RepositoryMenuItemModel(
						"CONTEXT_SHARE",
						R.string.share_location,
						R.drawable.ic_share,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		return items;
	}

	@Override
	public void onHubActionSelected(String actionId) {
		switch (actionId) {
			case "MILESTONE_FILTER_TOGGLE":
				currentState = currentState.equals("open") ? "closed" : "open";
				refreshData();
				break;
			case "MILESTONE_ADD_NEW":
				CreateMilestoneBottomSheet.newInstance(repository, null)
						.show(getChildFragmentManager(), "CREATE_MILESTONE");
				break;
			case "BOOKMARK_TAB":
				BookmarkHelper.toggleBookmark(
						requireContext(),
						"repo",
						repository.getOwner(),
						repository.getName(),
						"milestones",
						null,
						null,
						getString(R.string.milestones),
						UrlHelper.buildCurrentContextUrl(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								"milestones"));
				break;
			case "CONTEXT_SHARE":
				String url =
						UrlHelper.buildCurrentContextUrl(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								"milestones");
				AppUtil.sharingIntent(requireContext(), url);
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
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchMilestones(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								currentState,
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getMilestones()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							binding.pullToRefresh.setRefreshing(false);

							if (adapter == null) {
								boolean canEdit =
										repository.getPermissions().isPush()
												&& !repository.getRepository().isArchived();

								adapter =
										new MilestonesAdapter(
												requireContext(),
												list,
												canEdit,
												this::showMilestoneMenu);
								binding.recyclerView.setAdapter(adapter);
							} else {
								adapter.updateList(list);
							}

							if (milestoneIdToScroll != null) scrollToMilestone(list);
							updateUiVisibility(
									Boolean.TRUE.equals(viewModel.getIsLoading().getValue()));
						});

		viewModel
				.getActionResult()
				.observe(
						getViewLifecycleOwner(),
						code -> {
							if (code == -1) return;

							if (code == 200 || code == 201 || code == 204) {
								int messageRes;
								if (code == 201) messageRes = R.string.milestoneCreated;
								else if (code == 200) messageRes = R.string.milestoneStatusUpdate;
								else messageRes = R.string.milestone_deleted;

								Toasty.show(requireContext(), messageRes);
								refreshData();

								new Handler(Looper.getMainLooper())
										.postDelayed(
												() -> {
													if (isAdded()) viewModel.resetActionResult();
												},
												100);
							}
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

	private void showMilestoneMenu(Milestone milestone) {
		List<GenericMenuItemModel> items = new ArrayList<>();

		boolean isOpen = milestone.getState() == Milestone.StateEnum.OPEN;

		if (isOpen) {
			items.add(
					new GenericMenuItemModel(
							"MILESTONE_CLOSE",
							R.string.close,
							R.drawable.ic_close,
							R.attr.colorErrorContainer,
							R.attr.colorOnErrorContainer));
		} else {
			items.add(
					new GenericMenuItemModel(
							"MILESTONE_REOPEN",
							R.string.isOpen,
							R.drawable.ic_refresh,
							R.attr.colorPrimarySurface,
							R.attr.colorOnPrimarySurface));
		}

		items.add(
				new GenericMenuItemModel(
						"MILESTONE_EDIT",
						R.string.edit_milestone,
						R.drawable.ic_edit,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		items.add(
				new GenericMenuItemModel(
						"MILESTONE_DELETE",
						R.string.menuDeleteText,
						R.drawable.ic_delete,
						R.attr.colorErrorContainer,
						R.attr.colorOnErrorContainer));

		items.add(
				new GenericMenuItemModel(
						"MILESTONE_SHARE",
						R.string.share_location,
						R.drawable.ic_share,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		GenericMenuBottomSheet sheet =
				GenericMenuBottomSheet.newInstance(milestone.getTitle(), null, items);

		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "MILESTONE_CLOSE", "MILESTONE_REOPEN":
							showCloseConfirmation(milestone);
							break;
						case "MILESTONE_EDIT":
							CreateMilestoneBottomSheet.newInstance(repository, milestone)
									.show(getChildFragmentManager(), "EDIT_MILESTONE");
							break;
						case "MILESTONE_DELETE":
							showDeleteConfirmation(milestone);
							break;
						case "MILESTONE_SHARE":
							String shareUrl =
									UrlHelper.buildCurrentContextUrl(
											requireContext(),
											repository.getOwner(),
											repository.getName(),
											"milestone",
											String.valueOf(milestone.getId()));
							AppUtil.sharingIntent(requireContext(), shareUrl);
							break;
					}
				});

		sheet.show(getChildFragmentManager(), "MILESTONE_MENU");
	}

	private void showDeleteConfirmation(Milestone milestone) {
		new MaterialAlertDialogBuilder(requireContext())
				.setTitle(R.string.delete_milestone)
				.setMessage(getString(R.string.milestone_delete_confirm_text, milestone.getTitle()))
				.setPositiveButton(
						R.string.menuDeleteText,
						(dialog, which) -> {
							viewModel.deleteMilestone(
									requireContext(),
									repository.getOwner(),
									repository.getName(),
									milestone.getId(),
									milestone);
						})
				.setNegativeButton(R.string.cancelButton, null)
				.show();
	}

	private void showCloseConfirmation(Milestone milestone) {
		boolean isOpen = milestone.getState() == Milestone.StateEnum.OPEN;
		int titleRes = isOpen ? R.string.closeMilestone : R.string.openMilestone;
		int msgRes = isOpen ? R.string.close_milestone_msg : R.string.open_milestone_msg;

		new MaterialAlertDialogBuilder(requireContext())
				.setTitle(titleRes)
				.setMessage(getString(msgRes, milestone.getTitle()))
				.setPositiveButton(
						isOpen ? R.string.close : R.string.isOpen,
						(dialog, which) -> {
							EditMilestoneOption.StateEnum newState =
									isOpen
											? EditMilestoneOption.StateEnum.CLOSED
											: EditMilestoneOption.StateEnum.OPEN;

							viewModel.toggleMilestoneState(
									requireContext(),
									repository.getOwner(),
									repository.getName(),
									milestone,
									newState);
						})
				.setNegativeButton(R.string.cancelButton, null)
				.show();
	}

	private void updateUiVisibility(boolean isLoading) {
		boolean hasData = adapter != null && adapter.getItemCount() > 0;
		boolean hasLoadedOnce = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);

		if (isLoading) {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		} else {
			boolean showEmpty = !hasData && hasLoadedOnce;
			binding.layoutEmpty.getRoot().setVisibility(showEmpty ? View.VISIBLE : View.GONE);
		}

		binding.recyclerView.setVisibility(hasData ? View.VISIBLE : View.GONE);
	}

	private void scrollToMilestone(List<Milestone> list) {
		try {
			int id = Integer.parseInt(milestoneIdToScroll);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getId() == id) {
					binding.recyclerView.scrollToPosition(i);
					milestoneIdToScroll = null;
					break;
				}
			}
		} catch (NumberFormatException ignored) {
		}
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchMilestones(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				currentState,
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

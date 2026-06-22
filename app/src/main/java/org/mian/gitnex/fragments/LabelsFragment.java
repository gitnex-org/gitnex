package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.LabelsAdapter;
import org.mian.gitnex.bottomsheets.CreateLabelBottomSheet;
import org.mian.gitnex.bottomsheets.GenericMenuBottomSheet;
import org.mian.gitnex.databinding.FragmentLabelsBinding;
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
import org.mian.gitnex.viewmodels.LabelsViewModel;

/**
 * @author mmarif
 */
public class LabelsFragment extends Fragment implements RepoDetailActivity.RepoHubProvider {

	private FragmentLabelsBinding binding;
	private LabelsViewModel viewModel;
	private LabelsAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private RepositoryContext repository;
	private final String type = "repo";
	private int resultLimit;
	private boolean isSearching = false;
	private boolean isFirstLoad = true;

	public static LabelsFragment newInstance(RepositoryContext repository) {
		LabelsFragment fragment = new LabelsFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
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
		binding = FragmentLabelsBinding.inflate(inflater, container, false);

		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(requireActivity()).get(LabelsViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupSearch();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.recyclerView, binding.pullToRefresh, null);
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
						"labels",
						null);

		items.add(
				new RepositoryMenuItemModel(
						"LABEL_SEARCH",
						R.string.search,
						R.drawable.ic_search,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		if (repository.getPermissions().isAdmin() && !repository.getRepository().isArchived()) {
			items.add(
					new RepositoryMenuItemModel(
							"LABEL_ADD_NEW",
							R.string.pageTitleCreateLabel,
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
			case "LABEL_ADD_NEW":
				CreateLabelBottomSheet sheet =
						CreateLabelBottomSheet.newInstance(
								"repo", repository.getOwner(), repository.getName(), null);
				sheet.show(getChildFragmentManager(), "CreateLabelSheet");
				break;
			case "LABEL_SEARCH":
				binding.searchView.show();
				break;
			case "BOOKMARK_TAB":
				BookmarkHelper.toggleBookmark(
						requireContext(),
						"repo",
						repository.getOwner(),
						repository.getName(),
						"labels",
						null,
						null,
						getString(R.string.newIssueLabelsTitle),
						UrlHelper.buildCurrentContextUrl(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								"labels"));
				break;
			case "CONTEXT_SHARE":
				String url =
						UrlHelper.buildCurrentContextUrl(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								"labels");
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
						if (!isSearching) {
							viewModel.fetchLabels(
									requireContext(),
									repository.getOwner(),
									repository.getName(),
									type,
									page,
									resultLimit,
									false);
						}
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getLabels()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (adapter == null) {
								boolean canEdit =
										repository.getPermissions().isPush()
												&& !repository.getRepository().isArchived();

								adapter =
										new LabelsAdapter(
												requireContext(),
												list,
												canEdit,
												this::showLabelMenu);
								binding.recyclerView.setAdapter(adapter);
								binding.searchResultsRecycler.setAdapter(adapter);
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
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});

		viewModel
				.getActionResult()
				.observe(
						getViewLifecycleOwner(),
						code -> {
							if (code == 200 || code == 201 || code == 204) {
								int messageRes;
								if (code == 201) {
									messageRes = R.string.labelCreated;
								} else if (code == 200) {
									messageRes = R.string.labelUpdated;
								} else {
									messageRes = R.string.labelDeleteText;
								}
								Toasty.show(requireContext(), messageRes);

								refreshData();
								new Handler(Looper.getMainLooper())
										.postDelayed(
												() -> {
													if (isAdded()) {
														viewModel.resetActionResult();
													}
												},
												100);
							}
						});
	}

	private void showLabelMenu(Label label) {
		List<GenericMenuItemModel> items = new ArrayList<>();

		items.add(
				new GenericMenuItemModel(
						"LABEL_EDIT",
						R.string.menuEditText,
						R.drawable.ic_edit,
						R.attr.colorPrimaryContainer,
						R.attr.colorOnPrimaryContainer));

		items.add(
				new GenericMenuItemModel(
						"LABEL_DELETE",
						R.string.menuDeleteText,
						R.drawable.ic_delete,
						R.attr.colorErrorContainer,
						R.attr.colorOnErrorContainer));

		GenericMenuBottomSheet sheet =
				GenericMenuBottomSheet.newInstance(label.getName(), null, items);

		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "LABEL_EDIT":
							CreateLabelBottomSheet.newInstance(
											type,
											repository.getOwner(),
											repository.getName(),
											label)
									.show(getChildFragmentManager(), "EditLabel");
							break;
						case "LABEL_DELETE":
							new MaterialAlertDialogBuilder(requireContext())
									.setTitle(R.string.labelDeleteTitle)
									.setMessage(
											getString(
													R.string.labelDeleteConfirmText,
													label.getName()))
									.setPositiveButton(
											R.string.menuDeleteText,
											(d, w) ->
													viewModel.deleteLabel(
															requireContext(),
															type,
															repository.getOwner(),
															repository.getName(),
															label.getId()))
									.setNegativeButton(R.string.cancelButton, null)
									.show();
							break;
					}
				});

		sheet.show(getChildFragmentManager(), "LABEL_MENU");
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

	private void setupSearch() {
		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								String query = s.toString();
								isSearching = !query.isEmpty();
								if (adapter != null) {
									adapter.getFilter().filter(query);
								}
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchLabels(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				type,
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

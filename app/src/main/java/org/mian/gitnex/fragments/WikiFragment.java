package org.mian.gitnex.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gitnex.tea4j.v2.models.WikiPageMetaData;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.WikiListAdapter;
import org.mian.gitnex.bottomsheets.ContentViewerBottomSheet;
import org.mian.gitnex.bottomsheets.CreateWikiBottomSheet;
import org.mian.gitnex.bottomsheets.GenericMenuBottomSheet;
import org.mian.gitnex.databinding.FragmentWikiBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.BookmarkHelper;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.TokenAuthorizationDialog;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.GenericMenuItemModel;
import org.mian.gitnex.models.RepositoryMenuItemModel;
import org.mian.gitnex.viewmodels.WikiViewModel;

/**
 * @author mmarif
 */
public class WikiFragment extends Fragment implements RepoDetailActivity.RepoHubProvider {

	private FragmentWikiBinding binding;
	private WikiViewModel viewModel;
	private WikiListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private RepositoryContext repository;
	private int resultLimit;
	private boolean isFirstLoad = true;
	private String pendingPageName = null;
	private boolean isPendingEdit = false;

	public static WikiFragment newInstance(RepositoryContext repository) {
		WikiFragment fragment = new WikiFragment();
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentWikiBinding.inflate(inflater, container, false);

		viewModel = new ViewModelProvider(requireActivity()).get(WikiViewModel.class);
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
						"wiki",
						null);

		if (repository.getPermissions().isAdmin() && !repository.getRepository().isArchived()) {
			items.add(
					new RepositoryMenuItemModel(
							"WIKI_ADD_NEW",
							R.string.createWikiPage,
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
			case "WIKI_ADD_NEW":
				CreateWikiBottomSheet.newInstance(repository, null)
						.show(getParentFragmentManager(), "CREATE_WIKI");
				break;
			case "BOOKMARK_TAB":
				BookmarkHelper.toggleBookmark(
						requireContext(),
						"repo",
						repository.getOwner(),
						repository.getName(),
						"wiki",
						null,
						null,
						getString(R.string.wiki),
						UrlHelper.buildCurrentContextUrl(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								"wiki"));
				break;
			case "CONTEXT_SHARE":
				String url =
						UrlHelper.buildCurrentContextUrl(
								requireContext(),
								repository.getOwner(),
								repository.getName(),
								"wiki");
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
						viewModel.fetchWikiPages(
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
				.getWikiPages()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (adapter == null) {
								boolean canEdit =
										repository.getPermissions().isPush()
												&& !repository.getRepository().isArchived();

								adapter =
										new WikiListAdapter(
												requireContext(),
												list,
												canEdit,
												wikiPage -> openWiki(wikiPage, null), // click
												wikiPage -> openWiki(wikiPage, "edit"), // edit
												this::showDeleteDialog, // delete
												this::showWikiMenu // menu
												);
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
				.getActionResult()
				.observe(
						getViewLifecycleOwner(),
						code -> {
							if (code == null || code == -1) return;

							if (code == 204) {
								Toasty.show(requireContext(), R.string.wikiPageDeleted);
								refreshData();
							} else if (code == 200 || code == 201) {
								refreshData();
							} else {
								Toasty.show(requireContext(), R.string.genericError);
							}

							viewModel.resetActionResult();
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						err -> {
							if (err != null) Toasty.show(requireContext(), err);
						});

		viewModel
				.getIsLoadingPage()
				.observe(
						getViewLifecycleOwner(),
						isLoading -> {
							binding.expressiveLoader.setVisibility(VISIBLE);
						});

		viewModel
				.getPageContent()
				.observe(
						getViewLifecycleOwner(),
						wikiPage -> {
							if (wikiPage != null && pendingPageName != null) {
								if (isPendingEdit) {
									CreateWikiBottomSheet.newInstance(repository, wikiPage)
											.show(getParentFragmentManager(), "EDIT_WIKI");
								} else {
									String decodedContent =
											AppUtil.decodeBase64(wikiPage.getContentBase64());
									showContentViewer(
											wikiPage.getTitle(),
											decodedContent,
											wikiPage.getHtmlUrl());
								}
								pendingPageName = null;
								viewModel.clearPageContent();
								binding.expressiveLoader.setVisibility(GONE);
							}
						});

		viewModel
				.getPageError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								if (error.equals("UNAUTHORIZED")) {
									TokenAuthorizationDialog.authorizationTokenRevokedDialog(
											requireContext());
								} else {
									Toasty.show(requireContext(), error);
								}
								pendingPageName = null;
								viewModel.clearPageError();
							}
						});
	}

	private void openWiki(WikiPageMetaData wikiPage, String action) {
		if (action != null && action.equals("edit")) {
			pendingPageName = wikiPage.getSubUrl();
			isPendingEdit = true;
			viewModel.fetchWikiPageContent(
					requireContext(),
					repository.getOwner(),
					repository.getName(),
					wikiPage.getSubUrl());
		} else {
			pendingPageName = wikiPage.getTitle();
			isPendingEdit = false;
			viewModel.fetchWikiPageContent(
					requireContext(),
					repository.getOwner(),
					repository.getName(),
					wikiPage.getSubUrl());
		}
	}

	private void showContentViewer(String title, String content, String url) {
		Map<String, String> metadata = new HashMap<>();
		if (url != null) {
			metadata.put("URL", url);
		}
		ContentViewerBottomSheet.newInstance(
						content,
						title,
						repository,
						null,
						metadata,
						ContentViewerBottomSheet.Feature.ALLOW_COPY,
						ContentViewerBottomSheet.Feature.ALLOW_SHARE,
						ContentViewerBottomSheet.Feature.MARKDOWN_PREVIEW,
						ContentViewerBottomSheet.Feature.START_IN_MARKDOWN,
						ContentViewerBottomSheet.Feature.SHOW_TITLE)
				.show(getParentFragmentManager(), "WIKI_VIEWER");
	}

	private void showDeleteDialog(WikiPageMetaData wikiPage) {
		new MaterialAlertDialogBuilder(requireContext())
				.setTitle(
						String.format(getString(R.string.deleteGenericTitle), wikiPage.getTitle()))
				.setMessage(getString(R.string.deleteWikiPageMessage, wikiPage.getTitle()))
				.setPositiveButton(
						R.string.menuDeleteText,
						(d, w) -> {
							viewModel.deleteWikiPage(
									requireContext(),
									repository.getOwner(),
									repository.getName(),
									wikiPage.getSubUrl());
						})
				.setNegativeButton(R.string.cancelButton, null)
				.show();
	}

	private void showWikiMenu(WikiPageMetaData wikiPage) {
		List<GenericMenuItemModel> items = new ArrayList<>();

		boolean isPageBookmarked =
				BookmarkHelper.isBookmarked(
						requireContext(),
						"repo",
						repository.getOwner(),
						repository.getName(),
						"wiki_page",
						wikiPage.getSubUrl());

		items.add(
				new GenericMenuItemModel(
						"WIKI_EDIT",
						R.string.editWikiPage,
						R.drawable.ic_edit,
						R.attr.colorPrimaryContainer,
						R.attr.colorOnPrimaryContainer));

		items.add(
				new GenericMenuItemModel(
						"WIKI_DELETE",
						R.string.menuDeleteText,
						R.drawable.ic_delete,
						R.attr.colorErrorContainer,
						R.attr.colorOnErrorContainer));

		items.add(
				new GenericMenuItemModel(
						"WIKI_BOOKMARK",
						isPageBookmarked ? R.string.bookmark_remove : R.string.bookmark_add,
						isPageBookmarked
								? R.drawable.ic_bookmark_remove
								: R.drawable.ic_bookmark_add,
						isPageBookmarked ? R.attr.colorErrorContainer : R.attr.colorPrimarySurface,
						isPageBookmarked
								? R.attr.colorOnErrorContainer
								: R.attr.colorOnPrimarySurface));

		items.add(
				new GenericMenuItemModel(
						"WIKI_SHARE",
						R.string.share_location,
						R.drawable.ic_share,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		GenericMenuBottomSheet sheet =
				GenericMenuBottomSheet.newInstance(wikiPage.getTitle(), null, items);

		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "WIKI_EDIT":
							openWiki(wikiPage, "edit");
							break;
						case "WIKI_DELETE":
							showDeleteDialog(wikiPage);
							break;
						case "WIKI_BOOKMARK":
							BookmarkHelper.toggleBookmark(
									requireContext(),
									"repo",
									repository.getOwner(),
									repository.getName(),
									"wiki_page",
									wikiPage.getSubUrl(),
									null,
									wikiPage.getTitle(),
									UrlHelper.buildCurrentContextUrl(
											requireContext(),
											repository.getOwner(),
											repository.getName(),
											"wiki",
											wikiPage.getSubUrl()));
							break;
						case "WIKI_SHARE":
							String shareUrl =
									UrlHelper.buildCurrentContextUrl(
											requireContext(),
											repository.getOwner(),
											repository.getName(),
											"wiki",
											wikiPage.getSubUrl());
							AppUtil.sharingIntent(requireContext(), shareUrl);
							break;
					}
				});

		sheet.show(getChildFragmentManager(), "WIKI_MENU");
	}

	public void openWikiPageByName(String pageName) {
		pendingPageName = pageName;
		isPendingEdit = false;
		viewModel.fetchWikiPageContent(
				requireContext(), repository.getOwner(), repository.getName(), pageName);
	}

	private void updateUiVisibility(boolean isLoading) {
		boolean hasData = adapter != null && adapter.getItemCount() > 0;
		boolean hasLoadedOnce = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? VISIBLE : GONE);

		if (isLoading) {
			binding.layoutEmpty.getRoot().setVisibility(GONE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(!hasData && hasLoadedOnce ? VISIBLE : GONE);
		}
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchWikiPages(
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

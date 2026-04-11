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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.WikiPageMetaData;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.WikiActivity;
import org.mian.gitnex.adapters.WikiListAdapter;
import org.mian.gitnex.databinding.BottomsheetWikiItemMenuBinding;
import org.mian.gitnex.databinding.FragmentWikiBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
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

	public static WikiFragment newInstance(RepositoryContext repository) {
		WikiFragment fragment = new WikiFragment();
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
		binding = FragmentWikiBinding.inflate(inflater, container, false);

		viewModel = new ViewModelProvider(this).get(WikiViewModel.class);
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
							"WIKI_ADD_NEW",
							R.string.addButton,
							R.drawable.ic_add,
							R.attr.colorPrimaryContainer,
							R.attr.colorOnPrimaryContainer));
		}

		return items;
	}

	@Override
	public void onHubActionSelected(String actionId) {
		if (actionId.equals("WIKI_ADD_NEW")) {
			Intent intent = new Intent(getContext(), WikiActivity.class);
			intent.putExtra("action", "add");
			intent.putExtra(RepositoryContext.INTENT_EXTRA, repository);
			startActivity(intent);
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
							if (code == 204) {
								Toasty.show(requireContext(), R.string.wikiPageDeleted);
								viewModel.resetActionResult();
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						err -> {
							if (err != null) Toasty.show(requireContext(), err);
						});
	}

	private void openWiki(WikiPageMetaData wikiPage, String action) {
		Intent intent = new Intent(requireContext(), WikiActivity.class);
		intent.putExtra("pageName", wikiPage.getTitle());
		if (action != null) intent.putExtra("action", action);
		intent.putExtra(RepositoryContext.INTENT_EXTRA, repository);
		startActivity(intent);
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
									wikiPage.getTitle());
						})
				.setNegativeButton(R.string.cancelButton, null)
				.show();
	}

	private void showWikiMenu(WikiPageMetaData wikiPage) {
		BottomsheetWikiItemMenuBinding sheetB =
				BottomsheetWikiItemMenuBinding.inflate(getLayoutInflater());
		BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
		dialog.setContentView(sheetB.getRoot());

		AppUtil.applySheetStyle(dialog, true);

		sheetB.sheetTitle.setText(wikiPage.getTitle());

		sheetB.editWiki.setOnClickListener(
				v1 -> {
					openWiki(wikiPage, "edit");
					dialog.dismiss();
				});

		sheetB.deleteWiki.setOnClickListener(
				v1 -> {
					showDeleteDialog(wikiPage);
					dialog.dismiss();
				});
		dialog.show();
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

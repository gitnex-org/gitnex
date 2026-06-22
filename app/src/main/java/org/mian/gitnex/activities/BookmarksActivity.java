package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.BookmarksAdapter;
import org.mian.gitnex.bottomsheets.BookmarkSearchBottomSheet;
import org.mian.gitnex.database.models.Bookmark;
import org.mian.gitnex.databinding.ActivityBookmarksBinding;
import org.mian.gitnex.helpers.BookmarkHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.BookmarksViewModel;

/**
 * @author mmarif
 */
public class BookmarksActivity extends BaseActivity implements BookmarksAdapter.OnBookmarkAction {

	private ActivityBookmarksBinding binding;
	private BookmarksViewModel viewModel;
	private BookmarksAdapter adapter;

	private String searchQuery = "";
	private String searchType = null;
	private int searchChipIndex = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityBookmarksBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.bookmarksList, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(BookmarksViewModel.class);

		setupListeners();
		observeViewModel();
		loadBookmarks();
	}

	private void setupListeners() {
		binding.btnBack.setOnClickListener(v -> finish());

		binding.btnSearch.setOnClickListener(
				v -> {
					BookmarkSearchBottomSheet searchSheet =
							BookmarkSearchBottomSheet.newInstance(
									searchQuery, searchType, searchChipIndex);
					searchSheet.setOnSearchListener(
							(query, type) -> {
								searchQuery = query;
								searchType = type;
								searchChipIndex = getChipIndex(type);
								viewModel.searchBookmarks(
										this,
										getAccount().getAccount().getAccountId(),
										query,
										type);
							});
					searchSheet.show(getSupportFragmentManager(), "BOOKMARK_SEARCH");
				});

		binding.btnDeleteAll.setOnClickListener(
				v -> {
					new MaterialAlertDialogBuilder(this)
							.setTitle(R.string.deleteAllBookmarks)
							.setMessage(R.string.deleteAllBookmarksConfirm)
							.setPositiveButton(
									R.string.menuDeleteText,
									(d, w) -> {
										int accountId = getAccount().getAccount().getAccountId();
										viewModel.deleteAll(this, accountId);
										loadBookmarks();
									})
							.setNeutralButton(R.string.cancelButton, null)
							.show();
				});

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					loadBookmarks();
					binding.pullToRefresh.setRefreshing(false);
				});
	}

	private int getChipIndex(String type) {
		if (type == null) return 0;
		return switch (type) {
			case "repo" -> 1;
			case "issue" -> 2;
			case "pr" -> 3;
			case "profile" -> 4;
			case "org" -> 5;
			case "wiki" -> 6;
			default -> 0;
		};
	}

	private void observeViewModel() {
		viewModel
				.getBookmarks()
				.observe(
						this,
						list -> {
							if (list == null) return;

							if (adapter == null) {
								binding.bookmarksList.setLayoutManager(
										new androidx.recyclerview.widget.LinearLayoutManager(this));

								adapter = new BookmarksAdapter(this, list, this);
								binding.bookmarksList.setAdapter(adapter);
							} else {
								adapter.updateList(list);
							}

							boolean isEmpty = list.isEmpty();
							binding.layoutEmpty
									.getRoot()
									.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
							binding.bookmarksList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
						});

		viewModel
				.getIsLoading()
				.observe(
						this,
						loading ->
								binding.expressiveLoader.setVisibility(
										loading ? View.VISIBLE : View.GONE));
	}

	private void loadBookmarks() {
		int accountId = getAccount().getAccount().getAccountId();
		viewModel.loadBookmarks(this, accountId);
	}

	@Override
	public void onOpen(Bookmark bookmark) {
		BookmarkHelper.openBookmark(this, bookmark);
	}

	@Override
	public void onRemove(Bookmark bookmark) {
		viewModel.deleteBookmark(this, bookmark.getBookmarkId());
		loadBookmarks();
		Toasty.show(this, getString(R.string.bookmark_removed));
	}
}

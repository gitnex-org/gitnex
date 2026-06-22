package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.BookmarksApi;
import org.mian.gitnex.database.models.Bookmark;

/**
 * @author mmarif
 */
public class BookmarksViewModel extends ViewModel {

	private final MutableLiveData<List<Bookmark>> bookmarks = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private BookmarksApi api;

	public LiveData<List<Bookmark>> getBookmarks() {
		return bookmarks;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public void loadBookmarks(Context context, int accountId) {
		if (api == null) api = BaseApi.getInstance(context, BookmarksApi.class);
		if (api == null) return;

		isLoading.setValue(true);
		api.getAll(accountId)
				.observeForever(
						data -> {
							bookmarks.setValue(data);
							isLoading.setValue(false);
						});
	}

	public void deleteBookmark(Context context, int bookmarkId) {
		if (api == null) api = BaseApi.getInstance(context, BookmarksApi.class);
		if (api == null) return;
		api.delete(bookmarkId);
	}

	public void deleteAll(Context context, int accountId) {
		if (api == null) api = BaseApi.getInstance(context, BookmarksApi.class);
		if (api == null) return;
		api.deleteAll(accountId);
	}

	public void searchBookmarks(Context context, int accountId, String query, String type) {
		if (api == null) api = BaseApi.getInstance(context, BookmarksApi.class);
		if (api == null) return;

		if (type != null && !type.isEmpty()) {
			if ("wiki".equals(type)) {
				api.getByType(accountId, "repo")
						.observeForever(
								data -> {
									if (data != null) {
										List<Bookmark> filtered = new ArrayList<>();
										for (Bookmark b : data) {
											if ("wiki".equals(b.getBookmarkAction())
													|| "wiki_page".equals(b.getBookmarkAction())) {
												if (query == null
														|| query.isEmpty()
														|| b.getTitle()
																.toLowerCase()
																.contains(query.toLowerCase())) {
													filtered.add(b);
												}
											}
										}
										bookmarks.setValue(filtered);
									}
								});
				return;
			}

			if (query != null && !query.isEmpty()) {
				api.search(accountId, query)
						.observeForever(
								data -> {
									if (data != null) {
										List<Bookmark> filtered = new ArrayList<>();
										for (Bookmark b : data) {
											if (type.equals(b.getType())) {
												filtered.add(b);
											}
										}
										bookmarks.setValue(filtered);
									}
								});
			} else {
				api.getByType(accountId, type).observeForever(bookmarks::setValue);
			}
		} else if (query != null && !query.isEmpty()) {
			api.search(accountId, query).observeForever(bookmarks::setValue);
		} else {
			api.getAll(accountId).observeForever(bookmarks::setValue);
		}
	}
}

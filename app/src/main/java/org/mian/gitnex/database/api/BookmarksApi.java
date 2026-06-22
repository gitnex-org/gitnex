package org.mian.gitnex.database.api;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import org.mian.gitnex.database.dao.BookmarksDao;
import org.mian.gitnex.database.models.Bookmark;

/**
 * @author mmarif
 */
public class BookmarksApi extends BaseApi {

	private final BookmarksDao bookmarksDao;

	BookmarksApi(Context context) {
		super(context);
		bookmarksDao = gitnexDatabase.bookmarksDao();
	}

	public long insert(Bookmark bookmark) {
		bookmark.setCreatedAt(System.currentTimeMillis());
		return bookmarksDao.insert(bookmark);
	}

	public void update(int bookmarkId, String title, String url) {
		executorService.execute(() -> bookmarksDao.update(bookmarkId, title, url));
	}

	public void delete(int bookmarkId) {
		executorService.execute(() -> bookmarksDao.delete(bookmarkId));
	}

	public void deleteAll(int accountId) {
		executorService.execute(() -> bookmarksDao.deleteAll(accountId));
	}

	public Bookmark findByKey(
			int accountId,
			String type,
			String owner,
			String bookmarkAction,
			String identifier,
			String repo) {
		String cleanType = type != null ? type : "";
		String cleanOwner = owner != null ? owner : "";
		String cleanAction = bookmarkAction != null ? bookmarkAction : "";
		String cleanId = identifier != null ? identifier : "";
		String cleanRepo = repo != null ? repo : "";

		return bookmarksDao.findSpecificBookmark(
				accountId, cleanType, cleanOwner, cleanAction, cleanId, cleanRepo);
	}

	public LiveData<List<Bookmark>> getAll(int accountId) {
		return bookmarksDao.getAll(accountId);
	}

	public LiveData<List<Bookmark>> search(int accountId, String query) {
		return bookmarksDao.search(accountId, query);
	}

	public LiveData<List<Bookmark>> getByType(int accountId, String type) {
		return bookmarksDao.getByType(accountId, type);
	}
}

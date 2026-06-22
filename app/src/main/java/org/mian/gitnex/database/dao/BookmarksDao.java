package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import org.mian.gitnex.database.models.Bookmark;

/**
 * @author mmarif
 */
@Dao
public interface BookmarksDao {

	@Insert
	long insert(Bookmark bookmark);

	@Query("UPDATE bookmarks SET title = :title, url = :url WHERE bookmarkId = :bookmarkId")
	void update(int bookmarkId, String title, String url);

	@Query("DELETE FROM bookmarks WHERE bookmarkId = :bookmarkId")
	void delete(int bookmarkId);

	@Query("DELETE FROM bookmarks WHERE accountId = :accountId")
	void deleteAll(int accountId);

	@Query(
			"SELECT * FROM bookmarks WHERE accountId = :accountId "
					+ "AND type = :type "
					+ "AND owner = :owner "
					+ "AND bookmarkAction = :bookmarkAction "
					+ "AND identifier = :identifier "
					+ "AND repo = :repo LIMIT 1")
	Bookmark findSpecificBookmark(
			int accountId,
			String type,
			String owner,
			String bookmarkAction,
			String identifier,
			String repo);

	@Query("SELECT * FROM bookmarks WHERE accountId = :accountId ORDER BY createdAt DESC")
	LiveData<List<Bookmark>> getAll(int accountId);

	@Query(
			"SELECT * FROM bookmarks WHERE accountId = :accountId AND (title LIKE '%' || :query || '%' OR owner LIKE '%' || :query || '%' OR repo LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%') ORDER BY createdAt DESC")
	LiveData<List<Bookmark>> search(int accountId, String query);

	@Query(
			"SELECT * FROM bookmarks WHERE accountId = :accountId AND type = :type ORDER BY createdAt DESC")
	LiveData<List<Bookmark>> getByType(int accountId, String type);
}

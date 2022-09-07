package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import org.mian.gitnex.database.models.Draft;
import org.mian.gitnex.database.models.DraftWithRepository;
import java.util.List;

/**
 * @author M M Arif
 */

@Dao
public interface DraftsDao {

	@Insert
	long insertDraft(Draft drafts);

	@Query("SELECT * FROM Drafts JOIN Repositories ON Repositories.repositoryId = Drafts.draftRepositoryId WHERE draftAccountId = :accountId" + " ORDER BY " + "draftId DESC")
	LiveData<List<DraftWithRepository>> fetchAllDrafts(int accountId);

	@Query("SELECT * FROM Drafts WHERE draftAccountId = :accountId ORDER BY draftId DESC")
	LiveData<List<Draft>> fetchDrafts(int accountId);

	@Query("SELECT * FROM Drafts WHERE draftAccountId = :accountId and draftRepositoryId = :repositoryId")
	LiveData<Draft> fetchSingleDraftByAccountIdAndRepositoryId(int accountId, int repositoryId);

	@Query("SELECT * FROM Drafts WHERE draftId = :draftId")
	LiveData<Draft> fetchDraftById(int draftId);

	@Query("SELECT * FROM Drafts WHERE issueId = :issueId")
	LiveData<Draft> fetchDraftByIssueId(int issueId);

	@Query("SELECT count(draftId) FROM Drafts WHERE issueId = :issueId AND draftRepositoryId = :draftRepositoryId AND commentId = :commentId")
	Integer checkDraftDao(int issueId, int draftRepositoryId, String commentId);

	@Query("UPDATE Drafts SET draftText = :draftText, commentId = :commentId WHERE draftId = :draftId")
	void updateDraft(String draftText, int draftId, String commentId);

	@Query("UPDATE Drafts SET draftText = :draftText WHERE issueId = :issueId AND draftRepositoryId = :draftRepositoryId AND commentId = :commentId")
	void updateDraftByIssueId(String draftText, int issueId, int draftRepositoryId, String commentId);

	@Query("SELECT draftId FROM Drafts WHERE issueId = :issueId AND draftRepositoryId = :draftRepositoryId")
	Integer getDraftId(int issueId, int draftRepositoryId);

	@Query("DELETE FROM Drafts WHERE draftId = :draftId")
	void deleteByDraftId(int draftId);

	@Query("DELETE FROM Drafts WHERE draftAccountId = :accountId")
	void deleteAllDrafts(int accountId);

}

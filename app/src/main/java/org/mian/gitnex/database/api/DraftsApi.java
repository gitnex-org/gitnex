package org.mian.gitnex.database.api;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import org.mian.gitnex.database.dao.DraftsDao;
import org.mian.gitnex.database.models.Draft;
import org.mian.gitnex.database.models.DraftWithRepository;

/**
 * @author M M Arif
 */
public class DraftsApi extends BaseApi {

	private final DraftsDao draftsDao;

	DraftsApi(Context context) {
		super(context);
		draftsDao = gitnexDatabase.draftsDao();
	}

	public long insertDraft(
			int repositoryId,
			int draftAccountId,
			int issueId,
			String draftText,
			String draftType,
			String commentId,
			String issueType) {

		Draft draft = new Draft();
		draft.setDraftRepositoryId(repositoryId);
		draft.setDraftAccountId(draftAccountId);
		draft.setIssueId(issueId);
		draft.setDraftText(draftText);
		draft.setDraftType(draftType);
		draft.setCommentId(commentId);
		draft.setIssueType(issueType);

		return insertDraftAsyncTask(draft);
	}

	private long insertDraftAsyncTask(final Draft draft) {
		return draftsDao.insertDraft(draft);
	}

	public long getDraftIdAsync(int issueId, int draftRepositoryId) {
		return draftsDao.getDraftId(issueId, draftRepositoryId);
	}

	public Integer checkDraft(int issueId, int draftRepositoryId, String commentId) {
		return draftsDao.checkDraftDao(issueId, draftRepositoryId, commentId);
	}

	public LiveData<List<DraftWithRepository>> getDrafts(int accountId) {
		return draftsDao.fetchAllDrafts(accountId);
	}

	public LiveData<Draft> getDraftByIssueId(int issueId) {
		return draftsDao.fetchDraftByIssueId(issueId);
	}

	public void deleteSingleDraft(final int draftId) {
		final LiveData<Draft> draft = draftsDao.fetchDraftById(draftId);

		if (draft != null) {
			executorService.execute(() -> draftsDao.deleteByDraftId(draftId));
		}
	}

	public void deleteAllDrafts(final int accountId) {
		executorService.execute(() -> draftsDao.deleteAllDrafts(accountId));
	}

	public void updateDraft(final String draftText, final int draftId, final String commentId) {
		executorService.execute(() -> draftsDao.updateDraft(draftText, draftId, commentId));
	}

	public void updateDraftByIssueIdAsyncTask(
			final String draftText,
			final int issueId,
			final int draftRepositoryId,
			final String commentId) {
		executorService.execute(
				() ->
						draftsDao.updateDraftByIssueId(
								draftText, issueId, draftRepositoryId, commentId));
	}
}

package org.mian.gitnex.database.api;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.DraftsDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.Draft;
import org.mian.gitnex.database.models.DraftWithRepository;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import java.util.List;

/**
 * Author M M Arif
 */

public class DraftsApi {

	private static DraftsDao draftsDao;
	private static long draftId;
	private static Integer checkDraftFlag;

	public DraftsApi(Context context) {

		GitnexDatabase db;
		db = GitnexDatabase.getDatabaseInstance(context);
		draftsDao = db.draftsDao();
	}

	public long insertDraft(int repositoryId, int draftAccountId, int issueId, String draftText, String draftType, String commentId) {

		Draft draft = new Draft();
		draft.setDraftRepositoryId(repositoryId);
		draft.setDraftAccountId(draftAccountId);
		draft.setIssueId(issueId);
		draft.setDraftText(draftText);
		draft.setDraftType(draftType);
		draft.setCommentId(draftType);

		return insertDraftAsyncTask(draft);
	}

	private static long insertDraftAsyncTask(final Draft draft) {

		try {

			Thread thread = new Thread(() -> draftId = draftsDao.insertDraft(draft));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.draftsRepository, e.toString());
		}

		return draftId;
	}

	public long getDraftIdAsync(int issueId, int draftRepositoryId) {

		try {

			Thread thread = new Thread(() -> draftId = draftsDao.getDraftId(issueId, draftRepositoryId));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.draftsRepository, e.toString());
		}

		return draftId;
	}

	public Integer checkDraft(int issueId, int draftRepositoryId, String commentId) {

		try {

			Thread thread = new Thread(() -> checkDraftFlag = draftsDao.checkDraftDao(issueId, draftRepositoryId, commentId));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.draftsRepository, e.toString());
		}

		return checkDraftFlag;
	}

	public LiveData<List<DraftWithRepository>> getDrafts(int accountId) {

		return draftsDao.fetchAllDrafts(accountId);
	}

	public LiveData<Draft> getDraftByIssueId(int issueId) {

		return draftsDao.fetchDraftByIssueId(issueId);
	}

	public void deleteSingleDraft(final int draftId) {

		final LiveData<Draft> draft = draftsDao.fetchDraftById(draftId);

		if(draft != null) {

			new Thread(() -> draftsDao.deleteByDraftId(draftId)).start();
		}
	}

	public static void deleteAllDrafts(final int accountId) {

		new Thread(() -> draftsDao.deleteAllDrafts(accountId)).start();
	}

	public static void updateDraft(final String draftText, final int draftId, final String commentId) {

		new Thread(() -> draftsDao.updateDraft(draftText, draftId, commentId)).start();
	}

	public static void updateDraftByIssueIdAsyncTask(final String draftText, final int issueId, final int draftRepositoryId, final String commentId) {

		new Thread(() -> draftsDao.updateDraftByIssueId(draftText, issueId, draftRepositoryId, commentId)).start();
	}

}

package org.mian.gitnex.database.api;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import org.mian.gitnex.database.dao.NotesDao;
import org.mian.gitnex.database.models.Notes;

/**
 * @author M M Arif
 */
public class NotesApi extends BaseApi {

	private final NotesDao notesDao;

	NotesApi(Context context) {
		super(context);
		notesDao = gitnexDatabase.notesDao();
	}

	public long insertNote(String content, Integer datetime) {

		Notes notes = new Notes();
		notes.setContent(content);
		notes.setDatetime(datetime);

		return insertNoteAsyncTask(notes);
	}

	public long insertNoteAsyncTask(Notes notes) {
		return notesDao.insertNote(notes);
	}

	public LiveData<List<Notes>> fetchAllNotes() {
		return notesDao.fetchAllNotes();
	}

	public Notes fetchNoteById(int noteId) {
		return notesDao.fetchNoteById(noteId);
	}

	public Integer fetchNotesCount() {
		return notesDao.fetchNotesCount();
	}

	public void updateNote(final String content, final long modified, int noteId) {
		executorService.execute(() -> notesDao.updateNote(content, modified, noteId));
	}

	public void deleteAllNotes() {
		executorService.execute(notesDao::deleteAllNotes);
	}

	public void deleteNote(final int noteId) {
		final Notes note = notesDao.fetchNoteById(noteId);

		if (note != null) {
			executorService.execute(() -> notesDao.deleteNote(noteId));
		}
	}
}

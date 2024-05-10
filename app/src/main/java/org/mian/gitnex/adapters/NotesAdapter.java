package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vdurmont.emoji.EmojiParser;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateIssueActivity;
import org.mian.gitnex.activities.CreateNoteActivity;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author M M Arif
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

	private List<Notes> notesList;
	private final Context ctx;
	private final Intent noteIntent;
	private final String insert;

	public NotesAdapter(Context ctx, List<Notes> notesListMain, String insert) {
		this.ctx = ctx;
		this.notesList = notesListMain;
		noteIntent = new Intent(ctx, CreateNoteActivity.class);
		this.insert = insert;
	}

	public class NotesViewHolder extends RecyclerView.ViewHolder {

		private Notes notes;

		private final TextView content;
		private final TextView datetime;

		private NotesViewHolder(View itemView) {

			super(itemView);

			content = itemView.findViewById(R.id.content);
			datetime = itemView.findViewById(R.id.datetime);
			ImageView deleteNote = itemView.findViewById(R.id.delete_note);

			itemView.setOnClickListener(
					view -> {
						noteIntent.putExtra("action", "edit");
						noteIntent.putExtra("noteId", notes.getNoteId());
						ctx.startActivity(noteIntent);
					});

			deleteNote.setOnClickListener(
					itemDelete -> {
						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(
										ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);

						materialAlertDialogBuilder
								.setTitle(ctx.getString(R.string.menuDeleteText))
								.setMessage(ctx.getString(R.string.noteDeleteDialogMessage))
								.setPositiveButton(
										R.string.menuDeleteText,
										(dialog, whichButton) ->
												deleteNote(
														getBindingAdapterPosition(),
														notes.getNoteId()))
								.setNeutralButton(R.string.cancelButton, null)
								.show();
					});

			if (insert.equalsIgnoreCase("insert")) {

				deleteNote.setVisibility(View.GONE);

				itemView.setOnClickListener(
						view -> {
							CreateIssueActivity parentActivity = (CreateIssueActivity) ctx;
							EditText text = parentActivity.findViewById(R.id.newIssueDescription);
							text.append(notes.getContent());

							parentActivity.dialogNotes.dismiss();
						});
			}
		}
	}

	private void deleteNote(int position, int noteId) {

		NotesApi notesApi = BaseApi.getInstance(ctx, NotesApi.class);
		assert notesApi != null;
		notesApi.deleteNote(noteId);
		notesList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, notesList.size());
		Toasty.success(ctx, ctx.getResources().getQuantityString(R.plurals.noteDeleteMessage, 1));
	}

	@NonNull @Override
	public NotesAdapter.NotesViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {
		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_notes, parent, false);
		return new NotesViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull NotesAdapter.NotesViewHolder holder, int position) {

		Locale locale = ctx.getResources().getConfiguration().locale;
		Notes currentItem = notesList.get(position);
		holder.notes = currentItem;

		Markdown.render(
				ctx,
				EmojiParser.parseToUnicode(
						Objects.requireNonNull(
								StringUtils.substring(currentItem.getContent(), 0, 140))),
				holder.content);

		if (currentItem.getModified() != null) {
			String modifiedTime =
					TimeHelper.formatTime(
							Date.from(Instant.ofEpochSecond(currentItem.getModified())), locale);
			holder.datetime.setText(
					ctx.getResources().getString(R.string.noteTimeModified, modifiedTime));
		} else {
			String createdTime =
					TimeHelper.formatTime(
							Date.from(Instant.ofEpochSecond(currentItem.getDatetime())), locale);
			holder.datetime.setText(
					ctx.getResources().getString(R.string.noteDateTime, createdTime));
		}
	}

	@Override
	public int getItemCount() {
		return notesList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
	}

	public void updateList(List<Notes> list) {

		notesList = list;
		notifyDataChanged();
	}
}

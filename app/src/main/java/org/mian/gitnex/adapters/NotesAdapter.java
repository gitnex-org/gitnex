package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.CreateNoteActivity;
import org.mian.gitnex.activities.CreatePullRequestActivity;
import org.mian.gitnex.activities.CreateReleaseActivity;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.ListNotesBinding;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

	private List<Notes> notesList;
	private final Context ctx;
	private final String insert;
	private OnItemClickListener itemClickListener;

	public interface OnItemClickListener {
		void onItemClick(Notes note);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.itemClickListener = listener;
	}

	public NotesAdapter(Context ctx, List<Notes> notesList, String insert, String source) {
		this.ctx = ctx;
		this.notesList = notesList;
		this.insert = insert;
	}

	public class NotesViewHolder extends RecyclerView.ViewHolder {
		private final ListNotesBinding binding;
		private Notes note;

		private NotesViewHolder(ListNotesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			setupClickListeners();
		}

		private void setupClickListeners() {
			binding.noteCard.setOnClickListener(
					v -> {
						if ("insert".equalsIgnoreCase(insert)) {
							if (itemClickListener != null) {
								itemClickListener.onItemClick(note);
							} else {
								performInsert();
							}
						} else {
							Intent intent = new Intent(ctx, CreateNoteActivity.class);
							intent.putExtra("action", "edit");
							intent.putExtra("noteId", note.getNoteId());
							ctx.startActivity(intent);
						}
					});

			binding.deleteNote.setOnClickListener(
					v -> {
						new MaterialAlertDialogBuilder(
										ctx, R.style.ThemeOverlay_Material3_Dialog_Alert)
								.setTitle(ctx.getString(R.string.menuDeleteText))
								.setMessage(ctx.getString(R.string.noteDeleteDialogMessage))
								.setPositiveButton(
										R.string.menuDeleteText,
										(dialog, which) ->
												deleteNote(
														getBindingAdapterPosition(),
														note.getNoteId()))
								.setNeutralButton(R.string.cancelButton, null)
								.show();
					});
		}

		private void performInsert() {

			if (!(ctx instanceof BaseActivity activity)) return;

			EditText targetField = null;
			AlertDialog dialogToDismiss = null;

			if (activity instanceof CreateReleaseActivity releaseAct) {
				targetField = releaseAct.findViewById(R.id.releaseContent);
				dialogToDismiss = releaseAct.dialogNotes;
			} else if (activity instanceof CreatePullRequestActivity prAct) {
				targetField = prAct.findViewById(R.id.prBody);
				dialogToDismiss = prAct.dialogNotes;
			}

			if (targetField != null) {
				targetField.append(note.getContent());

				if (dialogToDismiss != null && dialogToDismiss.isShowing()) {
					dialogToDismiss.dismiss();
				}
			}
		}

		public void bind(Notes note) {
			this.note = note;

			Markdown.render(
					ctx,
					EmojiParser.parseToUnicode(
							Objects.requireNonNull(
									StringUtils.substring(note.getContent(), 0, 140))),
					binding.noteContent);

			long timestamp = (note.getModified() != null) ? note.getModified() : note.getDatetime();
			String formattedTime =
					TimeHelper.formatTime(
							Date.from(Instant.ofEpochSecond(timestamp)), Locale.getDefault());

			String dateLabel =
					(note.getModified() != null)
							? ctx.getString(R.string.noteTimeModified, formattedTime)
							: ctx.getString(R.string.noteDateTime, formattedTime);

			binding.noteDate.setText(dateLabel);

			binding.deleteNote.setVisibility(
					"insert".equalsIgnoreCase(insert) ? View.GONE : View.VISIBLE);
		}
	}

	private void deleteNote(int position, int noteId) {
		NotesApi notesApi = BaseApi.getInstance(ctx, NotesApi.class);
		if (notesApi != null) {
			notesApi.deleteNote(noteId);
			notesList.remove(position);
			notifyItemRemoved(position);
			notifyItemRangeChanged(position, notesList.size());
			Toasty.show(ctx, ctx.getResources().getQuantityString(R.plurals.noteDeleteMessage, 1));
		}
	}

	@NonNull @Override
	public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new NotesViewHolder(
				ListNotesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
		holder.bind(notesList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return notesList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Notes> list) {
		this.notesList = list;
		notifyDataSetChanged();
	}
}

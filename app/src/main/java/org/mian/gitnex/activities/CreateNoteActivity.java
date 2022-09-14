package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.ActivityCreateNoteBinding;
import org.mian.gitnex.helpers.Markdown;
import java.time.Instant;
import java.util.Objects;

/**
 * @author M M Arif
 */

public class CreateNoteActivity extends BaseActivity {

	private ActivityCreateNoteBinding activityCreateNoteBinding;
	private boolean renderMd = false;
	private String action;
	private Notes notes;
	private NotesApi notesApi;
	private int noteId;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityCreateNoteBinding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
		notesApi = BaseApi.getInstance(ctx, NotesApi.class);

		setContentView(activityCreateNoteBinding.getRoot());
		setSupportActionBar(activityCreateNoteBinding.toolbar);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		assert imm != null;
		imm.showSoftInput(activityCreateNoteBinding.noteContent, InputMethodManager.SHOW_IMPLICIT);
		activityCreateNoteBinding.noteContent.requestFocus();

		activityCreateNoteBinding.close.setOnClickListener(view -> finish());

		if(getIntent().getStringExtra("action") != null) {
			action = getIntent().getStringExtra("action");
		}
		else {
			action = "";
		}

		activityCreateNoteBinding.close.setOnClickListener(close -> finish());
		activityCreateNoteBinding.toolbarTitle.setMovementMethod(new ScrollingMovementMethod());

		if(action.equalsIgnoreCase("edit")) {

			noteId = getIntent().getIntExtra( "noteId", 0);
			notes = notesApi.fetchNoteById(noteId);
			activityCreateNoteBinding.noteContent.setText(notes.getContent());

			activityCreateNoteBinding.markdownPreview.setVisibility(View.GONE);
			activityCreateNoteBinding.toolbarTitle.setText(R.string.editNote);

			activityCreateNoteBinding.noteContent.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {

					String text = activityCreateNoteBinding.noteContent.getText().toString();

					if(!text.isEmpty()) {

						updateNote(text);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
			});
		}
		else if(action.equalsIgnoreCase("add")) {

			activityCreateNoteBinding.markdownPreview.setVisibility(View.GONE);

			activityCreateNoteBinding.noteContent.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {

					String text = activityCreateNoteBinding.noteContent.getText().toString();

					if(!text.isEmpty() && text.length() > 4) {

						if(noteId > 0) {
							updateNote(text);
						}
						else {
							noteId = (int) notesApi.insertNote(text, (int) Instant.now().getEpochSecond());
						}
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
			});
		}
		else {
			activityCreateNoteBinding.markdownPreview.setVisibility(View.VISIBLE);
		}
	}

	private void updateNote(String content) {
		notesApi.updateNote(content, Instant.now().getEpochSecond(), noteId);
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.markdown_switcher, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if(id == android.R.id.home) {

			finish();
			return true;
		}
		else if(id == R.id.markdown) {

			if(action.equalsIgnoreCase("edit") || action.equalsIgnoreCase("add")) {
				if(!renderMd) {
					Markdown.render(ctx, EmojiParser.parseToUnicode(Objects.requireNonNull(activityCreateNoteBinding.noteContent.getText().toString())), activityCreateNoteBinding.markdownPreview);

					activityCreateNoteBinding.markdownPreview.setVisibility(View.VISIBLE);
					activityCreateNoteBinding.noteContent.setVisibility(View.GONE);
					renderMd = true;
				}
				else {
					activityCreateNoteBinding.markdownPreview.setVisibility(View.GONE);
					activityCreateNoteBinding.noteContent.setVisibility(View.VISIBLE);
					renderMd = false;
				}
			}

			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}
}

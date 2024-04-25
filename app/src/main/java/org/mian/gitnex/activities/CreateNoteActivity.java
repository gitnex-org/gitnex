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
import java.time.Instant;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.ActivityCreateNoteBinding;
import org.mian.gitnex.helpers.Markdown;

/**
 * @author M M Arif
 */
public class CreateNoteActivity extends BaseActivity {

	private ActivityCreateNoteBinding binding;
	private boolean renderMd = false;
	private String action;
	private Notes notes;
	private NotesApi notesApi;
	private int noteId;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
		notesApi = BaseApi.getInstance(ctx, NotesApi.class);

		setContentView(binding.getRoot());
		setSupportActionBar(binding.toolbar);

		InputMethodManager imm =
				(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		assert imm != null;
		imm.showSoftInput(binding.noteContent, InputMethodManager.SHOW_IMPLICIT);
		binding.noteContent.requestFocus();

		binding.close.setOnClickListener(view -> finish());

		if (getIntent().getStringExtra("action") != null) {
			action = getIntent().getStringExtra("action");
		} else {
			action = "";
		}

		binding.close.setOnClickListener(close -> finish());
		binding.toolbarTitle.setMovementMethod(new ScrollingMovementMethod());

		if (action.equalsIgnoreCase("edit")) {

			noteId = getIntent().getIntExtra("noteId", 0);
			notes = notesApi.fetchNoteById(noteId);
			binding.noteContent.setText(notes.getContent());

			assert notes.getContent() != null;
			binding.noteContent.setSelection(notes.getContent().length());

			binding.markdownPreview.setVisibility(View.GONE);
			binding.toolbarTitle.setText(R.string.editNote);

			binding.noteContent.addTextChangedListener(
					new TextWatcher() {

						@Override
						public void afterTextChanged(Editable s) {

							String text = binding.noteContent.getText().toString();

							if (!text.isEmpty()) {

								updateNote(text);
							}
						}

						@Override
						public void beforeTextChanged(
								CharSequence s, int start, int count, int after) {}

						@Override
						public void onTextChanged(
								CharSequence s, int start, int before, int count) {}
					});
		} else if (action.equalsIgnoreCase("add")) {

			binding.markdownPreview.setVisibility(View.GONE);

			binding.noteContent.addTextChangedListener(
					new TextWatcher() {

						@Override
						public void afterTextChanged(Editable s) {

							String text = binding.noteContent.getText().toString();

							if (!text.isEmpty() && text.length() > 4) {

								if (noteId > 0) {
									updateNote(text);
								} else {
									noteId =
											(int)
													notesApi.insertNote(
															text,
															(int) Instant.now().getEpochSecond());
								}
							}
						}

						@Override
						public void beforeTextChanged(
								CharSequence s, int start, int count, int after) {}

						@Override
						public void onTextChanged(
								CharSequence s, int start, int before, int count) {}
					});
		} else {
			binding.markdownPreview.setVisibility(View.VISIBLE);
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

		if (id == android.R.id.home) {

			finish();
			return true;
		} else if (id == R.id.markdown) {

			if (action.equalsIgnoreCase("edit") || action.equalsIgnoreCase("add")) {
				if (!renderMd) {
					Markdown.render(
							ctx,
							EmojiParser.parseToUnicode(
									Objects.requireNonNull(
											binding.noteContent.getText().toString())),
							binding.markdownPreview);

					binding.markdownPreview.setVisibility(View.VISIBLE);
					binding.noteContent.setVisibility(View.GONE);
					renderMd = true;
				} else {
					binding.markdownPreview.setVisibility(View.GONE);
					binding.noteContent.setVisibility(View.VISIBLE);
					renderMd = false;
				}
			}

			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}

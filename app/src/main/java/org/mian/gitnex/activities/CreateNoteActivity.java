package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.vdurmont.emoji.EmojiParser;
import java.time.Instant;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.ActivityCreateNoteBinding;
import org.mian.gitnex.helpers.Markdown;

/**
 * @author mmarif
 */
public class CreateNoteActivity extends BaseActivity {

	private ActivityCreateNoteBinding binding;
	private boolean renderMd = false;
	private String action;
	private NotesApi notesApi;
	private int noteId;
	private final Handler saveHandler = new Handler(Looper.getMainLooper());
	private Runnable saveRunnable;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		notesApi = BaseApi.getInstance(ctx, NotesApi.class);
		action =
				getIntent().getStringExtra("action") != null
						? getIntent().getStringExtra("action")
						: "";

		setupUI();
		setupListeners();
		loadInitialData();
	}

	private void setupUI() {
		binding.noteContent.requestFocus();
		InputMethodManager imm =
				(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(binding.noteContent, InputMethodManager.SHOW_IMPLICIT);
		}

		binding.markdownPreview.setVisibility(View.GONE);
	}

	private void setupListeners() {

		binding.btnBack.setOnClickListener(v -> finish());

		binding.btnMdPreview.setBackgroundResource(R.drawable.nav_pill_background);
		binding.btnMdPreview.setBackgroundTintList(null);
		if (binding.btnMdPreview.getBackground() != null) {
			binding.btnMdPreview.getBackground().setAlpha(0);
		}

		binding.btnMdPreview.setOnClickListener(v -> toggleMarkdownPreview());

		binding.noteContent.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						saveHandler.removeCallbacks(saveRunnable);
					}

					@Override
					public void afterTextChanged(Editable s) {
						String text = s.toString().trim();
						if (text.isEmpty()) return;

						saveRunnable =
								() -> {
									if (action.equalsIgnoreCase("edit")) {
										updateNote(text);
									} else if (action.equalsIgnoreCase("add")
											&& text.length() > 4) {
										if (noteId > 0) {
											updateNote(text);
										} else {
											noteId =
													(int)
															notesApi.insertNote(
																	text,
																	(int)
																			Instant.now()
																					.getEpochSecond());
										}
									}
								};
						saveHandler.postDelayed(saveRunnable, 500);
					}

					@Override
					public void beforeTextChanged(
							CharSequence s, int start, int count, int after) {}
				});
	}

	private void loadInitialData() {
		if (action.equalsIgnoreCase("edit")) {
			noteId = getIntent().getIntExtra("noteId", 0);
			Notes notes = notesApi.fetchNoteById(noteId);
			if (notes != null && notes.getContent() != null) {
				binding.noteContent.setText(notes.getContent());
				binding.noteContent.setSelection(notes.getContent().length());
			}
		}
	}

	private void toggleMarkdownPreview() {
		renderMd = !renderMd;

		if (renderMd) {
			Markdown.render(
					ctx,
					EmojiParser.parseToUnicode(binding.noteContent.getText().toString()),
					binding.markdownPreview);
			binding.markdownPreview.setVisibility(View.VISIBLE);
			binding.noteContent.setVisibility(View.GONE);

			binding.btnMdPreview.setSelected(true);
			if (binding.btnMdPreview.getBackground() != null) {
				binding.btnMdPreview.getBackground().setAlpha(255);
			}
			hideKeyboard();
		} else {
			binding.markdownPreview.setVisibility(View.GONE);
			binding.noteContent.setVisibility(View.VISIBLE);

			binding.btnMdPreview.setSelected(false);
			if (binding.btnMdPreview.getBackground() != null) {
				binding.btnMdPreview.getBackground().setAlpha(0);
			}
			binding.noteContent.requestFocus();
		}
	}

	private void hideKeyboard() {
		InputMethodManager imm =
				(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && getCurrentFocus() != null) {
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	private void updateNote(String content) {
		notesApi.updateNote(content, Instant.now().getEpochSecond(), noteId);
	}
}

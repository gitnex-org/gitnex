package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetFullscreenEditorBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.MentionHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class BottomSheetFullScreenEditor extends BottomSheetDialogFragment {

	private static final String CONTENT = "content";
	private static final String REPO_CONTEXT = "repo_context";
	private static final String SHOW_NOTES = "show_notes";
	private static final String SHOW_MD = "show_md";

	private BottomsheetFullscreenEditorBinding binding;
	private RepositoryContext repoContext;
	private boolean isMarkdownMode = false;
	private boolean showNotes = false;
	private boolean showMd = false;
	private EditorListener listener;
	private MentionHelper mentionHelper;

	public interface EditorListener {
		void onContentChanged(String newContent);
	}

	public static BottomSheetFullScreenEditor newInstance(
			String content, RepositoryContext repoContext, boolean showNotes, boolean showMd) {
		BottomSheetFullScreenEditor fragment = new BottomSheetFullScreenEditor();
		Bundle args = new Bundle();
		args.putString(CONTENT, content);
		args.putSerializable(REPO_CONTEXT, repoContext);
		args.putBoolean(SHOW_NOTES, showNotes);
		args.putBoolean(SHOW_MD, showMd);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			repoContext = (RepositoryContext) getArguments().getSerializable(REPO_CONTEXT);
			showNotes = getArguments().getBoolean(SHOW_NOTES, false);
			showMd = getArguments().getBoolean(SHOW_MD, false);
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetFullscreenEditorBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String content = getArguments() != null ? getArguments().getString(CONTENT) : "";
		if (content == null) content = "";

		mentionHelper = new MentionHelper(requireContext(), binding.fullscreenEditor);
		mentionHelper.setup();

		binding.fullscreenEditor.setText(content);
		binding.fullscreenEditor.setSelection(content.length());

		binding.fullscreenBtnNotes.setVisibility(showNotes ? View.VISIBLE : View.GONE);
		binding.fullscreenBtnMarkdown.setVisibility(showMd ? View.VISIBLE : View.GONE);

		binding.fullscreenBtnClear.setOnClickListener(
				v -> {
					binding.fullscreenEditor.setText("");
					if (isMarkdownMode) {
						binding.fullscreenMarkdownPreview.setAdapter(null);
					}
				});

		binding.fullscreenBtnNotes.setOnClickListener(
				v -> {
					BottomSheetNotesPicker notesPicker = BottomSheetNotesPicker.newInstance();
					notesPicker.setOnNoteSelectedListener(
							noteContent -> {
								int start = binding.fullscreenEditor.getSelectionStart();
								binding.fullscreenEditor.getText().insert(start, noteContent);
							});
					notesPicker.show(getChildFragmentManager(), "NOTES_PICKER");
				});

		binding.fullscreenBtnCollapse.setOnClickListener(
				v -> {
					if (listener != null) {
						String newContent =
								binding.fullscreenEditor.getText() != null
										? binding.fullscreenEditor.getText().toString()
										: "";
						listener.onContentChanged(newContent);
					}
					dismiss();
				});

		binding.fullscreenBtnMarkdown.setOnClickListener(
				v -> {
					isMarkdownMode = !isMarkdownMode;
					binding.fullscreenBtnMarkdown.setIconResource(
							isMarkdownMode ? R.drawable.ic_edit : R.drawable.ic_markdown);
					binding.fullscreenBtnMarkdown.setIconSize(52);

					if (isMarkdownMode) {
						binding.fullscreenEditor.setVisibility(View.GONE);
						binding.fullscreenMarkdownScroll.setVisibility(View.VISIBLE);
						String editorContent =
								binding.fullscreenEditor.getText() != null
										? binding.fullscreenEditor.getText().toString()
										: "";
						Markdown.render(
								requireContext(),
								EmojiParser.parseToUnicode(editorContent),
								binding.fullscreenMarkdownPreview,
								repoContext);
					} else {
						binding.fullscreenMarkdownScroll.setVisibility(View.GONE);
						binding.fullscreenEditor.setVisibility(View.VISIBLE);
						binding.fullscreenEditor.requestFocus();

						String currentText =
								binding.fullscreenEditor.getText() != null
										? binding.fullscreenEditor.getText().toString()
										: "";
						binding.fullscreenEditor.setSelection(currentText.length());
					}
				});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	public void setEditorListener(EditorListener listener) {
		this.listener = listener;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
		mentionHelper.dismissPopup();
	}
}

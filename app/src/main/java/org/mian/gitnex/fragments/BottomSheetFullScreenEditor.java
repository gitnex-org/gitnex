package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.amrdeveloper.codeview.CodeView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vdurmont.emoji.EmojiParser;
import java.util.HashMap;
import java.util.Map;
import org.mian.gitnex.R;
import org.mian.gitnex.core.MainGrammarLocator;
import org.mian.gitnex.databinding.BottomsheetFullscreenEditorBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.MentionHelper;
import org.mian.gitnex.helpers.codeeditor.CustomCodeViewAdapter;
import org.mian.gitnex.helpers.codeeditor.SourcePositionListener;
import org.mian.gitnex.helpers.codeeditor.languages.Language;
import org.mian.gitnex.helpers.codeeditor.theme.Theme;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class BottomSheetFullScreenEditor extends BottomSheetDialogFragment {

	public enum EditorMode {
		STANDARD, // Mentions enabled, no syntax highlighting, regular font
		MARKDOWN, // Mentions enabled, Markdown preview available, regular font
		CODE // No mentions, no Markdown preview, monospace font, syntax highlighting
	}

	private static final String CONTENT = "content";
	private static final String REPO_CONTEXT = "repo_context";
	private static final String EDITOR_MODE = "editor_mode";
	private static final String FILE_EXTENSION = "file_extension";

	private BottomsheetFullscreenEditorBinding binding;
	private RepositoryContext repoContext;
	private EditorMode editorMode = EditorMode.STANDARD;
	private String fileExtension;
	private boolean isMarkdownMode = false;
	private EditorListener listener;
	private MentionHelper mentionHelper;

	public interface EditorListener {
		void onContentChanged(String newContent);
	}

	public static BottomSheetFullScreenEditor newInstance(
			String content,
			RepositoryContext repoContext,
			EditorMode mode,
			@Nullable String fileExtension) {
		BottomSheetFullScreenEditor fragment = new BottomSheetFullScreenEditor();
		Bundle args = new Bundle();
		args.putString(CONTENT, content);
		args.putSerializable(REPO_CONTEXT, repoContext);
		args.putString(EDITOR_MODE, mode.name());
		if (fileExtension != null) {
			args.putString(FILE_EXTENSION, fileExtension);
		}
		fragment.setArguments(args);
		return fragment;
	}

	public static BottomSheetFullScreenEditor newInstance(
			String content, RepositoryContext repoContext, boolean showNotes, boolean showMd) {
		EditorMode mode = showMd ? EditorMode.MARKDOWN : EditorMode.STANDARD;
		return newInstance(content, repoContext, mode, null);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			repoContext = (RepositoryContext) getArguments().getSerializable(REPO_CONTEXT);
			String modeStr = getArguments().getString(EDITOR_MODE);
			if (modeStr != null) {
				editorMode = EditorMode.valueOf(modeStr);
			}
			fileExtension = getArguments().getString(FILE_EXTENSION);
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

		setupByMode(content);
		setupListeners();
	}

	private void setupByMode(String content) {
		switch (editorMode) {
			case CODE:
				binding.fullscreenEditor.setVisibility(View.GONE);
				binding.fullscreenCodeView.setVisibility(View.VISIBLE);
				binding.fullscreenBtnNotes.setVisibility(View.GONE);
				binding.fullscreenBtnMarkdown.setVisibility(View.GONE);
				binding.fullscreenBtnClear.setVisibility(View.GONE);
				binding.editorInfoLayout.setVisibility(View.VISIBLE);
				binding.editorCursorPosition.setVisibility(View.VISIBLE);

				if (fileExtension != null) {
					binding.editorFileExt.setText(fileExtension);
				}

				setupCodeView(content);
				break;

			case MARKDOWN:
				binding.fullscreenEditor.setVisibility(View.VISIBLE);
				binding.fullscreenCodeView.setVisibility(View.GONE);
				mentionHelper = new MentionHelper(requireContext(), binding.fullscreenEditor);
				mentionHelper.setup();
				binding.fullscreenBtnNotes.setVisibility(View.VISIBLE);
				binding.fullscreenBtnMarkdown.setVisibility(View.VISIBLE);
				binding.fullscreenBtnClear.setVisibility(View.VISIBLE);
				binding.editorInfoLayout.setVisibility(View.GONE);

				binding.fullscreenEditor.setText(content);
				binding.fullscreenEditor.setSelection(content.length());
				break;

			case STANDARD:
			default:
				binding.fullscreenEditor.setVisibility(View.VISIBLE);
				binding.fullscreenCodeView.setVisibility(View.GONE);
				mentionHelper = new MentionHelper(requireContext(), binding.fullscreenEditor);
				mentionHelper.setup();
				binding.fullscreenBtnNotes.setVisibility(View.VISIBLE);
				binding.fullscreenBtnMarkdown.setVisibility(View.GONE);
				binding.fullscreenBtnClear.setVisibility(View.VISIBLE);
				binding.editorInfoLayout.setVisibility(View.GONE);

				binding.fullscreenEditor.setText(content);
				binding.fullscreenEditor.setSelection(content.length());
				break;
		}
	}

	private void setupCodeView(String content) {
		CodeView codeView = binding.fullscreenCodeView;
		codeView.setTypeface(android.graphics.Typeface.MONOSPACE);

		String language =
				fileExtension != null ? MainGrammarLocator.fromExtension(fileExtension) : "text";
		Language lang = Language.fromName(language);

		Theme theme = Theme.getDefaultTheme(requireContext());
		lang.applyTheme(requireContext(), codeView, theme);

		setupIndentation(codeView, lang);

		Map<Character, Character> pairCompleteMap = new HashMap<>();
		pairCompleteMap.put('{', '}');
		pairCompleteMap.put('[', ']');
		pairCompleteMap.put('(', ')');
		pairCompleteMap.put('<', '>');
		pairCompleteMap.put('"', '"');
		pairCompleteMap.put('\'', '\'');
		codeView.setPairCompleteMap(pairCompleteMap);
		codeView.enablePairComplete(true);
		codeView.enablePairCompleteCenterCursor(true);

		java.util.List<com.amrdeveloper.codeview.Code> codeList = lang.getCodeList();
		if (codeList != null && !codeList.isEmpty()) {
			CustomCodeViewAdapter adapter = new CustomCodeViewAdapter(requireContext(), codeList);
			codeView.setAdapter(adapter);
		}

		setupCursorPositionTracker();

		if (content != null && !content.isEmpty()) {
			codeView.setText(content);
		}
	}

	private void setupIndentation(CodeView codeView, Language lang) {
		codeView.setIndentationStarts(lang.getIndentationStarts());
		codeView.setIndentationEnds(lang.getIndentationEnds());

		String indentSetting =
				AppDatabaseSettings.getSettingsValue(
						requireContext(), AppDatabaseSettings.APP_CE_INDENTATION_KEY);
		if (indentSetting == null || Integer.parseInt(indentSetting) == 1) {
			codeView.setEnableAutoIndentation(true);

			String tabWidthSetting =
					AppDatabaseSettings.getSettingsValue(
							requireContext(), AppDatabaseSettings.APP_CE_TABS_WIDTH_KEY);
			int tabWidth = 4;
			if (tabWidthSetting != null) {
				tabWidth =
						switch (Integer.parseInt(tabWidthSetting)) {
							case 0 -> 2;
							case 2 -> 6;
							case 3 -> 8;
							default -> 4;
						};
			}
			codeView.setTabLength(tabWidth);
		} else {
			codeView.setEnableAutoIndentation(false);
		}
	}

	private void setupCursorPositionTracker() {
		binding.editorCursorPosition.setVisibility(View.VISIBLE);
		SourcePositionListener sourcePositionListener =
				new SourcePositionListener(binding.fullscreenCodeView);
		sourcePositionListener.setOnPositionChanged(
				(line, column) -> {
					binding.editorCursorPosition.setText(
							getString(R.string.cursor_position_format, line, column));
				});
	}

	private void setupListeners() {
		binding.fullscreenBtnCollapse.setOnClickListener(
				v -> {
					String newContent;

					if (editorMode == EditorMode.CODE) {
						newContent = binding.fullscreenCodeView.getText().toString();
					} else {
						newContent =
								binding.fullscreenEditor.getText() != null
										? binding.fullscreenEditor.getText().toString()
										: "";
					}

					if (listener != null) {
						listener.onContentChanged(newContent);
					}
					dismiss();
				});

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

		binding.fullscreenBtnMarkdown.setOnClickListener(
				v -> {
					isMarkdownMode = !isMarkdownMode;
					binding.fullscreenBtnMarkdown.setIconResource(
							isMarkdownMode ? R.drawable.ic_edit : R.drawable.ic_markdown);

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
		if (mentionHelper != null) {
			mentionHelper.dismissPopup();
		}
	}
}

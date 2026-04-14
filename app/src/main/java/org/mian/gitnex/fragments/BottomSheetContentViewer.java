package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetContentViewerBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class BottomSheetContentViewer extends BottomSheetDialogFragment {

	public enum Feature {
		MARKDOWN_PREVIEW, // Enable markdown preview toggle
		START_IN_MARKDOWN, // Start showing Markdown instead of raw content
		ALLOW_COPY, // Show copy button (always visible by default)
		ALLOW_SHARE, // Show share button (always visible by default)
		SYNTAX_HIGHLIGHT, // Use renderWithHighlights for code
		SHOW_TITLE, // Show title in header
	}

	/*
	Spannable highlighted = SyntaxHighlighter.highlight(code, "java");
	BottomSheetContentViewer.newInstance(
		highlighted,
		"Main.java",
		repoContext,
		BottomSheetContentViewer.Feature.SYNTAX_HIGHLIGHT,
		ContentViewerBottomSheet.Feature.MARKDOWN_PREVIEW,
		ContentViewerBottomSheet.Feature.SHOW_TITLE
	).show(fm, "viewer");
	 */

	private static final String ARG_CONTENT = "content";
	private static final String ARG_TITLE = "title";
	private static final String ARG_REPO_CONTEXT = "repo_context";
	private static final String ARG_FEATURES = "features";
	private static final String ARG_IS_SPANNABLE = "is_spannable";

	private BottomsheetContentViewerBinding binding;
	private final Set<Feature> enabledFeatures = new HashSet<>();
	private RepositoryContext repoContext;
	private String rawContent;
	private String title;
	private Spannable spannableContent;
	private boolean isSpannable = false;
	private boolean isMarkdownMode = false;

	public static BottomSheetContentViewer newInstance(
			String content,
			@Nullable String title,
			@Nullable RepositoryContext repoContext,
			Feature... features) {
		BottomSheetContentViewer fragment = new BottomSheetContentViewer();
		Bundle args = new Bundle();
		args.putString(ARG_CONTENT, content);
		args.putBoolean(ARG_IS_SPANNABLE, false);
		if (title != null) args.putString(ARG_TITLE, title);
		if (repoContext != null) args.putSerializable(ARG_REPO_CONTEXT, repoContext);
		args.putStringArrayList(ARG_FEATURES, featureNamesToList(features));
		fragment.setArguments(args);
		return fragment;
	}

	public static BottomSheetContentViewer newInstance(
			Spannable spannable,
			@Nullable String title,
			@Nullable RepositoryContext repoContext,
			Feature... features) {
		BottomSheetContentViewer fragment = new BottomSheetContentViewer();
		Bundle args = new Bundle();
		args.putCharSequence(ARG_CONTENT, spannable);
		args.putBoolean(ARG_IS_SPANNABLE, true);
		if (title != null) args.putString(ARG_TITLE, title);
		if (repoContext != null) args.putSerializable(ARG_REPO_CONTEXT, repoContext);
		args.putStringArrayList(ARG_FEATURES, featureNamesToList(features));
		fragment.setArguments(args);
		return fragment;
	}

	private static ArrayList<String> featureNamesToList(Feature... features) {
		ArrayList<String> names = new ArrayList<>();
		for (Feature f : features) {
			names.add(f.name());
		}
		return names;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			isSpannable = getArguments().getBoolean(ARG_IS_SPANNABLE, false);

			if (isSpannable) {
				spannableContent = (Spannable) getArguments().getCharSequence(ARG_CONTENT);
			} else {
				rawContent = getArguments().getString(ARG_CONTENT, "");
			}

			title = getArguments().getString(ARG_TITLE);
			repoContext = (RepositoryContext) getArguments().getSerializable(ARG_REPO_CONTEXT);

			ArrayList<String> featureNames = getArguments().getStringArrayList(ARG_FEATURES);
			if (featureNames != null) {
				for (String name : featureNames) {
					try {
						enabledFeatures.add(Feature.valueOf(name));
					} catch (IllegalArgumentException ignored) {
					}
				}
			}
		}

		isMarkdownMode = enabledFeatures.contains(Feature.START_IN_MARKDOWN);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetContentViewerBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setupUI();
		renderContent();
	}

	private void setupUI() {
		binding.btnClose.setOnClickListener(v -> dismiss());

		if (enabledFeatures.contains(Feature.ALLOW_COPY)) {
			binding.btnCopy.setVisibility(View.VISIBLE);
			binding.btnCopy.setOnClickListener(v -> copyContent());
		}

		if (enabledFeatures.contains(Feature.ALLOW_SHARE)) {
			binding.btnShare.setVisibility(View.VISIBLE);
			binding.btnShare.setOnClickListener(v -> shareContent());
		}

		if (enabledFeatures.contains(Feature.MARKDOWN_PREVIEW)) {
			binding.btnMarkdown.setVisibility(View.VISIBLE);
			binding.btnMarkdown.setOnClickListener(v -> toggleMarkdownMode());
			updateMarkdownIcon();
		}

		if (enabledFeatures.contains(Feature.SHOW_TITLE) && title != null) {
			binding.viewerTitle.setText(title);
			binding.viewerTitle.setVisibility(View.VISIBLE);
		}
	}

	private void renderContent() {
		if (isMarkdownMode) {
			renderMarkdown();
		} else {
			renderRaw();
		}
	}

	private void renderRaw() {
		binding.rawContentScroll.setVisibility(View.VISIBLE);
		binding.markdownPreviewScroll.setVisibility(View.GONE);
		binding.markdownPreview.setVisibility(View.GONE);
		binding.markdownPreviewText.setVisibility(View.GONE);

		String content = getContentAsString();
		binding.rawContentText.setText(content);
	}

	private void renderMarkdown() {
		binding.rawContentScroll.setVisibility(View.GONE);
		binding.markdownPreviewScroll.setVisibility(View.VISIBLE);

		String content = getContentAsString();
		if (content == null) content = "";

		if (enabledFeatures.contains(Feature.SYNTAX_HIGHLIGHT) && spannableContent != null) {
			binding.markdownPreview.setVisibility(View.VISIBLE);
			binding.markdownPreviewText.setVisibility(View.GONE);
			Markdown.renderWithHighlights(
					requireContext(), spannableContent, binding.markdownPreview, repoContext);
		} else if (repoContext != null) {
			binding.markdownPreview.setVisibility(View.VISIBLE);
			binding.markdownPreviewText.setVisibility(View.GONE);
			Markdown.render(requireContext(), content, binding.markdownPreview, repoContext);
		} else {
			binding.markdownPreview.setVisibility(View.GONE);
			binding.markdownPreviewText.setVisibility(View.VISIBLE);
			Markdown.render(requireContext(), content, binding.markdownPreviewText);
		}
	}

	private String getContentAsString() {
		return isSpannable && spannableContent != null ? spannableContent.toString() : rawContent;
	}

	private void toggleMarkdownMode() {
		isMarkdownMode = !isMarkdownMode;
		updateMarkdownIcon();
		renderContent();
	}

	private void updateMarkdownIcon() {
		binding.btnMarkdown.setIconResource(
				isMarkdownMode ? R.drawable.ic_edit : R.drawable.ic_markdown);
	}

	private void copyContent() {
		String content = getContentAsString();
		AppUtil.copyToClipboard(requireContext(), content, getString(R.string.copied_to_clipboard));
	}

	private void shareContent() {
		String content = getContentAsString();
		AppUtil.sharingIntent(requireContext(), content);
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}

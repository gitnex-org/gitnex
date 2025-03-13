package org.mian.gitnex.helpers;

import android.content.Context;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mian.gitnex.R;
import org.mian.gitnex.views.SyntaxHighlightedArea;

/**
 * @author mmarif
 */
public class FileContentSearcher {

	private final List<int[]> matchPositions = new ArrayList<>();
	private int currentMatchIndex = -1;
	private String content;
	private String fileExtension;
	private final NestedScrollView scrollView;
	private final RecyclerView recyclerView;

	public FileContentSearcher(NestedScrollView scrollView, RecyclerView recyclerView) {
		this.scrollView = scrollView;
		this.recyclerView = recyclerView;
	}

	public void search(
			String content,
			String fileExtension,
			String query,
			SyntaxHighlightedArea codeView,
			RecyclerView markdownView,
			boolean isMarkdown) {

		this.content = content;
		this.fileExtension = fileExtension;
		matchPositions.clear();
		currentMatchIndex = -1;

		if (query == null || query.trim().isEmpty()) {
			clearHighlights(codeView, markdownView, isMarkdown);
			return;
		}

		Pattern pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			matchPositions.add(new int[] {matcher.start(), matcher.end()});
		}

		if (!matchPositions.isEmpty()) {
			currentMatchIndex = 0;
			highlightMatches(codeView, markdownView, isMarkdown);
			scrollToMatch(codeView, markdownView, isMarkdown, currentMatchIndex);
		} else {
			clearHighlights(codeView, markdownView, isMarkdown);
		}
	}

	public void nextMatch(
			SyntaxHighlightedArea codeView, RecyclerView markdownView, boolean isMarkdown) {

		if (matchPositions.isEmpty() || currentMatchIndex == -1) return;
		currentMatchIndex = (currentMatchIndex + 1) % matchPositions.size();
		highlightMatches(codeView, markdownView, isMarkdown);
		scrollToMatch(codeView, markdownView, isMarkdown, currentMatchIndex);
	}

	public void previousMatch(
			SyntaxHighlightedArea codeView, RecyclerView markdownView, boolean isMarkdown) {

		if (matchPositions.isEmpty() || currentMatchIndex == -1) return;
		currentMatchIndex = (currentMatchIndex - 1 + matchPositions.size()) % matchPositions.size();
		highlightMatches(codeView, markdownView, isMarkdown);
		scrollToMatch(codeView, markdownView, isMarkdown, currentMatchIndex);
	}

	public int getMatchCount() {
		return matchPositions.size();
	}

	private void highlightMatches(
			SyntaxHighlightedArea codeView, RecyclerView markdownView, boolean isMarkdown) {

		SpannableString spannable = new SpannableString(content);

		int searchHighlightColor = ContextCompat.getColor(codeView.getContext(), R.color.darkGreen);
		int selectedHighlightColor =
				ContextCompat.getColor(codeView.getContext(), R.color.iconPrMergedColor);

		for (int i = 0; i < matchPositions.size(); i++) {
			int[] pos = matchPositions.get(i);
			int color = (i == currentMatchIndex) ? selectedHighlightColor : searchHighlightColor;
			spannable.setSpan(
					new BackgroundColorSpan(color),
					pos[0],
					pos[1],
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		if (isMarkdown && markdownView != null) {
			Markdown.renderWithHighlights(codeView.getContext(), spannable, markdownView, null);
		} else {
			codeView.setSpannable(spannable);
		}
	}

	private void scrollToMatch(
			SyntaxHighlightedArea codeView,
			RecyclerView markdownView,
			boolean isMarkdown,
			int index) {

		if (index < 0 || index >= matchPositions.size()) return;
		int[] pos = matchPositions.get(index);

		if (isMarkdown && recyclerView != null) {

			Context context = codeView.getContext();
			int avgCharHeight =
					(int)
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_SP,
									14,
									context.getResources().getDisplayMetrics());
			int charsPerLine = recyclerView.getWidth() / (avgCharHeight / 2);
			int scrollY =
					(pos[0] / (charsPerLine > 0 ? charsPerLine : 1)) * (int) (avgCharHeight * 1.2);
			recyclerView.smoothScrollToPosition(scrollY / avgCharHeight);
		} else if (scrollView != null) {

			TextView sourceView = codeView.getSourceView();
			ViewTreeObserver vto = sourceView.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(
					new ViewTreeObserver.OnGlobalLayoutListener() {
						@Override
						public void onGlobalLayout() {

							sourceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
							Layout layout = sourceView.getLayout();
							if (layout != null) {

								int line = layout.getLineForOffset(pos[0]);
								int y = layout.getLineTop(line);
								int contentHeight = sourceView.getHeight();
								int scrollViewHeight = scrollView.getHeight();
								int maxScroll = Math.max(0, contentHeight - scrollViewHeight);
								int adjustedY = Math.min(y, maxScroll);

								if (contentHeight > scrollViewHeight) {
									scrollView.requestLayout();
									scrollView.post(() -> scrollView.smoothScrollTo(0, adjustedY));
								}
							} else {

								int lineHeight = sourceView.getLineHeight();
								int lines = content.substring(0, pos[0]).split("\n").length - 1;
								int fallbackY = lines * lineHeight;
								int contentHeight = sourceView.getHeight();
								int scrollViewHeight = scrollView.getHeight();
								int maxScroll = Math.max(0, contentHeight - scrollViewHeight);
								int adjustedFallbackY = Math.min(fallbackY, maxScroll);

								if (contentHeight > scrollViewHeight) {
									scrollView.post(
											() -> scrollView.smoothScrollTo(0, adjustedFallbackY));
								}
							}
						}
					});
		}
	}

	private void clearHighlights(
			SyntaxHighlightedArea codeView, RecyclerView markdownView, boolean isMarkdown) {
		if (isMarkdown && markdownView != null) {
			Markdown.renderWithHighlights(
					codeView.getContext(), new SpannableString(content), markdownView, null);
		} else {
			codeView.setContent(content, fileExtension);
		}
	}
}

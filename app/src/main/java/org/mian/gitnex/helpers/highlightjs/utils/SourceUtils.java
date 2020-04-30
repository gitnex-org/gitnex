package org.mian.gitnex.helpers.highlightjs.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.mian.gitnex.helpers.highlightjs.HighlightJsView;

/**
 * This Class was created by Patrick J
 * on 09.06.16. (modified by opyale)
 */

public class SourceUtils {

	public static String generateContent(String source, @NonNull String theme, @Nullable String language, boolean supportZoom, boolean showLineNumbers,
	                                     HighlightJsView.TextWrap textWrap) {

		return getStylePageHeader(supportZoom, textWrap) +
				getSourceForTheme(theme) +
				(showLineNumbers ? getLineNumberStyling() : "") +
				getScriptPageHeader(showLineNumbers) +
				getSourceForLanguage(source, language) +
				getTemplateFooter();
	}

	private static String getStylePageHeader(boolean enableZoom, HighlightJsView.TextWrap textWrap) {

		String preTag;

		switch (textWrap) {

			case WORD_WRAP:
				preTag = "pre { white-space: pre-wrap; word-wrap: break-word; }";
				break;

			case BREAK_ALL:
				preTag = "pre { white-space: pre-wrap; word-break: break-all; }";
				break;

			default:
				preTag = "pre { }";
				break;

		}

		return "<!DOCTYPE html>" +
				"<html>" +
				"<head>" +
				"<meta charset=\"utf-8\">" +
				(enableZoom ? "" : "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0\">") +
				"<style>" +
				"html, body { width:100%; height: 100%; margin: 0px; padding: 0px; }" +
				preTag +
				"</style>";
	}

	private static String getScriptPageHeader(boolean showLineNumbers) {

		return "<script src=\"./highlightjs/highlight.pack.js\"></script>" +
				(showLineNumbers ? "<script src=\"./highlightjs/highlightjs-line-numbers.min.js\"></script>" : "") +
				"<script>hljs.initHighlightingOnLoad();</script>" +
				(showLineNumbers ? "<script>hljs.initLineNumbersOnLoad();</script>" : "") +
				"</head><body style=\"margin: 0; padding: 0\" class=\"hljs\">";
	}

	private static String getLineNumberStyling() {

		return "<style>" +
				".hljs-line-numbers { text-align: right; border-right: 1px solid #ccc; color: #999; user-select: none; }" +
				"</style>";
	}

	private static String getTemplateFooter() {

		return "</body></html>";
	}

	private static String escapeCode(String code) {

		return code.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	private static String getSourceForTheme(String theme) {

		return String.format("<link rel=\"stylesheet\" href=\"./highlightjs/themes/%s.css\">", theme);
	}

	private static String getSourceForLanguage(String source, String language) {

		if (language != null) {
			return String.format("<pre><code class=\"%s\">%s</code></pre>", language, escapeCode(source));
		} else {
			return String.format("<pre><code>%s</code></pre>", escapeCode(source));
		}

	}

}
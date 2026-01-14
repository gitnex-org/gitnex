package org.mian.gitnex.helpers.markdown;

import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;
import android.widget.TextView;
import androidx.annotation.NonNull;
import io.noties.markwon.AbstractMarkwonPlugin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mmarif
 */
public class AutoLinkPlugin extends AbstractMarkwonPlugin {

	private static final Pattern URL_PATTERN =
			Pattern.compile(
					"\\b((https?|ftp)://|www\\.)[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?\\b",
					Pattern.CASE_INSENSITIVE);

	public static AutoLinkPlugin create() {
		return new AutoLinkPlugin();
	}

	@Override
	public void afterSetText(@NonNull TextView textView) {
		CharSequence text = textView.getText();
		if (text instanceof SpannableStringBuilder builder) {
			linkifyUrls(builder);
		}
	}

	private void linkifyUrls(SpannableStringBuilder builder) {
		String text = builder.toString();
		Matcher matcher = URL_PATTERN.matcher(text);

		java.util.ArrayList<int[]> matches = new java.util.ArrayList<>();
		while (matcher.find()) {
			if (!hasUrlSpan(builder, matcher.start(), matcher.end())) {
				matches.add(new int[] {matcher.start(), matcher.end()});
			}
		}

		for (int[] match : matches) {
			String url = text.substring(match[0], match[1]);
			if (url.startsWith("www.")) {
				url = "https://" + url;
			}
			builder.setSpan(
					new URLSpan(url),
					match[0],
					match[1],
					SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	private boolean hasUrlSpan(SpannableStringBuilder builder, int start, int end) {
		URLSpan[] spans = builder.getSpans(start, end, URLSpan.class);
		for (URLSpan span : spans) {
			int spanStart = builder.getSpanStart(span);
			int spanEnd = builder.getSpanEnd(span);
			if (spanStart <= start && spanEnd >= end) {
				return true;
			}
		}
		return false;
	}
}

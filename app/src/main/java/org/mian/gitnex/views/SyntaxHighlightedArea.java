package org.mian.gitnex.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.qwerty287.markwonprism4j.Prism4jSyntaxHighlight;
import de.qwerty287.markwonprism4j.Prism4jTheme;
import de.qwerty287.markwonprism4j.Prism4jThemeDarkula;
import de.qwerty287.markwonprism4j.Prism4jThemeDefault;
import io.noties.prism4j.Prism4j;
import org.mian.gitnex.R;
import org.mian.gitnex.core.MainGrammarLocator;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author opyale
 */
public class SyntaxHighlightedArea extends LinearLayout {

	private Prism4jTheme prism4jTheme;

	private TextView sourceView;
	private LinesView linesView;

	public SyntaxHighlightedArea(@NonNull Context context) {
		super(context);
		setup();
	}

	public SyntaxHighlightedArea(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	public SyntaxHighlightedArea(
			@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setup();
	}

	public void setup() {

		prism4jTheme =
				AppUtil.getColorFromAttribute(getContext(), R.attr.isDark) == 1
						? Prism4jThemeDarkula.create()
						: Prism4jThemeDefault.create();

		sourceView = new TextView(getContext());

		sourceView.setLayoutParams(
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		sourceView.setTypeface(
				Typeface.createFromAsset(
						getContext().getAssets(), "fonts/sourcecodeproregular.ttf"));
		sourceView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		sourceView.setTextColor(prism4jTheme.textColor());
		sourceView.setTextIsSelectable(true);

		int padding = AppUtil.getPixelsFromDensity(getContext(), 5);
		sourceView.setPadding(padding, 0, padding, 0);

		HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getContext());
		horizontalScrollView.setLayoutParams(
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		horizontalScrollView.addView(sourceView);

		linesView = new LinesView(getContext());

		linesView.setLayoutParams(
				new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
		linesView.setPadding(
				AppUtil.getPixelsFromDensity(getContext(), 3),
				0,
				AppUtil.getPixelsFromDensity(getContext(), 6),
				0);

		linesView.getPaint().setTypeface(sourceView.getTypeface());
		linesView.getPaint().setTextSize(sourceView.getTextSize());

		linesView.setBackgroundColor(prism4jTheme.background());
		linesView.setTextColor(prism4jTheme.textColor());
		linesView.setLineColor(prism4jTheme.textColor());

		setOrientation(HORIZONTAL);
		setBackgroundColor(prism4jTheme.background());
		addView(linesView);
		addView(horizontalScrollView);
	}

	public void setContent(@NonNull String source, @NonNull String extension) {

		if (source.length() > 0) {

			Thread highlightingThread =
					new Thread(
							() -> {
								try {

									MainGrammarLocator mainGrammarLocator =
											MainGrammarLocator.getInstance();

									CharSequence highlightedSource =
											Prism4jSyntaxHighlight.create(
															new Prism4j(mainGrammarLocator),
															prism4jTheme,
															MainGrammarLocator
																	.DEFAULT_FALLBACK_LANGUAGE)
													.highlight(
															mainGrammarLocator.fromExtension(
																	extension),
															source);

									getActivity()
											.runOnUiThread(
													() -> sourceView.setText(highlightedSource));

								} catch (Throwable ignored) {
									// Fall back to plaintext if something fails
									getActivity().runOnUiThread(() -> sourceView.setText(source));
								}
							});

			Thread lineCountingThread =
					new Thread(
							() -> {
								long lineCount = AppUtil.getLineCount(source);

								try {
									highlightingThread.join();
								} catch (InterruptedException ignored) {
								}

								getActivity()
										.runOnUiThread(() -> linesView.setLineCount(lineCount));
							});

			highlightingThread.start();
			lineCountingThread.start();
		}
	}

	private Activity getActivity() {
		return (Activity) getContext();
	}

	public String getContent() {
		return sourceView.getText().toString();
	}

	private static class LinesView extends View {

		private final Paint paint = new Paint();
		private final Rect textBounds = new Rect();

		@ColorInt private int backgroundColor;
		@ColorInt private int textColor;
		@ColorInt private int lineColor;

		private long lineCount;

		public LinesView(Context context) {
			super(context);
		}

		public void setLineCount(long lineCount) {
			this.lineCount = lineCount;
			requestLayout();
		}

		@Override
		public void setBackgroundColor(@ColorInt int backgroundColor) {
			this.backgroundColor = backgroundColor;
			invalidate();
		}

		public void setTextColor(@ColorInt int textColor) {
			this.textColor = textColor;
			invalidate();
		}

		public void setLineColor(@ColorInt int lineColor) {
			this.lineColor = lineColor;
			invalidate();
		}

		public Paint getPaint() {
			return paint;
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

			String highestLineNumber = String.valueOf(lineCount);

			paint.getTextBounds(highestLineNumber, 0, highestLineNumber.length(), textBounds);

			setMeasuredDimension(
					getPaddingLeft() + textBounds.width() + getPaddingRight(),
					MeasureSpec.getSize(heightMeasureSpec));
		}

		@Override
		protected void onDraw(Canvas canvas) {

			paint.setColor(backgroundColor);

			canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

			float marginTopBottom = (float) (getHeight() - (textBounds.height() / 2)) / lineCount;

			paint.setColor(textColor);

			canvas.save();
			canvas.translate(getPaddingLeft(), marginTopBottom);

			for (int currentLine = 1; currentLine <= lineCount; currentLine++) {

				canvas.drawText(String.valueOf(currentLine), 0, 0, paint);
				canvas.translate(0, marginTopBottom);
			}

			paint.setColor(lineColor);

			int dividerX = getWidth() - 1;
			int dividerY = getHeight();

			canvas.restore();
			canvas.drawLine(dividerX, 0, dividerX, dividerY, paint);
		}
	}
}

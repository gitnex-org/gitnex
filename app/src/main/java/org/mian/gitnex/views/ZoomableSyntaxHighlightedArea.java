package org.mian.gitnex.views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.mian.gitnex.core.MainGrammarLocator;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.codeeditor.markwon.SyntaxHighlighter;

/**
 * @author mmarif
 */
public class ZoomableSyntaxHighlightedArea extends SyntaxHighlightedArea {

	private ZoomableTextView zoomableTextView;
	private HorizontalScrollView horizontalScrollView;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private static final int LARGE_FILE_THRESHOLD = 1000;
	private Runnable hideProgressCallback;

	public ZoomableSyntaxHighlightedArea(@NonNull Context context) {
		super(context);
		setupZoom();
	}

	public ZoomableSyntaxHighlightedArea(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		setupZoom();
	}

	public ZoomableSyntaxHighlightedArea(
			@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setupZoom();
	}

	private void setupZoom() {
		setup();

		zoomableTextView = new ZoomableTextView(getContext());
		zoomableTextView.setLayoutParams(
				new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		zoomableTextView.setTypeface(
				Typeface.createFromAsset(
						getContext().getAssets(), "fonts/sourcecodeproregular.ttf"));
		zoomableTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		zoomableTextView.setTextColor(
				getContext().getResources().getColor(theme.getDefaultColor(), null));
		zoomableTextView.setTextIsSelectable(true);
		int padding = AppUtil.getPixelsFromDensity(getContext(), 5);
		zoomableTextView.setPadding(padding, 0, padding, 0);
		zoomableTextView.setVisibility(View.GONE);

		horizontalScrollView = new HorizontalScrollView(getContext());
		horizontalScrollView.setLayoutParams(
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		horizontalScrollView.addView(zoomableTextView);

		removeAllViews();
		setOrientation(HORIZONTAL);
		setBackgroundColor(getContext().getResources().getColor(theme.getBackgroundColor(), null));
		addView(getLinesView());
		addView(horizontalScrollView);

		getLinesView()
				.setLayoutParams(
						new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		getLinesView()
				.setPadding(
						AppUtil.getPixelsFromDensity(getContext(), 3),
						0,
						AppUtil.getPixelsFromDensity(getContext(), 6),
						0);
		getLinesView().getPaint().setTypeface(zoomableTextView.getTypeface());
		getLinesView().getPaint().setTextSize(zoomableTextView.getTextSize());
		getLinesView()
				.setBackgroundColor(
						getContext().getResources().getColor(theme.getBackgroundColor(), null));
		getLinesView()
				.setTextColor(getContext().getResources().getColor(theme.getDefaultColor(), null));
		getLinesView()
				.setLineColor(getContext().getResources().getColor(theme.getDefaultColor(), null));
		getLinesView().setVisibility(View.GONE);
	}

	public void setHideProgressCallback(Runnable callback) {
		this.hideProgressCallback = callback;
	}

	@Override
	public void setContent(@NonNull String source, @NonNull String extension) {

		zoomableTextView.setVisibility(View.GONE);
		getLinesView().setVisibility(View.GONE);

		executor.execute(
				() -> {
					long lineCount = fastLineCount(source);
					CharSequence content = source;

					if (lineCount <= LARGE_FILE_THRESHOLD) {
						try {
							content =
									SyntaxHighlighter.create(
													getContext(),
													theme,
													MainGrammarLocator.DEFAULT_FALLBACK_LANGUAGE)
											.highlight(
													MainGrammarLocator.fromExtension(extension)
															.toUpperCase(),
													source);
						} catch (Throwable ignored) {
						}
					}

					CharSequence finalContent = content;
					mainHandler.post(
							() -> {
								zoomableTextView.setText(finalContent);
								getLinesView().setLineCount(lineCount);
								getLinesView().setMinimumWidth(1000);

								zoomableTextView.setVisibility(View.VISIBLE);
								getLinesView().setVisibility(View.VISIBLE);
								syncLinesViewAfterLayout();

								if (hideProgressCallback != null) {
									hideProgressCallback.run();
								}

								executor.execute(
										() -> {
											float maxWidth = calculateMaxWidth(source);
											CharSequence highlighted =
													lineCount > LARGE_FILE_THRESHOLD
															? SyntaxHighlighter.create(
																			getContext(),
																			theme,
																			MainGrammarLocator
																					.DEFAULT_FALLBACK_LANGUAGE)
																	.highlight(
																			MainGrammarLocator
																					.fromExtension(
																							extension)
																					.toUpperCase(),
																			source)
															: null;

											mainHandler.post(
													() -> {
														int scaledWidth =
																(int)
																		(maxWidth
																				* zoomableTextView
																						.getScaleFactor());
														getLinesView().setMinimumWidth(scaledWidth);
														if (highlighted != null) {
															zoomableTextView.setText(highlighted);
														}
													});
										});
							});
				});
	}

	private long fastLineCount(String source) {
		int count = 1;
		for (int i = 0; i < source.length(); i++) {
			if (source.charAt(i) == '\n') count++;
		}
		return count;
	}

	private float calculateMaxWidth(String source) {
		TextView tempTextView = new TextView(getContext());
		tempTextView.setLayoutParams(
				new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		tempTextView.setTypeface(zoomableTextView.getTypeface());
		tempTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		float maxWidth = 0;
		String[] lines = source.split("\n");
		for (int i = 0; i < Math.min(lines.length, 20); i++) {
			tempTextView.setText(lines[i]);
			tempTextView.measure(
					View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
					View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
			maxWidth = Math.max(maxWidth, tempTextView.getMeasuredWidth());
		}
		return maxWidth;
	}

	private void syncLinesViewAfterLayout() {
		zoomableTextView
				.getViewTreeObserver()
				.addOnGlobalLayoutListener(
						new ViewTreeObserver.OnGlobalLayoutListener() {
							@Override
							public void onGlobalLayout() {
								syncLinesView();
								zoomableTextView
										.getViewTreeObserver()
										.removeOnGlobalLayoutListener(this);
							}
						});
	}

	@Override
	public void setSpannable(@NonNull Spannable spannable) {
		zoomableTextView.setVisibility(View.GONE);
		getLinesView().setVisibility(View.GONE);

		executor.execute(
				() -> {
					String source = spannable.toString();
					long lineCount = fastLineCount(source);

					mainHandler.post(
							() -> {
								zoomableTextView.setText(spannable);
								getLinesView().setLineCount(lineCount);
								getLinesView().setMinimumWidth(1000);

								zoomableTextView.setVisibility(View.VISIBLE);
								getLinesView().setVisibility(View.VISIBLE);
								syncLinesViewAfterLayout();

								if (hideProgressCallback != null) {
									hideProgressCallback.run();
								}

								executor.execute(
										() -> {
											float maxWidth = calculateMaxWidth(source);
											mainHandler.post(
													() -> {
														int scaledWidth =
																(int)
																		(maxWidth
																				* zoomableTextView
																						.getScaleFactor());
														getLinesView().setMinimumWidth(scaledWidth);
													});
										});
							});
				});
	}

	@Override
	public TextView getSourceView() {
		return zoomableTextView;
	}

	private void syncLinesView() {
		float scale = zoomableTextView.getScaleFactor();
		getLinesView().setScaleX(scale);
		getLinesView().setScaleY(scale);
		getLinesView().setTranslationX(zoomableTextView.getTranslateX());
		getLinesView().setTranslationY(zoomableTextView.getTranslateY());
		horizontalScrollView.setEnabled(scale <= 1.0f);
		requestLayout();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		executor.shutdown();
	}
}

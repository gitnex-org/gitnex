package org.mian.gitnex.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewParent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * @author mmarif
 */
public class ZoomableTextView extends AppCompatTextView {

	private ScaleGestureDetector scaleGestureDetector;
	private float scaleFactor = 1.0f;
	private static final float MIN_SCALE = 0.5f;
	private static final float MAX_SCALE = 3.0f;
	private float translateX = 0f, translateY = 0f;
	private float lastX, lastY;
	private boolean isPanning = false;

	public ZoomableTextView(@NonNull Context context) {
		super(context);
		init(context);
	}

	public ZoomableTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ZoomableTextView(
			@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
		setTextIsSelectable(true);
		setScaleX(scaleFactor);
		setScaleY(scaleFactor);
		setTranslationX(translateX);
		setTranslationY(translateY);
		setTextColor(0xFF000000);
		setPivotX(0f);
		setPivotY(0f);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = scaleGestureDetector.onTouchEvent(event);
		int action = event.getActionMasked();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				lastX = event.getX();
				lastY = event.getY();
				if (scaleFactor > 1.0f) {
					isPanning = true;
					requestParentDisallowIntercept(true);
					return true;
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (isPanning && event.getPointerCount() == 1) {
					float dx = event.getX() - lastX;
					float dy = event.getY() - lastY;

					float contentWidth = getContentWidth();
					float contentHeight = getContentHeight();
					float scaledWidth = contentWidth * scaleFactor;
					float scaledHeight = contentHeight * scaleFactor;

					float minX = Math.min(0, -(scaledWidth - getWidth()));
					float maxX = Math.max(0, getWidth() - scaledWidth);
					float minY = Math.min(0, -(scaledHeight - getHeight()));
					float maxY = Math.max(0, getHeight() - scaledHeight);

					translateX = Math.max(minX, Math.min(maxX, translateX + dx));
					translateY = Math.max(minY, Math.min(maxY, translateY + dy));

					setTranslationX(translateX);
					setTranslationY(translateY);

					lastX = event.getX();
					lastY = event.getY();
					return true;
				}
				break;

			case MotionEvent.ACTION_POINTER_DOWN:
				requestParentDisallowIntercept(true);
				return true;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				isPanning = false;
				requestParentDisallowIntercept(false);
				break;
		}

		return handled || super.onTouchEvent(event);
	}

	private void requestParentDisallowIntercept(boolean disallow) {
		ViewParent parent = getParent();
		while (parent != null) {
			parent.requestDisallowInterceptTouchEvent(disallow);
			parent = parent.getParent();
		}
	}

	private float getContentWidth() {
		if (getLayout() == null) return getMeasuredWidth();
		float maxWidth = 0;
		int lineCount = getLineCount();
		for (int i = 0; i < Math.min(lineCount, 50); i++) {
			float lineWidth = getLayout().getLineWidth(i);
			maxWidth = Math.max(maxWidth, lineWidth);
		}
		return maxWidth;
	}

	private float getContentHeight() {
		if (getLayout() == null || getLineCount() == 0) return getMeasuredHeight();
		return getLayout().getHeight();
	}

	public float getScaleFactor() {
		return scaleFactor;
	}

	public float getTranslateX() {
		return translateX;
	}

	public float getTranslateY() {
		return translateY;
	}

	public void resetZoom() {
		scaleFactor = 1.0f;
		translateX = 0f;
		translateY = 0f;
		setScaleX(scaleFactor);
		setScaleY(scaleFactor);
		setTranslationX(translateX);
		setTranslationY(translateY);
		isPanning = false;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
			requestParentDisallowIntercept(true);
			return true;
		}

		@Override
		public boolean onScale(@NonNull ScaleGestureDetector detector) {
			float previousScale = scaleFactor;
			float newScale = scaleFactor * detector.getScaleFactor();
			scaleFactor = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));

			float focusX = detector.getFocusX();
			float focusY = detector.getFocusY();

			float contentWidth = getContentWidth();
			float contentHeight = getContentHeight();
			float scaledWidth = contentWidth * scaleFactor;
			float scaledHeight = contentHeight * scaleFactor;

			float minX = Math.min(0, -(scaledWidth - getWidth()));
			float maxX = Math.max(0, getWidth() - scaledWidth);
			float minY = Math.min(0, -(scaledHeight - getHeight()));
			float maxY = Math.max(0, getHeight() - scaledHeight);

			float scaleChange = scaleFactor / previousScale;
			float dx = (focusX - translateX) * (1 - scaleChange);
			float dy = (focusY - translateY) * (1 - scaleChange);
			translateX += dx;
			translateY += dy;

			translateX = Math.max(minX, Math.min(maxX, translateX));
			translateY = Math.max(minY, Math.min(maxY, translateY));

			setScaleX(scaleFactor);
			setScaleY(scaleFactor);
			setTranslationX(translateX);
			setTranslationY(translateY);

			return true;
		}

		@Override
		public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
			if (scaleFactor <= 1.0f) {
				resetZoom();
			}
			requestParentDisallowIntercept(false);
		}
	}
}

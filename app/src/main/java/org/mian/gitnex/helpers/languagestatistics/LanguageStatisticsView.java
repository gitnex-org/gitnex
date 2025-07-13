package org.mian.gitnex.helpers.languagestatistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import java.util.ArrayList;

/**
 * @author mmarif
 */
public class LanguageStatisticsView extends View {

	private ArrayList<SeekbarItem> progressItemsList;
	private static final float CORNER_RADIUS = 8f;
	private final Paint progressPaint = new Paint();
	private final RectF progressRect = new RectF();

	public LanguageStatisticsView(Context context) {
		super(context);
		init();
	}

	public LanguageStatisticsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LanguageStatisticsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		progressItemsList = new ArrayList<>();
	}

	public void setData(ArrayList<SeekbarItem> progressItemsListInit) {
		this.progressItemsList = progressItemsListInit;
		invalidate();
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		super.onDraw(canvas);

		if (progressItemsList.isEmpty()) return;

		int width = getWidth();
		int height = getHeight();
		float lastX = 0f;

		for (int i = 0; i < progressItemsList.size(); i++) {
			SeekbarItem progressItem = progressItemsList.get(i);
			progressPaint.setColor(getResources().getColor(progressItem.color, null));

			float progressItemWidth = (progressItem.progressItemPercentage * width) / 100f;
			float progressItemRight = lastX + progressItemWidth;

			if (i == progressItemsList.size() - 1 && progressItemRight < width) {
				progressItemRight = width;
			}

			progressRect.set(lastX, 0f, progressItemRight, height);
			canvas.drawRoundRect(progressRect, CORNER_RADIUS, CORNER_RADIUS, progressPaint);

			lastX = progressItemRight;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(
				MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
	}
}

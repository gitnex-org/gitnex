package org.mian.gitnex.helpers.languagestatistics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import java.util.ArrayList;

/**
 * @author M M Arif
 */
public class LanguageStatisticsBar extends androidx.appcompat.widget.AppCompatSeekBar {

	private ArrayList<SeekbarItem> progressItemsList;

	public LanguageStatisticsBar(Context context) {
		super(context);
	}

	public LanguageStatisticsBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LanguageStatisticsBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void initData(ArrayList<SeekbarItem> progressItemsListInit) {
		this.progressItemsList = progressItemsListInit;
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	protected void onDraw(Canvas canvas) {

		if (progressItemsList.size() > 0) {

			int progressBarWidth = getWidth();
			int progressBarHeight = getHeight();
			int thumbOffSet = getThumbOffset();
			int lastProgressX = 0;
			int progressItemWidth, progressItemRight;

			for (int i = 0; i < progressItemsList.size(); i++) {

				SeekbarItem progressItem = progressItemsList.get(i);
				@SuppressLint("DrawAllocation")
				Paint progressPaint = new Paint();
				progressPaint.setColor(getResources().getColor(progressItem.color, null));

				progressItemWidth =
						(int) (progressItem.progressItemPercentage * progressBarWidth / 100);

				progressItemRight = lastProgressX + progressItemWidth;

				if (i == progressItemsList.size() - 1 && progressItemRight != progressBarWidth) {
					progressItemRight = progressBarWidth;
				}

				@SuppressLint("DrawAllocation")
				Rect progressRect = new Rect();
				progressRect.set(
						lastProgressX,
						thumbOffSet / 2,
						progressItemRight,
						progressBarHeight - thumbOffSet / 2);
				canvas.drawRect(progressRect, progressPaint);
				lastProgressX = progressItemRight;
			}
			super.onDraw(canvas);
		}
	}
}

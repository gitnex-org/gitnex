package org.mian.gitnex.helpers;

import android.graphics.Paint;
import android.graphics.Rect;

/**
 * @author M M Arif
 */
public class LabelWidthCalculator {

	public static int calculateLabelWidth(String text, int textSize, int paddingLeftRight) {

		Paint paint = new Paint();
		Rect rect = new Rect();

		paint.setTextSize(textSize);
		paint.getTextBounds(text, 0, text.length(), rect);

		return rect.width() + (paddingLeftRight * 2);
	}
}

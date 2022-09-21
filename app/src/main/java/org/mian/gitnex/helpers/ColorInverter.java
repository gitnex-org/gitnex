package org.mian.gitnex.helpers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/**
 * @author M M Arif
 */
public class ColorInverter {

	@ColorInt
	public int getContrastColor(@ColorInt int color) {

		double a =
				1
						- (0.2126 * Color.red(color)
										+ 0.7152 * Color.green(color)
										+ 0.0722 * Color.blue(color))
								/ 255;

		int d =
				(a < 0.30)
						? 30
						: // almost black
						255; // white

		return Color.rgb(d, d, d);
	}

	@ColorInt
	public int getBitmapContrastColor(@NonNull Bitmap bitmap) {

		int colorSum = 0;
		int divisionValue = 0;

		for (int height = 0; height < bitmap.getHeight(); height += 10) {
			for (int width = 0; width < bitmap.getWidth(); width += 10) {

				colorSum += bitmap.getPixel(width, height);
				divisionValue++;
			}
		}

		// Calculate average color
		return getContrastColor(colorSum / divisionValue);
	}

	@ColorInt
	public int getImageViewContrastColor(@NonNull ImageView imageView) {

		return getBitmapContrastColor(((BitmapDrawable) imageView.getDrawable()).getBitmap());
	}
}

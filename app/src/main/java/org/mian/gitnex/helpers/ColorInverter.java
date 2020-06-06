package org.mian.gitnex.helpers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;
import androidx.annotation.ColorInt;

/**
 * Author M M Arif
 */

public class ColorInverter {

    @ColorInt
    public int getContrastColor(@ColorInt int color) {

        double a = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;

        int d;
        if (a < 0.5) {
            d = 30; // almost black
        } else {
            d = 255; // white
        }

        return Color.rgb(d, d, d);
    }

    @ColorInt
    public int getImageViewContrastColor(ImageView imageView) {

    	if(imageView != null) {

		    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
		    return getContrastColor(bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2));

	    }
    	else {

    		return Color.rgb(255, 255, 255);
	    }

    }

}

package org.mian.gitnex.helpers;

import android.graphics.Color;
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
            d = 0; // black
        } else {
            d = 255; // white
        }

        return Color.rgb(d, d, d);
    }

}

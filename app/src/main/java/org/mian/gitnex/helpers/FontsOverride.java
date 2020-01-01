package org.mian.gitnex.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class FontsOverride {

    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName, String fontAssetName) {

        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);

    }

    private static void replaceFont(String staticTypefaceFieldName,
                                    final Typeface newTypeface) {

        try {

            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);

        }
        catch (NoSuchFieldException | IllegalAccessException e) {

            Log.e("error", Objects.requireNonNull(e.getMessage()));

        }

    }

}

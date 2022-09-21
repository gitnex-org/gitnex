package org.mian.gitnex.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author M M Arif
 */
public class FontsOverride {

	public static void setDefaultFont(Context context) {
		final Typeface regular = AppUtil.getTypeface(context);

		for (String field : new String[] {"DEFAULT", "MONOSPACE", "SERIF", "SANS_SERIF"}) {
			replaceFont(field, regular);
		}
	}

	private static void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface) {

		try {

			final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
			staticField.setAccessible(true);
			staticField.set(null, newTypeface);

		} catch (NoSuchFieldException | IllegalAccessException e) {

			Log.e("replaceFont", Objects.requireNonNull(e.getMessage()));
		}
	}
}

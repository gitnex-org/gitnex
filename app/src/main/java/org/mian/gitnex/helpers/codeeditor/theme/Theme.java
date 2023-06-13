package org.mian.gitnex.helpers.codeeditor.theme;

import android.content.Context;
import androidx.annotation.ColorRes;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.codeeditor.languages.LanguageElement;

/**
 * @author qwerty287
 * @author M M Arif
 */
public interface Theme {

	FiveColorsTheme FIVE_COLORS = new FiveColorsTheme();
	FiveColorsDarkTheme FIVE_COLORS_DARK = new FiveColorsDarkTheme();
	BlueMoonTheme BLUE_MOON_THEME = new BlueMoonTheme();
	BlueMoonDarkTheme BLUE_MOON_DARK_THEME = new BlueMoonDarkTheme();

	static Theme getDefaultTheme(Context context) {
		TinyDB tinyDB = TinyDB.getInstance(context);
		if (tinyDB.getInt("ceColorId") == 0) {
			return AppUtil.getColorFromAttribute(context, R.attr.isDark) == 1
					? FIVE_COLORS_DARK
					: FIVE_COLORS;
		} else {
			return AppUtil.getColorFromAttribute(context, R.attr.isDark) == 1
					? BLUE_MOON_DARK_THEME
					: BLUE_MOON_THEME;
		}
	}

	@ColorRes
	int getColor(LanguageElement element);

	@ColorRes
	int getDefaultColor();

	@ColorRes
	int getBackgroundColor();
}

package org.mian.gitnex.helpers.codeeditor.theme;

import android.content.Context;
import androidx.annotation.ColorRes;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.codeeditor.languages.LanguageElement;

/**
 * @author qwerty287
 */
public interface Theme {

	FiveColorsTheme FIVE_COLORS = new FiveColorsTheme();

	FiveColorsDarkTheme FIVE_COLORS_DARK = new FiveColorsDarkTheme();

	static Theme getDefaultTheme(Context context) {
		return AppUtil.getColorFromAttribute(context, R.attr.isDark) == 1
				? FIVE_COLORS_DARK
				: FIVE_COLORS;
	}

	@ColorRes
	int getColor(LanguageElement element);

	@ColorRes
	int getDefaultColor();

	@ColorRes
	int getBackgroundColor();
}

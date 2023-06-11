package org.mian.gitnex.helpers.codeeditor.theme;

import androidx.annotation.ColorRes;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.codeeditor.languages.LanguageElement;

/**
 * @author M M Arif
 */
public class BlueMoonTheme implements Theme {

	@Override
	@ColorRes
	public int getColor(LanguageElement element) {
		switch (element) {
			case HEX:
			case NUMBER:
			case KEYWORD:
			case OPERATION:
			case GENERIC:
				return R.color.moon_dark_blue;
			case CHAR:
			case STRING:
				return R.color.moon_dark_turquoise;
			case SINGLE_LINE_COMMENT:
			case MULTI_LINE_COMMENT:
				return R.color.moon_dark_grey;
			case ATTRIBUTE:
			case TODO_COMMENT:
			case ANNOTATION:
				return R.color.moon_deep_sky_blue;
			default:
				return R.color.moon_dark_black;
		}
	}

	@Override
	@ColorRes
	public int getDefaultColor() {
		return R.color.moon_dark_black;
	}

	@Override
	@ColorRes
	public int getBackgroundColor() {
		return R.color.moon_background_grey;
	}
}

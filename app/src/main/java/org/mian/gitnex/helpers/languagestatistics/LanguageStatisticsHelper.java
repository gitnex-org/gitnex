package org.mian.gitnex.helpers.languagestatistics;

import android.annotation.SuppressLint;

/**
 * @author M M Arif
 */
public class LanguageStatisticsHelper {

	@SuppressLint("DefaultLocale")
	public static String calculatePercentage(Long number, float total) {
		float percent = number * 100f / total;
		return String.format("%.1f", percent);
	}
}

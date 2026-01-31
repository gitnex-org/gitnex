package org.mian.gitnex.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import org.mian.gitnex.helpers.TinyDB;

/**
 * @author mmarif
 */
public class NotificationsBadge {

	private static final String PREFS_NAME = "notification_badge_prefs";
	private static final String KEY_PREFIX = "badge_count_";

	public static void saveBadgeCount(Context context, int accountId, int count) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().putInt(KEY_PREFIX + accountId, count).apply();
	}

	public static int getBadgeCount(Context context, int accountId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(KEY_PREFIX + accountId, 0);
	}

	public static void clearAllBadgeCounts(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefs.edit().clear().apply();
	}

	public static void updateBadgeUI(Context context, int count) {
		TinyDB tinyDB = TinyDB.getInstance(context);
		int accountId = tinyDB.getInt("currentActiveAccountId", -1);
		if (accountId > 0) {
			saveBadgeCount(context, accountId, count);
		}
	}
}

package org.mian.gitnex.helpers;

import android.content.Context;
import okhttp3.Credentials;

/**
 * Author M M Arif
 */

public class Authorization {

	public static String get(Context context) {

		TinyDB tinyDb = TinyDB.getInstance(context);
		String loginUid = tinyDb.getString("loginUid");

		if(tinyDb.getBoolean("basicAuthFlag") &&
			!tinyDb.getString("basicAuthPassword").isEmpty()) {

			return Credentials.basic(loginUid, tinyDb.getString("basicAuthPassword"));
		}

		return  "token " + tinyDb.getString(loginUid + "-token");

	}

	public static String getWeb(Context context) {

		TinyDB tinyDb = TinyDB.getInstance(context);
		return Credentials.basic("", tinyDb.getString(tinyDb.getString("loginUid") + "-token"));

	}

}

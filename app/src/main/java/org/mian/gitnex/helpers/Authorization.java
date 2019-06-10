package org.mian.gitnex.helpers;

import android.content.Context;
import org.mian.gitnex.util.TinyDB;
import okhttp3.Credentials;

/**
 * Author M M Arif
 */

public class Authorization {

    public static String returnAuthentication(Context context, String loginUid, String token) {

        TinyDB tinyDb = new TinyDB(context);

        String credential;

        if(tinyDb.getBoolean("basicAuthFlag")) {

            if (!tinyDb.getString("basicAuthPassword").isEmpty()) {

                credential = Credentials.basic(loginUid, tinyDb.getString("basicAuthPassword"));

            }
            else {

                credential = token;

            }
        }
        else {

            credential = token;

        }

        return credential;

    }

}

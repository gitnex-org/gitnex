package org.mian.gitnex.actions;

import android.content.Context;
import org.gitnex.tea4j.models.NotificationThread;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import java.io.IOException;
import java.util.Date;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Author opyale
 */

public class NotificationsActions {

	public enum NotificationStatus {READ, UNREAD, PINNED}

	private TinyDB tinyDB;
	private Context context;
	private String instanceToken;

	public NotificationsActions(Context context) {

		this.context = context;
		this.tinyDB = TinyDB.getInstance(context);

		String loginUid = tinyDB.getString("loginUid");

		instanceToken = "token " + tinyDB.getString(loginUid + "-token");

	}

	public void setNotificationStatus(NotificationThread notificationThread, NotificationStatus notificationStatus) throws IOException {

		Call<ResponseBody> call = RetrofitClient.getApiInterface(context)
			.markNotificationThreadAsRead(instanceToken, notificationThread.getId(), notificationStatus.name());

		if(!call.execute().isSuccessful()) {

			throw new IllegalStateException();
		}
	}

	public boolean setAllNotificationsRead(Date date) throws IOException {

		Call<ResponseBody> call = RetrofitClient.getApiInterface(context)
			.markNotificationThreadsAsRead(instanceToken, AppUtil.getTimestampFromDate(context, date), true,
				new String[]{"unread", "pinned"}, "read");

		return call.execute().isSuccessful();

	}

}

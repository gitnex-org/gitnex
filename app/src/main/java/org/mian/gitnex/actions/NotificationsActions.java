package org.mian.gitnex.actions;

import android.content.Context;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.NotificationThread;
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
	private String instanceUrl;
	private String instanceToken;

	public NotificationsActions(Context context) {

		this.context = context;
		this.tinyDB = new TinyDB(context);

		String loginUid = tinyDB.getString("loginUid");

		instanceUrl = tinyDB.getString("instanceUrl");
		instanceToken = "token " + tinyDB.getString(loginUid + "-token");

	}

	public void setNotificationStatus(NotificationThread notificationThread, NotificationStatus notificationStatus) throws IOException {

		Call<ResponseBody> call = RetrofitClient.getInstance(instanceUrl, context).getApiInterface()
			.markNotificationThreadAsRead(instanceToken, notificationThread.getId(), notificationStatus.name());

		if(!call.execute().isSuccessful()) {

			throw new IllegalStateException();
		}
	}

	public boolean setAllNotificationsRead(Date date) throws IOException {

		Call<ResponseBody> call = RetrofitClient.getInstance(instanceUrl, context).getApiInterface()
			.markNotificationThreadsAsRead(instanceToken, AppUtil.getTimestampFromDate(context, date), true,
				new String[]{"unread", "pinned"}, "read");

		return call.execute().isSuccessful();

	}

}

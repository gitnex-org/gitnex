package org.mian.gitnex.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.gitnex.tea4j.v2.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author opyale
 */

public class NotificationsWorker extends Worker {

	private final Context context;
	private final TinyDB tinyDB;
	private final Map<UserAccount, Map<String, String>> userAccounts;

	public NotificationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

		super(context, workerParams);

		UserAccountsApi userAccountsApi = BaseApi.getInstance(context, UserAccountsApi.class);

		this.context = context;
		this.tinyDB = TinyDB.getInstance(context);
		this.userAccounts = new HashMap<>(userAccountsApi.getCount());

		for(UserAccount userAccount : userAccountsApi.loggedInUserAccounts()) {

			// We do also accept empty values, since the server version was not saved properly in the beginning.
			if(userAccount.getServerVersion() == null || userAccount.getServerVersion().isEmpty() ||
				new Version(userAccount.getServerVersion()).higherOrEqual("1.12.3")) {

				Map<String, String> userAccountParameters = new HashMap<>();
				userAccountParameters.put("previousTimestamp", AppUtil.getTimestampFromDate(context, new Date()));

				userAccounts.put(userAccount, userAccountParameters);
			}
		}
	}

	@NonNull
	@Override
	public Result doWork() {
		pollingLoops();
		return Result.success();
	}

	/**
	 * Used to bypass the 15-minute limit of {@code WorkManager}.
	 */
	private void pollingLoops() {
		int notificationLoops = tinyDB.getInt("pollingDelayMinutes", Constants.defaultPollingDelay) < 15 ?
			Math.min(15 - tinyDB.getInt("pollingDelayMinutes", Constants.defaultPollingDelay), 10) : 1;

		for(int i = 0; i < notificationLoops; i++) {
			long startPollingTime = System.currentTimeMillis();

			startPolling();

			try {
				if(notificationLoops > 1 && i < (notificationLoops - 1)) {
					Thread.sleep(60000 - (System.currentTimeMillis() - startPollingTime));
				}
			} catch(InterruptedException ignored) {}
		}
	}

	private void startPolling() {
		for(UserAccount userAccount : userAccounts.keySet()) {
			Map<String, String> userAccountParameters = userAccounts.get(userAccount);

			try {
				assert userAccountParameters != null;
				Call<List<NotificationThread>> call = RetrofitClient
					.getApiInterface(context, userAccount.getInstanceUrl(), userAccount.getToken(), null)
					.notifyGetList(false, Arrays.asList("unread"), null, new Date(userAccountParameters.get("previousTimestamp")), null,
						null, 1);

				Response<List<NotificationThread>> response = call.execute();

				if(response.code() == 200 && response.body() != null) {
					List<NotificationThread> notificationThreads = response.body();
					if(!notificationThreads.isEmpty()) {
						sendNotifications(userAccount, notificationThreads);
					}
					userAccountParameters.put("previousTimestamp", AppUtil.getTimestampFromDate(context, new Date()));
				}
			} catch(Exception ignored) {}
		}
	}

	private void sendNotifications(@NonNull UserAccount userAccount, @NonNull List<NotificationThread> notificationThreads) {

		PendingIntent pendingIntent = getPendingIntent(userAccount);

		NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

		Notification summaryNotification = new NotificationCompat.Builder(context, Constants.mainNotificationChannelId)
				.setContentTitle(context.getString(R.string.newMessages, userAccount.getUserName()))
				.setContentText(String.format(context.getString(R.string.youHaveGotNewNotifications), notificationThreads.size()))
				.setSmallIcon(R.drawable.gitnex_transparent)
				.setGroup(userAccount.getUserName())
				.setGroupSummary(true)
				.setAutoCancel(true)
				.setContentIntent(pendingIntent)
				.build();

		notificationManagerCompat.notify(userAccount.getAccountId(), summaryNotification);

		for(NotificationThread notificationThread : notificationThreads) {

			String subjectUrl = notificationThread.getSubject().getUrl();
			String issueId = context.getResources().getString(R.string.hash) + subjectUrl.substring(subjectUrl.lastIndexOf("/") + 1);
			String notificationHeader = issueId + " " + notificationThread.getSubject().getTitle() + " " + String.format(context.getResources().getString(R.string.notificationExtraInfo), notificationThread.getRepository().getFullName(), notificationThread.getSubject().getType());

			NotificationCompat.Builder builder1 = getBaseNotificationBuilder()
				.setContentTitle(notificationHeader)
				.setGroup(userAccount.getUserName())
				.setContentIntent(pendingIntent);

			notificationManagerCompat.notify(Notifications.uniqueNotificationId(context), builder1.build());

		}
	}

	private NotificationCompat.Builder getBaseNotificationBuilder() {

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.mainNotificationChannelId)
			.setSmallIcon(R.drawable.gitnex_transparent)
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setAutoCancel(true);

		if(tinyDB.getBoolean("notificationsEnableLights", true)) {
			builder.setLights(tinyDB.getInt("notificationsLightColor", Color.GREEN), 1500, 1500);
		}

		if(tinyDB.getBoolean("notificationsEnableVibration", true)) {
			builder.setVibrate(Constants.defaultVibrationPattern);
		} else {
			builder.setVibrate(null);
		}

		return builder;
	}

	private PendingIntent getPendingIntent(@NonNull UserAccount userAccount) {

		Intent intent = new Intent(context, MainActivity.class);

		intent.putExtra("launchFragment", "notifications");
		intent.putExtra("switchAccountId", userAccount.getAccountId());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		return PendingIntent.getActivity(context, userAccount.getAccountId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

	}

}

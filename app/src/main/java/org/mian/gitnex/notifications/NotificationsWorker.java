package org.mian.gitnex.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.gitnex.tea4j.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Author opyale
 */

public class NotificationsWorker extends Worker {

	private static final int MAXIMUM_NOTIFICATIONS = 100;
	private static final long[] VIBRATION_PATTERN = new long[]{ 1000, 1000 };

	private Context context;
	private TinyDB tinyDB;

	public NotificationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

		super(context, workerParams);

		this.context = context;
		this.tinyDB = TinyDB.getInstance(context);
	}

	@NonNull
	@Override
	public Result doWork() {

		String token = "token " + tinyDB.getString(tinyDB.getString("loginUid") + "-token");

		int notificationLoops = tinyDB.getInt("pollingDelayMinutes", StaticGlobalVariables.defaultPollingDelay) >= 15 ? 1 : Math.min(15 - tinyDB.getInt("pollingDelayMinutes"), 10);

		for(int i=0; i<notificationLoops; i++) {

			long startPollingTime = System.currentTimeMillis();

			try {

				String previousRefreshTimestamp = tinyDB.getString("previousRefreshTimestamp", AppUtil.getTimestampFromDate(context, new Date()));

				Call<List<NotificationThread>> call = RetrofitClient
					.getApiInterface(context)
					.getNotificationThreads(token, false, new String[]{"unread"}, previousRefreshTimestamp,
						null, 1, MAXIMUM_NOTIFICATIONS);

				Response<List<NotificationThread>> response = call.execute();

				if(response.code() == 200) {

					assert response.body() != null;

					List<NotificationThread> notificationThreads = response.body();

					if(!notificationThreads.isEmpty()) {

						sendNotification(notificationThreads);
					}

					tinyDB.putString("previousRefreshTimestamp", AppUtil.getTimestampFromDate(context, new Date()));
				}
				else {

					Log.e("onError", String.valueOf(response.code()));
				}
			}
			catch(Exception e) {

				Log.e("onError", e.toString());
			}

			try {

				if(notificationLoops > 1 && i < (notificationLoops - 1)) {

					Thread.sleep(60000 - (System.currentTimeMillis() - startPollingTime));
				}
			}
			catch (InterruptedException ignored) {}
		}

		return Result.success();
	}

	private void sendNotification(List<NotificationThread> notificationThreads) {

		int summaryId = 0;
		PendingIntent pendingIntent = getPendingIntent();

		NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
		attachNotificationChannel(notificationManagerCompat);

		Notification summaryNotification = new NotificationCompat.Builder(context, context.getPackageName())
				.setContentTitle(context.getString(R.string.newMessages))
				.setContentText(String.format(context.getString(R.string.youHaveGotNewNotifications), notificationThreads.size()))
				.setSmallIcon(R.drawable.gitnex_transparent)
				.setGroup(context.getPackageName())
				.setGroupSummary(true)
				.setAutoCancel(true)
				.setContentIntent(pendingIntent)
				.build();

		notificationManagerCompat.notify(summaryId, summaryNotification);

		for(NotificationThread notificationThread : notificationThreads) {

			NotificationManagerCompat notificationManagerCompat1 = NotificationManagerCompat.from(context);
			attachNotificationChannel(notificationManagerCompat1);

			String subjectUrl = notificationThread.getSubject().getUrl();
			String issueId = context.getResources().getString(R.string.hash) + subjectUrl.substring(subjectUrl.lastIndexOf("/") + 1);
			String notificationHeader = issueId + " " + notificationThread.getSubject().getTitle() + " " + String.format(context.getResources().getString(R.string.notificationExtraInfo), notificationThread.getRepository().getFull_name(), notificationThread.getSubject().getType());

			NotificationCompat.Builder builder1 = getBaseNotificationBuilder()
				.setContentTitle(notificationHeader)
				.setGroup(context.getPackageName())
				.setContentIntent(pendingIntent);

			pushNotification(notificationManagerCompat1, builder1.build());
		}
	}

	private void pushNotification(NotificationManagerCompat notificationManagerCompat, Notification notification) {

		int previousNotificationId = tinyDB.getInt("previousNotificationId", 0);
		int nextPreviousNotificationId = previousNotificationId > 71951418 ? 0 : previousNotificationId + 1;

		tinyDB.putInt("previousNotificationId", nextPreviousNotificationId);
		notificationManagerCompat.notify(previousNotificationId, notification);
	}

	private void attachNotificationChannel(NotificationManagerCompat notificationManagerCompat) {

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

			NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName(), context.getString(R.string.app_name),
				NotificationManager.IMPORTANCE_DEFAULT);

			notificationChannel.setDescription(context.getString(R.string.notificationChannelDescription));

			if(tinyDB.getBoolean("notificationsEnableVibration", true)) {

				notificationChannel.setVibrationPattern(VIBRATION_PATTERN);
				notificationChannel.enableVibration(true);
			}
			else {

				notificationChannel.enableVibration(false);
			}

			if(tinyDB.getBoolean("notificationsEnableLights", true)) {

				notificationChannel.setLightColor(tinyDB.getInt("notificationsLightColor", Color.GREEN));
				notificationChannel.enableLights(true);
			}
			else {

				notificationChannel.enableLights(false);
			}

			notificationManagerCompat.createNotificationChannel(notificationChannel);
		}
	}

	private NotificationCompat.Builder getBaseNotificationBuilder() {

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getPackageName())
			.setSmallIcon(R.drawable.gitnex_transparent)
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setAutoCancel(true);

		if(tinyDB.getBoolean("notificationsEnableLights", true)) {

			builder.setLights(tinyDB.getInt("notificationsLightColor", Color.GREEN), 1500, 1500);
		}

		if(tinyDB.getBoolean("notificationsEnableVibration", true)) {

			builder.setVibrate(VIBRATION_PATTERN);
		}
		else {

			builder.setVibrate(null);
		}

		return builder;
	}

	private PendingIntent getPendingIntent() {

		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra("launchFragment", "notifications");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		return PendingIntent.getActivity(context, 0, intent, 0);
	}
}

package org.mian.gitnex.notifications;

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
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.NotificationThread;
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

		int notificationLoops = tinyDB.getInt("pollingDelayMinutes") >= 15 ? 1 : Math.min(15 - tinyDB.getInt("pollingDelayMinutes"), 10);

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
					Log.i("ReceivedNotifications", String.valueOf(notificationThreads.size()));

					if(!notificationThreads.isEmpty()) {

						for(NotificationThread notificationThread : notificationThreads) {

							sendNotification(notificationThread);
						}
					}

					tinyDB.putString("previousRefreshTimestamp", AppUtil.getTimestampFromDate(context, new Date()));

				} else {

					Log.e("onError", String.valueOf(response.code()));
				}
			} catch(Exception e) {

				Log.e("onError", e.toString());
			}

			try {

				if(notificationLoops > 1 && i < (notificationLoops - 1)) {

					Thread.sleep(60000 - (System.currentTimeMillis() - startPollingTime));
				}
			} catch (InterruptedException ignored) {}
		}

		return Result.success();

	}

	private void sendNotification(NotificationThread notificationThread) {

		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra("launchFragment", "notifications");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		if(notificationManager != null) {

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

				NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName(), context.getString(R.string.app_name),
					NotificationManager.IMPORTANCE_HIGH);

				notificationChannel.enableLights(true);
				notificationChannel.setLightColor(Color.GREEN);
				notificationChannel.enableVibration(true);
				notificationChannel.setVibrationPattern(VIBRATION_PATTERN);

				notificationManager.createNotificationChannel(notificationChannel);

			}

			String subjectUrl = notificationThread.getSubject().getUrl();
			String issueId = context.getResources().getString(R.string.hash) + subjectUrl.substring(subjectUrl.lastIndexOf("/") + 1);

			String notificationHeader = issueId + " " + notificationThread.getSubject().getTitle();
			String notificationBody = String.format(context.getResources().getString(R.string.notificationBody),
				notificationThread.getSubject().getType());

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getPackageName())
				.setSmallIcon(R.drawable.gitnex_transparent).setContentTitle(notificationHeader)
				.setContentText(notificationBody)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(pendingIntent).setVibrate(VIBRATION_PATTERN).setAutoCancel(true);

			int previousNotificationId = tinyDB.getInt("previousNotificationId", 0);
			int newPreviousNotificationId = previousNotificationId > 71951418 ? 0 : previousNotificationId + 1;

			tinyDB.putInt("previousNotificationId", newPreviousNotificationId);

			notificationManager.notify(previousNotificationId, builder.build());

		}
	}

}

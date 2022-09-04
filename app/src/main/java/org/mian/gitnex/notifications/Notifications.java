package org.mian.gitnex.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.TinyDB;
import java.util.concurrent.TimeUnit;

/**
 * @author opyale
 */

public class Notifications {

	public static int uniqueNotificationId(Context context) {

		TinyDB tinyDB = TinyDB.getInstance(context);

		int previousNotificationId = tinyDB.getInt("previousNotificationId", 0);
		int nextPreviousNotificationId = previousNotificationId == Integer.MAX_VALUE ? 0 : previousNotificationId + 1;

		tinyDB.putInt("previousNotificationId", nextPreviousNotificationId);
		return previousNotificationId;

	}

	public static void createChannels(Context context) {

		TinyDB tinyDB = TinyDB.getInstance(context);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

			// Delete old notification channels
			notificationManager.deleteNotificationChannel(context.getPackageName()); // TODO Can be removed in future versions

			// Create new notification channels
			NotificationChannel mainChannel = new NotificationChannel(Constants.mainNotificationChannelId, context.getString(R.string.mainNotificationChannelName), NotificationManager.IMPORTANCE_DEFAULT);
			mainChannel.setDescription(context.getString(R.string.mainNotificationChannelDescription));

			if(tinyDB.getBoolean("notificationsEnableVibration", true)) {
				mainChannel.setVibrationPattern(Constants.defaultVibrationPattern);
				mainChannel.enableVibration(true);
			}
			else {
				mainChannel.enableVibration(false);
			}

			if(tinyDB.getBoolean("notificationsEnableLights", true)) {
				mainChannel.setLightColor(tinyDB.getInt("notificationsLightColor", Color.GREEN));
				mainChannel.enableLights(true);
			}
			else {
				mainChannel.enableLights(false);
			}

			NotificationChannel downloadChannel = new NotificationChannel(Constants.downloadNotificationChannelId, context.getString(R.string.fileViewerNotificationChannelName), NotificationManager.IMPORTANCE_LOW);
			downloadChannel.setDescription(context.getString(R.string.fileViewerNotificationChannelDescription));

			notificationManager.createNotificationChannel(mainChannel);
			notificationManager.createNotificationChannel(downloadChannel);

		}
	}

	public static void stopWorker(Context context) {

		WorkManager.getInstance(context).cancelAllWorkByTag(Constants.notificationsWorkerId);
	}

	public static void startWorker(Context context) {

		TinyDB tinyDB = TinyDB.getInstance(context);

		if(tinyDB.getBoolean("notificationsEnabled", true)) {

			Constraints.Builder constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresBatteryNotLow(false).setRequiresStorageNotLow(false).setRequiresCharging(false);

			if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				constraints.setRequiresDeviceIdle(false);
			}

			int pollingDelayMinutes = Math.max(tinyDB.getInt("pollingDelayMinutes", Constants.defaultPollingDelay), 15);

			PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(NotificationsWorker.class, pollingDelayMinutes, TimeUnit.MINUTES).setConstraints(constraints.build())
				.addTag(Constants.notificationsWorkerId).build();

			WorkManager.getInstance(context).enqueueUniquePeriodicWork(Constants.notificationsWorkerId, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);

		}
	}

}

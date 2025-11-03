package org.mian.gitnex.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.concurrent.TimeUnit;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.TinyDB;

/**
 * @author opyale
 * @author mmarif
 */
public class Notifications {

	public static int uniqueNotificationId(Context context) {

		TinyDB tinyDB = TinyDB.getInstance(context);

		int previousNotificationId = tinyDB.getInt("previousNotificationId", 0);
		int nextPreviousNotificationId =
				previousNotificationId == Integer.MAX_VALUE ? 0 : previousNotificationId + 1;

		tinyDB.putInt("previousNotificationId", nextPreviousNotificationId);
		return previousNotificationId;
	}

	public static void createChannels(Context context) {

		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Create new notification channels
		NotificationChannel mainChannel =
				new NotificationChannel(
						Constants.mainNotificationChannelId,
						context.getString(R.string.mainNotificationChannelName),
						NotificationManager.IMPORTANCE_DEFAULT);
		mainChannel.setDescription(context.getString(R.string.mainNotificationChannelDescription));

		NotificationChannel downloadChannel =
				new NotificationChannel(
						Constants.downloadNotificationChannelId,
						context.getString(R.string.fileViewerNotificationChannelName),
						NotificationManager.IMPORTANCE_LOW);
		downloadChannel.setDescription(
				context.getString(R.string.fileViewerNotificationChannelDescription));

		notificationManager.createNotificationChannel(mainChannel);
		notificationManager.createNotificationChannel(downloadChannel);
	}

	public static void stopWorker(Context context) {

		WorkManager.getInstance(context).cancelAllWorkByTag(Constants.notificationsWorkerId);
	}

	public static void startWorker(Context context) {

		int delay;
		if (Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								context, AppDatabaseSettings.APP_NOTIFICATIONS_DELAY_KEY))
				== 0) {
			delay = 15;
		} else if (Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								context, AppDatabaseSettings.APP_NOTIFICATIONS_DELAY_KEY))
				== 1) {
			delay = 30;
		} else if (Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								context, AppDatabaseSettings.APP_NOTIFICATIONS_DELAY_KEY))
				== 2) {
			delay = 45;
		} else if (Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								context, AppDatabaseSettings.APP_NOTIFICATIONS_DELAY_KEY))
				== 3) {
			delay = 60;
		} else {
			delay = Constants.defaultPollingDelay;
		}

		if (Boolean.parseBoolean(
				AppDatabaseSettings.getSettingsValue(
						context, AppDatabaseSettings.APP_NOTIFICATIONS_KEY))) {

			if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
				MaterialAlertDialogBuilder materialAlertDialogBuilder =
						new MaterialAlertDialogBuilder(context)
								.setTitle(R.string.pageTitleNotifications)
								.setCancelable(false)
								.setMessage(context.getString(R.string.openAppSettings))
								.setNeutralButton(
										R.string.cancelButton,
										(dialog, which) -> {
											dialog.dismiss();
											AppDatabaseSettings.updateSettingsValue(
													context,
													"false",
													AppDatabaseSettings.APP_NOTIFICATIONS_KEY);
										})
								.setPositiveButton(
										R.string.isOpen,
										(dialog, which) -> {
											Intent intent =
													new Intent(
															Settings
																	.ACTION_APPLICATION_DETAILS_SETTINGS);
											Uri uri =
													Uri.fromParts(
															"package",
															context.getPackageName(),
															null);
											intent.setData(uri);
											context.startActivity(intent);
										});

				materialAlertDialogBuilder.create().show();
				return;
			}

			Constraints.Builder constraints =
					new Constraints.Builder()
							.setRequiredNetworkType(NetworkType.CONNECTED)
							.setRequiresBatteryNotLow(false)
							.setRequiresStorageNotLow(false)
							.setRequiresCharging(false);

			constraints.setRequiresDeviceIdle(false);

			PeriodicWorkRequest periodicWorkRequest =
					new PeriodicWorkRequest.Builder(
									NotificationsWorker.class, delay, TimeUnit.MINUTES)
							.setConstraints(constraints.build())
							.addTag(Constants.notificationsWorkerId)
							.build();

			WorkManager.getInstance(context)
					.enqueueUniquePeriodicWork(
							Constants.notificationsWorkerId,
							ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
							periodicWorkRequest);
		}
	}
}

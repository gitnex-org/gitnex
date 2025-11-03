package org.mian.gitnex.notifications;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gitnex.tea4j.v2.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author opyale
 * @author mmarif
 */
public class NotificationsWorker extends Worker {

	private final Context context;
	private final Map<UserAccount, Map<String, String>> userAccounts;

	public NotificationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

		super(context, workerParams);

		UserAccountsApi userAccountsApi = BaseApi.getInstance(context, UserAccountsApi.class);

		this.context = context;
		assert userAccountsApi != null;
		this.userAccounts = new HashMap<>(userAccountsApi.getCount());

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

		ZonedDateTime zdt = ZonedDateTime.now();
		zdt = zdt.minusMinutes(delay);
		String previousTimestamp = String.valueOf(zdt.toOffsetDateTime());

		for (UserAccount userAccount : userAccountsApi.loggedInUserAccounts()) {

			// We do also accept empty values, since the server version was not saved properly in
			// the beginning.
			if (userAccount.getServerVersion() == null
					|| userAccount.getServerVersion().isEmpty()
					|| new Version(userAccount.getServerVersion()).higherOrEqual("1.12.3")) {

				Map<String, String> userAccountParameters = new HashMap<>();
				userAccountParameters.put("previousTimestamp", previousTimestamp);

				userAccounts.put(userAccount, userAccountParameters);
			}
		}
	}

	@NonNull @Override
	public Result doWork() {
		startPolling();
		return Result.success();
	}

	private void startPolling() {

		for (UserAccount userAccount : userAccounts.keySet()) {
			Map<String, String> userAccountParameters = userAccounts.get(userAccount);
			assert userAccountParameters != null;

			try {
				Call<List<NotificationThread>> call =
						RetrofitClient.getApiInterface(
										context,
										userAccount.getInstanceUrl(),
										"token " + userAccount.getToken(),
										null)
								.notifyGetList2(
										false,
										List.of("unread"),
										null,
										userAccountParameters.get("previousTimestamp"),
										null,
										1,
										25);

				Response<List<NotificationThread>> response = call.execute();

				if (response.code() == 200 && response.body() != null) {
					List<NotificationThread> notificationThreads = response.body();
					if (!notificationThreads.isEmpty()) {
						sendNotifications(userAccount, notificationThreads);
					}
				}
			} catch (Exception ignored) {
			}
		}
	}

	private boolean shouldShowNotification(Context context, long notificationId) {
		TinyDB tinyDB = TinyDB.getInstance(context);
		long lastShownId = tinyDB.getLong("lastShownNotificationId", 0L);

		// Only show if this notification is newer than the last one we showed
		boolean shouldShow = notificationId > lastShownId;

		if (shouldShow) {
			tinyDB.putLong("lastShownNotificationId", notificationId);
		}

		return shouldShow;
	}

	private void sendNotifications(
			@NonNull UserAccount userAccount,
			@NonNull List<NotificationThread> notificationThreads) {

		PendingIntent pendingIntent = getPendingIntent(userAccount);

		NotificationManagerCompat notificationManagerCompat =
				NotificationManagerCompat.from(context);

		Notification summaryNotification =
				new NotificationCompat.Builder(context, Constants.mainNotificationChannelId)
						.setContentTitle(
								context.getString(R.string.newMessages, userAccount.getUserName()))
						.setContentText(
								String.format(
										context.getString(R.string.youHaveGotNewNotifications),
										notificationThreads.size()))
						.setSmallIcon(R.drawable.gitnex_transparent)
						.setGroup(userAccount.getUserName())
						.setGroupSummary(true)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true)
						.build();

		if (ActivityCompat.checkSelfPermission(
						getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
				!= PackageManager.PERMISSION_GRANTED) {
			MaterialAlertDialogBuilder materialAlertDialogBuilder =
					new MaterialAlertDialogBuilder(context)
							.setTitle(R.string.pageTitleNotifications)
							.setMessage(context.getString(R.string.openAppSettings))
							.setNeutralButton(
									R.string.cancelButton, (dialog, which) -> dialog.dismiss())
							.setPositiveButton(
									R.string.isOpen,
									(dialog, which) -> {
										Intent intent =
												new Intent(
														Settings
																.ACTION_APPLICATION_DETAILS_SETTINGS);
										Uri uri =
												Uri.fromParts(
														"package", context.getPackageName(), null);
										intent.setData(uri);
										context.startActivity(intent);
									});

			materialAlertDialogBuilder.create().show();
			return;
		}

		notificationManagerCompat.notify(userAccount.getAccountId(), summaryNotification);

		for (NotificationThread notificationThread : notificationThreads) {

			if (!shouldShowNotification(context, notificationThread.getId())) {
				continue; // Skip if already shown
			}

			String subjectUrl = notificationThread.getSubject().getUrl();
			String issueId =
					context.getResources().getString(R.string.hash)
							+ subjectUrl.substring(subjectUrl.lastIndexOf("/") + 1);
			String notificationHeader =
					issueId
							+ " "
							+ notificationThread.getSubject().getTitle()
							+ " "
							+ String.format(
									context.getResources()
											.getString(R.string.notificationExtraInfo),
									notificationThread.getRepository().getFullName(),
									notificationThread.getSubject().getType());

			NotificationCompat.Builder builder1 =
					getBaseNotificationBuilder()
							.setContentTitle(notificationHeader)
							.setGroup(userAccount.getUserName())
							.setContentIntent(pendingIntent);

			notificationManagerCompat.notify(
					Notifications.uniqueNotificationId(context), builder1.build());
		}
	}

	private NotificationCompat.Builder getBaseNotificationBuilder() {

		return new NotificationCompat.Builder(context, Constants.mainNotificationChannelId)
				.setSmallIcon(R.drawable.gitnex_transparent)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setCategory(NotificationCompat.CATEGORY_MESSAGE)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setAutoCancel(true);
	}

	private PendingIntent getPendingIntent(@NonNull UserAccount userAccount) {

		Intent intent = new Intent(context, MainActivity.class);

		intent.putExtra("launchFragment", "notifications");
		intent.putExtra("switchAccountId", userAccount.getAccountId());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addNextIntentWithParentStack(intent);

		return stackBuilder.getPendingIntent(
				1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
	}
}

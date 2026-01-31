package org.mian.gitnex.notifications;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.gitnex.tea4j.v2.models.NotificationCount;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.TinyDB;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class NotificationsBadgeWorker extends Worker {

	private final Context context;

	public NotificationsBadgeWorker(@NonNull Context context, @NonNull WorkerParameters params) {
		super(context, params);
		this.context = context;
	}

	@NonNull @Override
	public Result doWork() {
		updateNotificationBadgeForAllAccounts();
		return Result.success();
	}

	private void updateNotificationBadgeForAllAccounts() {
		UserAccountsApi userAccountsApi = BaseApi.getInstance(context, UserAccountsApi.class);
		if (userAccountsApi == null) return;

		for (UserAccount account : userAccountsApi.loggedInUserAccounts()) {
			updateBadgeForAccount(account);
		}
	}

	private void updateBadgeForAccount(UserAccount account) {
		try {
			Call<NotificationCount> call =
					RetrofitClient.getApiInterface(
									context,
									account.getInstanceUrl(),
									"token " + account.getToken(),
									null)
							.notifyNewAvailable();

			Response<NotificationCount> response = call.execute();

			if (response.isSuccessful() && response.body() != null) {
				int newCount = Math.toIntExact(response.body().getNew());

				NotificationsBadge.saveBadgeCount(context, account.getAccountId(), newCount);

				if (isCurrentActiveAccount(account)) {
					NotificationsBadge.updateBadgeUI(context, newCount);
				}
			} else {
				NotificationsBadge.saveBadgeCount(context, account.getAccountId(), 0);
				if (isCurrentActiveAccount(account)) {
					NotificationsBadge.updateBadgeUI(context, 0);
				}
			}
		} catch (Exception ignored) {
		}
	}

	private boolean isCurrentActiveAccount(UserAccount account) {
		TinyDB tinyDB = TinyDB.getInstance(context);
		int currentAccountId = tinyDB.getInt("currentActiveAccountId", -1);
		return account.getAccountId() == currentAccountId;
	}
}

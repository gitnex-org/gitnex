package org.mian.gitnex.database.api;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.UserAccountsDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import java.util.List;

/**
 * Author M M Arif
 */

public class UserAccountsApi {

	private static UserAccountsDao userAccountsDao;
	private static UserAccount userAccount;
	private static Integer checkAccount;

	public UserAccountsApi(Context context) {

		GitnexDatabase db;
		db = GitnexDatabase.getDatabaseInstance(context);
		userAccountsDao = db.userAccountsDao();
	}

	public void insertNewAccount(String accountName, String instanceUrl, String userName, String token, String serverVersion) {

		UserAccount userAccount = new UserAccount();
		userAccount.setAccountName(accountName);
		userAccount.setInstanceUrl(instanceUrl);
		userAccount.setUserName(userName);
		userAccount.setToken(token);
		userAccount.setServerVersion(serverVersion);

		insertNewAccountAsync(userAccount);
	}

	private static void insertNewAccountAsync(final UserAccount userAccount) {

		new Thread(() -> userAccountsDao.newAccount(userAccount)).start();
	}

	public static void updateServerVersion(final String serverVersion, final int accountId) {

		new Thread(() -> userAccountsDao.updateServerVersion(serverVersion, accountId)).start();
	}

	public void updateToken(final int accountId, final String token) {

		new Thread(() -> userAccountsDao.updateAccountToken(accountId, token)).start();
	}

	public void updateTokenByAccountName(final String accountName, final String token) {

		new Thread(() -> userAccountsDao.updateAccountTokenByAccountName(accountName, token)).start();
	}

	public UserAccount getAccountData(String accountName) {

		try {

			Thread thread = new Thread(() -> userAccount = userAccountsDao.fetchRowByAccount_(accountName));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.userAccountsRepository, e.toString());
		}

		return userAccount;
	}

	public Integer getCount(String accountName) {

		try {

			Thread thread = new Thread(() -> checkAccount = userAccountsDao.getCount(accountName));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(StaticGlobalVariables.userAccountsRepository, e.toString());
		}

		return checkAccount;
	}

	public LiveData<List<UserAccount>> getAllAccounts() {

		return userAccountsDao.fetchAllAccounts();
	}

	public void deleteAccount(final int accountId) {

		new Thread(() -> userAccountsDao.deleteAccount(accountId)).start();
	}

}

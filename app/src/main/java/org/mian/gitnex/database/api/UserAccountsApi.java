package org.mian.gitnex.database.api;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.UserAccountsDao;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.Constants;
import java.util.List;

/**
 * Author M M Arif
 */

public class UserAccountsApi {

	private static UserAccountsDao userAccountsDao;
	private static UserAccount userAccount;
	private static List<UserAccount> userAccounts;
	private static Integer checkAccount;
	private static long accountId;

	public UserAccountsApi(Context context) {

		GitnexDatabase db;
		db = GitnexDatabase.getDatabaseInstance(context);
		userAccountsDao = db.userAccountsDao();
	}

	public long insertNewAccount(String accountName, String instanceUrl, String userName, String token, String serverVersion) {

		UserAccount userAccount = new UserAccount();
		userAccount.setAccountName(accountName);
		userAccount.setInstanceUrl(instanceUrl);
		userAccount.setUserName(userName);
		userAccount.setToken(token);
		userAccount.setServerVersion(serverVersion);

		return insertNewAccountAsync(userAccount);
	}

	private static long insertNewAccountAsync(final UserAccount userAccount) {

		try {

			Thread thread = new Thread(() -> accountId = userAccountsDao.newAccount(userAccount));
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(Constants.userAccountsApi, e.toString());
		}

		return accountId;
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

			Log.e(Constants.userAccountsApi, e.toString());
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

			Log.e(Constants.userAccountsApi, e.toString());
		}

		return checkAccount;
	}

	public LiveData<List<UserAccount>> getAllAccounts() {

		return userAccountsDao.fetchAllAccounts();
	}

	public List<UserAccount> usersAccounts() {

		try {

			Thread thread = new Thread(() -> userAccounts = userAccountsDao.userAccounts());
			thread.start();
			thread.join();
		}
		catch(InterruptedException e) {

			Log.e(Constants.userAccountsApi, e.toString());
		}

		return userAccounts;
	}

	public void deleteAccount(final int accountId) {

		new Thread(() -> userAccountsDao.deleteAccount(accountId)).start();
	}

}

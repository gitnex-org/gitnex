package org.mian.gitnex.database.api;

import android.content.Context;
import androidx.lifecycle.LiveData;
import org.mian.gitnex.database.dao.UserAccountsDao;
import org.mian.gitnex.database.models.UserAccount;
import java.util.List;

/**
 * Author M M Arif
 */

public class UserAccountsApi extends BaseApi {

	private final UserAccountsDao userAccountsDao;

	UserAccountsApi(Context context) {
		super(context);
		userAccountsDao = gitnexDatabase.userAccountsDao();
	}

	public long createNewAccount(String accountName, String instanceUrl, String userName, String token, String serverVersion) {

		UserAccount userAccount = new UserAccount();
		userAccount.setAccountName(accountName);
		userAccount.setInstanceUrl(instanceUrl);
		userAccount.setUserName(userName);
		userAccount.setToken(token);
		userAccount.setServerVersion(serverVersion);
		userAccount.setLoggedIn(true);

		return userAccountsDao.createAccount(userAccount);

	}

	public void updateServerVersion(final String serverVersion, final int accountId) {
		executorService.execute(() -> userAccountsDao.updateServerVersion(serverVersion, accountId));
	}

	public void updateToken(final int accountId, final String token) {
		executorService.execute(() -> userAccountsDao.updateAccountToken(accountId, token));
	}

	public void updateTokenByAccountName(final String accountName, final String token) {
		executorService.execute(() -> userAccountsDao.updateAccountTokenByAccountName(accountName, token));
	}

	public void updateUsername(final int accountId, final String newName) {
		executorService.execute(() -> userAccountsDao.updateUserName(newName, accountId));
	}

	public UserAccount getAccountByName(String accountName) {
		return userAccountsDao.getAccountByName(accountName);
	}

	public UserAccount getAccountById(int accountId) {
		return userAccountsDao.getAccountById(accountId);
	}

	public Integer getCount() {
		return userAccountsDao.getCount();
	}

	public Boolean userAccountExists(String accountName) {
		return userAccountsDao.userAccountExists(accountName);
	}

	public LiveData<List<UserAccount>> getAllAccounts() {
		return userAccountsDao.getAllAccounts();
	}

	public LiveData<List<UserAccount>> getAllLoggedInAccounts() {
		return userAccountsDao.getAllLoggedInAccounts();
	}

	public List<UserAccount> usersAccounts() {
		return userAccountsDao.userAccounts();
	}

	public List<UserAccount> loggedInUserAccounts() {
		return userAccountsDao.loggedInUserAccounts();
	}

	public void deleteAccount(final int accountId) {
		executorService.execute(() -> userAccountsDao.deleteAccount(accountId));
	}

	public void logout(int accountId) {
		userAccountsDao.logout(accountId);
	}

	public void login(int accountId) {
		executorService.execute(() -> userAccountsDao.login(accountId));
	}

}

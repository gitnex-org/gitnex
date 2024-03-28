package org.mian.gitnex.helpers.contexts;

import android.content.Context;
import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import okhttp3.Credentials;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.Version;

/**
 * @author qwerty287
 */
public class AccountContext implements Serializable {

	private UserAccount account;
	private User userInfo;

	public AccountContext(UserAccount account) {
		this.account = account;
	}

	public static AccountContext fromId(int id, Context context) {
		return new AccountContext(
				Objects.requireNonNull(UserAccountsApi.getInstance(context, UserAccountsApi.class))
						.getAccountById(id));
	}

	public UserAccount getAccount() {

		return account;
	}

	public void setAccount(UserAccount account) {

		this.account = account;
	}

	public String getAuthorization() {
		return "token " + account.getToken();
	}

	public String getWebAuthorization() {
		return Credentials.basic("", account.getToken());
	}

	public Version getServerVersion() {
		return new Version(account.getServerVersion());
	}

	public boolean requiresVersion(String version) {
		return getServerVersion().higherOrEqual(version);
	}

	public int getDefaultPageLimit() {
		return getAccount().getDefaultPagingNumber();
	}

	public int getMaxPageLimit() {
		return getAccount().getMaxResponseItems();
	}

	public User getUserInfo() {

		return userInfo;
	}

	public void setUserInfo(User userInfo) {

		this.userInfo = userInfo;
	}

	public String getFullName() {
		return userInfo != null
				? !userInfo.getFullName().isEmpty() ? userInfo.getFullName() : userInfo.getLogin()
				: account.getUserName();
	}

	public File getCacheDir(Context context) {

		assert account.getAccountName() != null;
		return new File(context.getCacheDir() + "responses", account.getAccountName());
	}

	public File getPicassoCacheDir(Context context) {

		assert account.getAccountName() != null;
		return new File(context.getCacheDir() + "/picasso_cache/", account.getAccountName());
	}
}

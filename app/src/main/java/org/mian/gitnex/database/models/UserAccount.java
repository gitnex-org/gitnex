package org.mian.gitnex.database.models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * @author mmarif
 */
@Entity(tableName = "userAccounts")
public class UserAccount implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private int accountId;

	@Nullable private String accountName;
	private String instanceUrl;
	private String userName;
	private String token;
	@Nullable private String serverVersion;
	private boolean isLoggedIn;
	private int maxResponseItems;
	private int defaultPagingNumber;
	private int maxAttachmentsSize;
	private int maxNumberOfAttachments;
	private String provider;
	@Nullable private String proxyAuthUsername;
	@Nullable private String proxyAuthPassword;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	@Nullable public String getAccountName() {
		return accountName;
	}

	public void setAccountName(@Nullable String accountName) {
		this.accountName = accountName;
	}

	public String getInstanceUrl() {
		return instanceUrl;
	}

	public void setInstanceUrl(String instanceUrl) {
		this.instanceUrl = instanceUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Nullable public String getServerVersion() {
		return serverVersion;
	}

	public void setServerVersion(@Nullable String serverVersion) {
		this.serverVersion = serverVersion;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		isLoggedIn = loggedIn;
	}

	public int getMaxResponseItems() {
		return maxResponseItems;
	}

	public void setMaxResponseItems(int maxResponseItems) {
		this.maxResponseItems = maxResponseItems;
	}

	public int getDefaultPagingNumber() {
		return defaultPagingNumber;
	}

	public void setDefaultPagingNumber(int defaultPagingNumber) {
		this.defaultPagingNumber = defaultPagingNumber;
	}

	public int getMaxAttachmentsSize() {
		return maxAttachmentsSize;
	}

	public void setMaxAttachmentsSize(int maxAttachmentsSize) {
		this.maxAttachmentsSize = maxAttachmentsSize;
	}

	public int getMaxNumberOfAttachments() {
		return maxNumberOfAttachments;
	}

	public void setMaxNumberOfAttachments(int maxNumberOfAttachments) {
		this.maxNumberOfAttachments = maxNumberOfAttachments;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	@Nullable public String getProxyAuthUsername() {
		return proxyAuthUsername;
	}

	public void setProxyAuthUsername(@Nullable String username) {
		this.proxyAuthUsername = username;
	}

	@Nullable public String getProxyAuthPassword() {
		return proxyAuthPassword;
	}

	public void setProxyAuthPassword(@Nullable String password) {
		this.proxyAuthPassword = password;
	}
}

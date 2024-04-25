package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import org.mian.gitnex.database.models.UserAccount;

/**
 * @author M M Arif
 */
@Dao
public interface UserAccountsDao {

	@Insert
	long createAccount(UserAccount userAccounts);

	@Query("SELECT * FROM UserAccounts ORDER BY accountId ASC")
	LiveData<List<UserAccount>> getAllAccounts();

	@Query("SELECT * FROM UserAccounts WHERE isLoggedIn = 1 ORDER BY accountId ASC")
	LiveData<List<UserAccount>> getAllLoggedInAccounts();

	@Query("SELECT * FROM UserAccounts ORDER BY accountId ASC")
	List<UserAccount> userAccounts();

	@Query("SELECT * FROM UserAccounts WHERE isLoggedIn = 1 ORDER BY accountId ASC")
	List<UserAccount> loggedInUserAccounts();

	@Query("SELECT COUNT(accountId) FROM UserAccounts WHERE isLoggedIn = 1")
	Integer getCount();

	@Query("SELECT COUNT(accountId) FROM UserAccounts WHERE accountName = :accountName LIMIT 1")
	Boolean userAccountExists(String accountName);

	@Query("SELECT * FROM UserAccounts WHERE accountName = :accountName LIMIT 1")
	UserAccount getAccountByName(String accountName);

	@Query("SELECT * FROM UserAccounts WHERE accountId = :accountId LIMIT 1")
	UserAccount getAccountById(int accountId);

	@Query("UPDATE UserAccounts SET serverVersion = :serverVersion WHERE accountId = :accountId")
	void updateServerVersion(String serverVersion, int accountId);

	@Query(
			"UPDATE UserAccounts SET maxResponseItems = :maxResponseItems, defaultPagingNumber = :defaultPagingNumber WHERE accountId = :accountId")
	void updateServerPagingLimit(int maxResponseItems, int defaultPagingNumber, int accountId);

	@Query(
			"UPDATE UserAccounts SET maxAttachmentsSize = :maxAttachmentsSize, maxNumberOfAttachments = :maxNumberOfAttachments WHERE accountId = :accountId")
	void updateGeneralAttachmentSettings(
			int maxAttachmentsSize, int maxNumberOfAttachments, int accountId);

	@Query("UPDATE UserAccounts SET accountName = :accountName WHERE accountId = :accountId")
	void updateAccountName(String accountName, int accountId);

	@Query("UPDATE UserAccounts SET token = :token WHERE accountId = :accountId")
	void updateAccountToken(int accountId, String token);

	@Query("UPDATE UserAccounts SET token = :token WHERE accountName = :accountName")
	void updateAccountTokenByAccountName(String accountName, String token);

	@Query(
			"UPDATE UserAccounts SET instanceUrl = :instanceUrl, token = :token WHERE accountId = :accountId")
	void updateHostInfo(String instanceUrl, String token, int accountId);

	@Query("UPDATE UserAccounts SET userName = :userName WHERE accountId = :accountId")
	void updateUserName(String userName, int accountId);

	@Query(
			"UPDATE UserAccounts SET instanceUrl = :instanceUrl, token = :token, userName = :userName, serverVersion = :serverVersion WHERE accountId = :accountId")
	void updateAll(
			String instanceUrl, String token, String userName, String serverVersion, int accountId);

	@Query("UPDATE UserAccounts SET isLoggedIn = 0 WHERE accountId = :accountId")
	void logout(int accountId);

	@Query("UPDATE UserAccounts SET isLoggedIn = 1 WHERE accountId = :accountId")
	void login(int accountId);

	@Query("DELETE FROM UserAccounts WHERE accountId = :accountId")
	void deleteAccount(int accountId);
}

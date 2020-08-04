package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import org.mian.gitnex.database.models.UserAccount;
import java.util.List;

/**
 * Author M M Arif
 */

@Dao
public interface UserAccountsDao {

    @Insert
    void newAccount(UserAccount userAccounts);

    @Query("SELECT * FROM UserAccounts ORDER BY accountId ASC")
    LiveData<List<UserAccount>> fetchAllAccounts();

    @Query("SELECT COUNT(accountId) FROM UserAccounts WHERE accountName = :accountName")
    Integer getCount(String accountName);

    @Query("SELECT * FROM UserAccounts WHERE accountName = :accountName")
    UserAccount fetchRowByAccount_(String accountName);

    @Query("SELECT * FROM UserAccounts WHERE accountId = :accountId")
    UserAccount fetchRowByAccountId(int accountId);

    @Query("UPDATE UserAccounts SET serverVersion = :serverVersion WHERE accountId = :accountId")
    void updateServerVersion(String serverVersion, int accountId);

    @Query("UPDATE UserAccounts SET accountName = :accountName WHERE accountId = :accountId")
    void updateAccountName(String accountName, int accountId);

    @Query("UPDATE UserAccounts SET token = :token WHERE accountId = :accountId")
    void updateAccountToken(int accountId, String token);

	@Query("UPDATE UserAccounts SET token = :token WHERE accountName = :accountName")
	void updateAccountTokenByAccountName(String accountName, String token);

    @Query("UPDATE UserAccounts SET instanceUrl = :instanceUrl, token = :token WHERE accountId = :accountId")
    void updateHostInfo(String instanceUrl, String token, int accountId);

    @Query("UPDATE UserAccounts SET userName = :userName WHERE accountId = :accountId")
    void updateUserName(String userName, int accountId);

    @Query("UPDATE UserAccounts SET instanceUrl = :instanceUrl, token = :token, userName = :userName, serverVersion = :serverVersion WHERE accountId = :accountId")
    void updateAll(String instanceUrl, String token, String userName, String serverVersion, int accountId);

    @Query("DELETE FROM UserAccounts WHERE accountId = :accountId")
    void deleteAccount(int accountId);

}

package org.mian.gitnex.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import org.mian.gitnex.database.models.AppSettings;

/**
 * @author M M Arif
 */
@Dao
public interface AppSettingsDao {

	@Insert
	long insertNewSetting(AppSettings appSettings);

	@Query("SELECT * FROM AppSettings")
	LiveData<List<AppSettings>> fetchAllSettings();

	@Query("SELECT * FROM AppSettings WHERE settingId = :settingId")
	AppSettings fetchSettingById(int settingId);

	@Query("SELECT * FROM AppSettings WHERE settingKey = :settingKey")
	AppSettings fetchSettingByKey(String settingKey);

	@Query("SELECT count(settingId) FROM AppSettings")
	Integer fetchTotalSettingsCount();

	@Query("SELECT count(settingId) FROM AppSettings WHERE settingKey = :settingKey")
	Integer fetchSettingCountByKey(String settingKey);

	@Query("UPDATE AppSettings SET settingValue = :settingValue WHERE settingKey = :settingKey")
	void updateSettingValueByKey(String settingValue, String settingKey);

	@Query("UPDATE AppSettings SET settingDefault = :settingDefault WHERE settingKey = :settingKey")
	void updateSettingDefaultByKey(String settingDefault, String settingKey);

	@Query("DELETE FROM AppSettings WHERE settingKey = :settingKey")
	void deleteBySettingKey(String settingKey);
}

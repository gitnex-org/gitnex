package org.mian.gitnex.database.models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * @author M M Arif
 */
@Entity(tableName = "appSettings")
public class AppSettings implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private int settingId;

	private String settingKey;
	private String settingValue;
	@Nullable private String settingDefault;

	public int getSettingId() {
		return settingId;
	}

	public void setSettingId(int settingId) {
		this.settingId = settingId;
	}

	@Nullable public String getSettingKey() {
		return settingKey;
	}

	public void setSettingKey(@Nullable String settingKey) {
		this.settingKey = settingKey;
	}

	public String getSettingValue() {
		return settingValue;
	}

	public void setSettingValue(String settingValue) {
		this.settingValue = settingValue;
	}

	public String getSettingDefault() {
		return settingDefault;
	}

	public void setSettingDefault(String settingDefault) {
		this.settingDefault = settingDefault;
	}
}

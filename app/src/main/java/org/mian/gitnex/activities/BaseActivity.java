package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author M M Arif
 */

public abstract class BaseActivity extends AppCompatActivity {

	protected TinyDB tinyDB;

	protected Context ctx = this;
	protected Context appCtx;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.appCtx = getApplicationContext();
		this.tinyDB = TinyDB.getInstance(appCtx);

		switch(tinyDB.getInt("themeId")) {

			case 1:

				tinyDB.putString("currentTheme", "light");
				setTheme(R.style.AppThemeLight);
				break;
			case 2:

				if(TimeHelper.timeBetweenHours(18, 6)) { // 6pm to 6am

					tinyDB.putString("currentTheme", "dark");
					setTheme(R.style.AppTheme);
				}
				else {

					tinyDB.putString("currentTheme", "light");
					setTheme(R.style.AppThemeLight);
				}
				break;
			case 3:

				tinyDB.putString("currentTheme", "light");
				setTheme(R.style.AppThemeRetro);
				break;
			case 4:
				if(TimeHelper.timeBetweenHours(18, 6)) { // 6pm to 6am

					tinyDB.putString("currentTheme", "dark");
					setTheme(R.style.AppTheme);
				}
				else {

					tinyDB.putString("currentTheme", "light");
					setTheme(R.style.AppThemeRetro);
				}
				break;
			case 5:

				tinyDB.putString("currentTheme", "dark");
				setTheme(R.style.AppThemePitchBlack);
				break;
			default:

				tinyDB.putString("currentTheme", "dark");
				setTheme(R.style.AppTheme);

		}

		AppUtil.setAppLocale(getResources(), tinyDB.getString("locale"));

	}

}



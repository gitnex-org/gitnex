package org.mian.gitnex.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.FontsOverride;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.util.TinyDB;

/**
 * Author M M Arif
 */

public abstract class BaseActivity extends AppCompatActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		final TinyDB tinyDb = new TinyDB(getApplicationContext());

		if(tinyDb.getInt("themeId") == 1) {
			setTheme(R.style.AppThemeLight);
		}
		else if(tinyDb.getInt("themeId") == 2) {

			boolean timeSetterFlag = TimeHelper.timeBetweenHours(18, 6); // 6pm to 6am

			if(timeSetterFlag) {
				setTheme(R.style.AppTheme);
			}
			else {
				setTheme(R.style.AppThemeLight);
			}

		}
		else {
			setTheme(R.style.AppTheme);
		}

		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceId());

		switch(tinyDb.getInt("customFontId", -1)) {

			case 0:
				FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/roboto.ttf");
				FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/roboto.ttf");
				FontsOverride.setDefaultFont(this, "SERIF", "fonts/roboto.ttf");
				FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/roboto.ttf");
				break;

			case 2:
				FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/sourcecodeproregular.ttf");
				FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/sourcecodeproregular.ttf");
				FontsOverride.setDefaultFont(this, "SERIF", "fonts/sourcecodeproregular.ttf");
				FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/sourcecodeproregular.ttf");
				break;

			default:
				FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/manroperegular.ttf");
				FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/manroperegular.ttf");
				FontsOverride.setDefaultFont(this, "SERIF", "fonts/manroperegular.ttf");
				FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/manroperegular.ttf");
				break;

		}

		// enabling counter badges by default
		if(tinyDb.getString("enableCounterBadgesInit").isEmpty()) {
			tinyDb.putBoolean("enableCounterBadges", true);
			tinyDb.putString("enableCounterBadgesInit", "yes");
		}

	}

	protected abstract int getLayoutResourceId();

}



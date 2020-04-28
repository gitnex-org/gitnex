package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.util.TinyDB;

/**
 * Author M M Arif
 */

public class SettingsReportsActivity extends BaseActivity {

	private Context appCtx;
	private View.OnClickListener onClickListener;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_reporting;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		TinyDB tinyDb = new TinyDB(appCtx);

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		Switch crashReportsSwitch = findViewById(R.id.crashReportsSwitch);

		if(tinyDb.getBoolean("crashReportingEnabled")) {
			crashReportsSwitch.setChecked(true);
		}
		else {
			crashReportsSwitch.setChecked(false);
		}

		// crash reports switcher
		crashReportsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {
				tinyDb.putBoolean("crashReportingEnabled", true);
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));
			}
			else {
				tinyDb.putBoolean("crashReportingEnabled", false);
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));
			}

		});

	}

	private void initCloseListener() {
		onClickListener = view -> {
			finish();
		};
	}

}

package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;

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

		SwitchMaterial crashReportsSwitch = findViewById(R.id.crashReportsSwitch);

		crashReportsSwitch.setChecked(tinyDb.getBoolean("crashReportingEnabled"));

		// crash reports switcher
		crashReportsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDb.putBoolean("crashReportingEnabled", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}

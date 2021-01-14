package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Toasty;

/**
 * Author M M Arif
 */

public class SettingsReportsActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_reporting;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		SwitchMaterial crashReportsSwitch = findViewById(R.id.crashReportsSwitch);

		crashReportsSwitch.setChecked(tinyDB.getBoolean("crashReportingEnabled"));

		// crash reports switcher
		crashReportsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("crashReportingEnabled", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}

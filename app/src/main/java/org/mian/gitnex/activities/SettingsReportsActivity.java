package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsReportsBinding;
import org.mian.gitnex.helpers.Toasty;

/**
 * Author M M Arif
 */

public class SettingsReportsActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_reports;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsReportsBinding activitySettingsReportsBinding = ActivitySettingsReportsBinding.inflate(getLayoutInflater());

		ImageView closeActivity = activitySettingsReportsBinding.close;

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		activitySettingsReportsBinding.crashReportsSwitch.setChecked(tinyDB.getBoolean("crashReportingEnabled"));

		// crash reports switcher
		activitySettingsReportsBinding.crashReportsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("crashReportingEnabled", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}

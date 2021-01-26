package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsDraftsBinding;
import org.mian.gitnex.helpers.Toasty;

/**
 * Author M M Arif
 */

public class SettingsDraftsActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_drafts;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsDraftsBinding activitySettingsDraftsBinding = ActivitySettingsDraftsBinding.inflate(getLayoutInflater());

		ImageView closeActivity = activitySettingsDraftsBinding.close;

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		activitySettingsDraftsBinding.commentsDeletionSwitch.setChecked(tinyDB.getBoolean("draftsCommentsDeletionEnabled"));

		// delete comments on submit switcher
		activitySettingsDraftsBinding.commentsDeletionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("draftsCommentsDeletionEnabled", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});

	}

	private void initCloseListener() { onClickListener = view -> finish(); }

}

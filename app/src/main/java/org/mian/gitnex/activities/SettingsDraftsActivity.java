package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author M M Arif
 */

public class SettingsDraftsActivity extends BaseActivity {

	private Context appCtx;
	private View.OnClickListener onClickListener;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_drafts;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		TinyDB tinyDb = new TinyDB(appCtx);

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		Switch commentsDeletionSwitch = findViewById(R.id.commentsDeletionSwitch);

		if(tinyDb.getBoolean("draftsCommentsDeletionEnabled")) {
			commentsDeletionSwitch.setChecked(true);
		}
		else {
			commentsDeletionSwitch.setChecked(false);
		}

		// delete comments on submit switcher
		commentsDeletionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {
				tinyDb.putBoolean("draftsCommentsDeletionEnabled", true);
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));
			}
			else {
				tinyDb.putBoolean("draftsCommentsDeletionEnabled", false);
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));
			}

		});

	}

	private void initCloseListener() { onClickListener = view -> finish(); }

}

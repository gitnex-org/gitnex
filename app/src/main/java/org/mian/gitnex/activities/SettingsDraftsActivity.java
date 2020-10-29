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

		SwitchMaterial commentsDeletionSwitch = findViewById(R.id.commentsDeletionSwitch);

		commentsDeletionSwitch.setChecked(tinyDb.getBoolean("draftsCommentsDeletionEnabled"));

		// delete comments on submit switcher
		commentsDeletionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDb.putBoolean("draftsCommentsDeletionEnabled", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});

	}

	private void initCloseListener() { onClickListener = view -> finish(); }

}

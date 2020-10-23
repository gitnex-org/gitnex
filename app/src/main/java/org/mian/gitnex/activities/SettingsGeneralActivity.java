package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsGeneralBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author M M Arif
 */

public class SettingsGeneralActivity extends BaseActivity {

	private ActivitySettingsGeneralBinding viewBinding;
	private Context appCtx;
	private View.OnClickListener onClickListener;

	private List<String> defaultScreen;
	private static int defaultScreenSelectedChoice = 0;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_general;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		viewBinding = ActivitySettingsGeneralBinding.inflate(getLayoutInflater());
		View view = viewBinding.getRoot();
		setContentView(view);

		TinyDB tinyDb = new TinyDB(appCtx);

		initCloseListener();
		viewBinding.close.setOnClickListener(onClickListener);

		String[] defaultScreen_ = {getResources().getString(R.string.generalDeepLinkSelectedText), getResources().getString(R.string.navRepos), getResources().getString(R.string.navOrgs), getResources().getString(R.string.pageTitleNotifications), getResources().getString(R.string.navExplore)};
		defaultScreen = new ArrayList<>(Arrays.asList(defaultScreen_));

		String[] linksArray = new String[defaultScreen.size()];
		defaultScreen.toArray(linksArray);

		if(!tinyDb.getString("defaultScreenStr").isEmpty()) {
			viewBinding.generalDeepLinkSelected.setText(tinyDb.getString("defaultScreenStr"));
		}

		if(defaultScreenSelectedChoice == 0) {
			defaultScreenSelectedChoice = tinyDb.getInt("defaultScreenId");
		}

		viewBinding.setDefaultLinkHandler.setOnClickListener(setDefaultLinkHandler -> {

			AlertDialog.Builder dlBuilder = new AlertDialog.Builder(SettingsGeneralActivity.this);
			dlBuilder.setTitle(R.string.linkSelectorDialogTitle);

			if(defaultScreenSelectedChoice != -1) {
				dlBuilder.setCancelable(true);
			}
			else {
				dlBuilder.setCancelable(false);
			}

			dlBuilder.setSingleChoiceItems(linksArray, defaultScreenSelectedChoice, (dialogInterfaceHomeScreen, i) -> {

				defaultScreenSelectedChoice = i;
				viewBinding.generalDeepLinkSelected.setText(linksArray[i]);
				tinyDb.putString("defaultScreenStr", linksArray[i]);
				tinyDb.putInt("defaultScreenId", i);

				dialogInterfaceHomeScreen.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog dlDialog = dlBuilder.create();
			dlDialog.show();

		});

	}

	private void initCloseListener() { onClickListener = view -> finish(); }
}

package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsGeneralBinding;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author M M Arif
 */

public class SettingsGeneralActivity extends BaseActivity {

	private ActivitySettingsGeneralBinding viewBinding;
	private View.OnClickListener onClickListener;

	private List<String> homeScreenList;
	private static int homeScreenSelectedChoice = 0;

	private List<String> linkHandlerDefaultScreen;
	private static int defaultLinkHandlerScreenSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivitySettingsGeneralBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		initCloseListener();
		viewBinding.close.setOnClickListener(onClickListener);

		// home screen
		String[] appHomeDefaultScreen = getResources().getStringArray(R.array.appDefaultHomeScreen);

		String[] appHomeDefaultScreenNew = getResources().getStringArray(R.array.appDefaultHomeScreenNew);

		if(getAccount().requiresVersion("1.12.3")) {

			appHomeDefaultScreen = appHomeDefaultScreenNew;
		}

		homeScreenList = new ArrayList<>(Arrays.asList(appHomeDefaultScreen));

		if(!((BaseActivity) ctx).getAccount().requiresVersion("1.14.0")) {
			homeScreenList.remove(8);
		}

		String[] homeScreenArray = new String[homeScreenList.size()];
		homeScreenList.toArray(homeScreenArray);

		if(homeScreenSelectedChoice == 0) {

			homeScreenSelectedChoice = tinyDB.getInt("homeScreenId", 0);
			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navMyRepos));
		}

		if(homeScreenSelectedChoice == 1) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.pageTitleStarredRepos));
		}
		else if(homeScreenSelectedChoice == 2) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navOrg));
		}
		else if(homeScreenSelectedChoice == 3) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navRepos));
		}
		else if(homeScreenSelectedChoice == 4) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navProfile));
		}
		else if(homeScreenSelectedChoice == 5) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.pageTitleExplore));
		}
		else if(homeScreenSelectedChoice == 6) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.titleDrafts));
		}
		else if(homeScreenSelectedChoice == 7) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.pageTitleNotifications));
		}
		else if(homeScreenSelectedChoice == 8) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navMyIssues));
		}

		viewBinding.homeScreenFrame.setOnClickListener(setDefaultHomeScreen -> {

			AlertDialog.Builder hsBuilder = new AlertDialog.Builder(SettingsGeneralActivity.this);

			hsBuilder.setTitle(R.string.settingsHomeScreenSelectorDialogTitle);
			hsBuilder.setCancelable(homeScreenSelectedChoice != -1);

			hsBuilder.setSingleChoiceItems(homeScreenArray, homeScreenSelectedChoice, (dialogInterfaceHomeScreen, i) -> {

				homeScreenSelectedChoice = i;
				viewBinding.homeScreenSelected.setText(homeScreenArray[i]);
				tinyDB.putInt("homeScreenId", i);

				dialogInterfaceHomeScreen.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog hsDialog = hsBuilder.create();
			hsDialog.show();
		});
		// home screen

		// link handler
		String[] linkHandlerDefaultScreenList = getResources().getStringArray(R.array.linkHandlerDefaultScreen);
		linkHandlerDefaultScreen = new ArrayList<>(Arrays.asList(linkHandlerDefaultScreenList));

		String[] linksArray = new String[linkHandlerDefaultScreen.size()];
		linkHandlerDefaultScreen.toArray(linksArray);

		defaultLinkHandlerScreenSelectedChoice = tinyDB.getInt("defaultScreenId");
		viewBinding.generalDeepLinkSelected.setText(linksArray[defaultLinkHandlerScreenSelectedChoice]);

		viewBinding.setDefaultLinkHandler.setOnClickListener(setDefaultLinkHandler -> {

			AlertDialog.Builder dlBuilder = new AlertDialog.Builder(SettingsGeneralActivity.this);
			dlBuilder.setTitle(R.string.linkSelectorDialogTitle);

			dlBuilder.setCancelable(defaultLinkHandlerScreenSelectedChoice != -1);

			dlBuilder.setSingleChoiceItems(linksArray, defaultLinkHandlerScreenSelectedChoice, (dialogInterfaceHomeScreen, i) -> {

				defaultLinkHandlerScreenSelectedChoice = i;
				viewBinding.generalDeepLinkSelected.setText(linksArray[i]);
				tinyDB.putInt("defaultScreenId", i);

				dialogInterfaceHomeScreen.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog dlDialog = dlBuilder.create();
			dlDialog.show();
		});
		// link handler

		// custom tabs
		viewBinding.switchTabs.setChecked(tinyDB.getBoolean("useCustomTabs"));
		viewBinding.switchTabs.setOnCheckedChangeListener((buttonView, isChecked) -> {
			tinyDB.putBoolean("useCustomTabs", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});
		viewBinding.customTabsFrame.setOnClickListener(v -> viewBinding.switchTabs.setChecked(!viewBinding.switchTabs.isChecked()));
		// custom tabs
	}

	private void initCloseListener() { onClickListener = view -> finish(); }
}

package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsGeneralBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
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

	private List<String> defaultScreen;
	private static int defaultLinkHandlerScreenSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivitySettingsGeneralBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		initCloseListener();
		viewBinding.close.setOnClickListener(onClickListener);

		// home screen
		String[] homeDefaultScreen_ = {getResources().getString(R.string.pageTitleMyRepos), getResources().getString(R.string.pageTitleStarredRepos), getResources().getString(R.string.pageTitleOrganizations),
			getResources().getString(R.string.pageTitleRepositories), getResources().getString(R.string.pageTitleProfile), getResources().getString(R.string.pageTitleExplore),
			getResources().getString(R.string.titleDrafts)};

		String[] homeDefaultScreenNew = {getResources().getString(R.string.pageTitleMyRepos), getResources().getString(R.string.pageTitleStarredRepos), getResources().getString(R.string.pageTitleOrganizations),
			getResources().getString(R.string.pageTitleRepositories), getResources().getString(R.string.pageTitleProfile), getResources().getString(R.string.pageTitleExplore),
			getResources().getString(R.string.titleDrafts), getResources().getString(R.string.pageTitleNotifications)};

		if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.12.3")) {

			homeDefaultScreen_ = homeDefaultScreenNew;
		}

		homeScreenList = new ArrayList<>(Arrays.asList(homeDefaultScreen_));
		String[] homeScreenArray = new String[homeScreenList.size()];
		homeScreenList.toArray(homeScreenArray);

		if(homeScreenSelectedChoice == 0) {

			homeScreenSelectedChoice = tinyDB.getInt("homeScreenId");
			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.pageTitleMyRepos));
		}

		if(homeScreenSelectedChoice == 1) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.pageTitleStarredRepos));
		}
		else if(homeScreenSelectedChoice == 2) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.pageTitleOrganizations));
		}
		else if(homeScreenSelectedChoice == 3) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.pageTitleRepositories));
		}
		else if(homeScreenSelectedChoice == 4) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.pageTitleProfile));
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
		String[] defaultScreen_ = {getResources().getString(R.string.generalDeepLinkSelectedText), getResources().getString(R.string.navRepos), getResources().getString(R.string.navOrg), getResources().getString(R.string.pageTitleNotifications), getResources().getString(R.string.navExplore)};
		defaultScreen = new ArrayList<>(Arrays.asList(defaultScreen_));

		String[] linksArray = new String[defaultScreen.size()];
		defaultScreen.toArray(linksArray);

		if(defaultLinkHandlerScreenSelectedChoice == 0) {

			defaultLinkHandlerScreenSelectedChoice = tinyDB.getInt("defaultScreenId");
			viewBinding.generalDeepLinkSelected.setText(getResources().getString(R.string.generalDeepLinkSelectedText));
		}

		if(defaultLinkHandlerScreenSelectedChoice == 1) {

			viewBinding.generalDeepLinkSelected.setText(getResources().getString(R.string.navRepos));
		}
		else if(defaultLinkHandlerScreenSelectedChoice == 2) {

			viewBinding.generalDeepLinkSelected.setText(getResources().getString(R.string.navOrg));
		}
		else if(defaultLinkHandlerScreenSelectedChoice == 3) {

			viewBinding.generalDeepLinkSelected.setText(getResources().getString(R.string.pageTitleNotifications));
		}
		else if(defaultLinkHandlerScreenSelectedChoice == 4) {

			viewBinding.generalDeepLinkSelected.setText(getResources().getString(R.string.navExplore));
		}

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
	}

	private void initCloseListener() { onClickListener = view -> finish(); }
}

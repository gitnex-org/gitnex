package org.mian.gitnex.activities;

import android.os.Bundle;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsGeneralBinding;
import org.mian.gitnex.helpers.SnackBar;

/**
 * @author M M Arif
 */
public class SettingsGeneralActivity extends BaseActivity {

	private static int homeScreenSelectedChoice = 0;
	private static int defaultLinkHandlerScreenSelectedChoice = 0;
	private ActivitySettingsGeneralBinding viewBinding;
	private List<String> homeScreenList;
	private List<String> linkHandlerDefaultScreen;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivitySettingsGeneralBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		viewBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		// home screen
		String[] appHomeDefaultScreen = getResources().getStringArray(R.array.appDefaultHomeScreen);

		String[] appHomeDefaultScreenNew =
				getResources().getStringArray(R.array.appDefaultHomeScreenNew);

		if (getAccount().requiresVersion("1.12.3")) {

			appHomeDefaultScreen = appHomeDefaultScreenNew;
		}

		homeScreenList = new ArrayList<>(Arrays.asList(appHomeDefaultScreen));

		if (!getAccount().requiresVersion("1.14.0")) {
			homeScreenList.remove(8);
		}

		String[] homeScreenArray = new String[homeScreenList.size()];
		homeScreenList.toArray(homeScreenArray);

		if (homeScreenSelectedChoice == 0) {

			homeScreenSelectedChoice = tinyDB.getInt("homeScreenId", 0);
			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navMyRepos));
		}

		if (homeScreenSelectedChoice == 1) {

			viewBinding.homeScreenSelected.setText(
					getResources().getString(R.string.pageTitleStarredRepos));
		} else if (homeScreenSelectedChoice == 2) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navOrg));
		} else if (homeScreenSelectedChoice == 3) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navRepos));
		} else if (homeScreenSelectedChoice == 4) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navAccount));
		} else if (homeScreenSelectedChoice == 5) {

			viewBinding.homeScreenSelected.setText(
					getResources().getString(R.string.pageTitleExplore));
		} else if (homeScreenSelectedChoice == 6) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.titleDrafts));
		} else if (homeScreenSelectedChoice == 7) {

			viewBinding.homeScreenSelected.setText(
					getResources().getString(R.string.pageTitleNotifications));
		} else if (homeScreenSelectedChoice == 8) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navMyIssues));
		} else if (homeScreenSelectedChoice == 9) {

			viewBinding.homeScreenSelected.setText(
					getResources().getString(R.string.navMostVisited));
		} else if (homeScreenSelectedChoice == 10) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.navNotes));
		} else if (homeScreenSelectedChoice == 11) {

			viewBinding.homeScreenSelected.setText(getResources().getString(R.string.dashboard));
		} else if (homeScreenSelectedChoice == 12) {

			viewBinding.homeScreenSelected.setText(
					getResources().getString(R.string.navWatchedRepositories));
		}

		viewBinding.homeScreenFrame.setOnClickListener(
				setDefaultHomeScreen -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.settingsHomeScreenSelectorDialogTitle)
									.setCancelable(homeScreenSelectedChoice != -1)
									.setSingleChoiceItems(
											homeScreenArray,
											homeScreenSelectedChoice,
											(dialogInterfaceHomeScreen, i) -> {
												homeScreenSelectedChoice = i;
												viewBinding.homeScreenSelected.setText(
														homeScreenArray[i]);
												tinyDB.putInt("homeScreenId", i);

												dialogInterfaceHomeScreen.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});
		// home screen

		// link handler
		String[] linkHandlerDefaultScreenList =
				getResources().getStringArray(R.array.linkHandlerDefaultScreen);
		linkHandlerDefaultScreen = new ArrayList<>(Arrays.asList(linkHandlerDefaultScreenList));

		String[] linksArray = new String[linkHandlerDefaultScreen.size()];
		linkHandlerDefaultScreen.toArray(linksArray);

		defaultLinkHandlerScreenSelectedChoice = tinyDB.getInt("defaultScreenId");
		viewBinding.generalDeepLinkSelected.setText(
				linksArray[defaultLinkHandlerScreenSelectedChoice]);

		viewBinding.setDefaultLinkHandler.setOnClickListener(
				setDefaultLinkHandler -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.linkSelectorDialogTitle)
									.setCancelable(defaultLinkHandlerScreenSelectedChoice != -1)
									.setSingleChoiceItems(
											linksArray,
											defaultLinkHandlerScreenSelectedChoice,
											(dialogInterfaceHomeScreen, i) -> {
												defaultLinkHandlerScreenSelectedChoice = i;
												viewBinding.generalDeepLinkSelected.setText(
														linksArray[i]);
												tinyDB.putInt("defaultScreenId", i);

												dialogInterfaceHomeScreen.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});
		// link handler

		// custom tabs
		viewBinding.switchTabs.setChecked(tinyDB.getBoolean("useCustomTabs"));
		viewBinding.switchTabs.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					tinyDB.putBoolean("useCustomTabs", isChecked);
					SnackBar.success(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});
		viewBinding.customTabsFrame.setOnClickListener(
				v -> viewBinding.switchTabs.setChecked(!viewBinding.switchTabs.isChecked()));
		// custom tabs

		// enable drafts deletion
		viewBinding.commentsDeletionSwitch.setChecked(
				tinyDB.getBoolean("draftsCommentsDeletionEnabled", true));

		// delete comments on submit switcher
		viewBinding.commentsDeletionSwitch.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					tinyDB.putBoolean("draftsCommentsDeletionEnabled", isChecked);
					SnackBar.success(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});
		viewBinding.enableDraftsCommentsDeletion.setOnClickListener(
				v ->
						viewBinding.commentsDeletionSwitch.setChecked(
								!viewBinding.commentsDeletionSwitch.isChecked()));
		// enable drafts deletion

		// crash reports switcher
		viewBinding.crashReportsSwitch.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					tinyDB.putBoolean("crashReportingEnabled", isChecked);
					SnackBar.success(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});
		viewBinding.enableSendReports.setOnClickListener(
				v ->
						viewBinding.crashReportsSwitch.setChecked(
								!viewBinding.crashReportsSwitch.isChecked()));
		// crash reports switcher
	}
}

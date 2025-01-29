package org.mian.gitnex.activities;

import android.os.Bundle;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsGeneralBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.SnackBar;

/**
 * @author M M Arif
 */
public class SettingsGeneralActivity extends BaseActivity {

	private static int homeScreenSelectedChoice;
	private static int defaultLinkHandlerScreenSelectedChoice;
	private ActivitySettingsGeneralBinding viewBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivitySettingsGeneralBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		viewBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		// home screen
		String[] appHomeDefaultScreen =
				getResources().getStringArray(R.array.appDefaultHomeScreenNew);

		homeScreenSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_HOME_SCREEN_KEY));

		viewBinding.homeScreenSelected.setText(appHomeDefaultScreen[homeScreenSelectedChoice]);

		viewBinding.homeScreenFrame.setOnClickListener(
				setDefaultHomeScreen -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.settingsHomeScreenSelectorDialogTitle)
									.setCancelable(homeScreenSelectedChoice != -1)
									.setSingleChoiceItems(
											appHomeDefaultScreen,
											homeScreenSelectedChoice,
											(dialogInterfaceHomeScreen, i) -> {
												homeScreenSelectedChoice = i;
												viewBinding.homeScreenSelected.setText(
														appHomeDefaultScreen[i]);

												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings.APP_HOME_SCREEN_KEY);

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
		List<String> linkHandlerDefaultScreen =
				new ArrayList<>(Arrays.asList(linkHandlerDefaultScreenList));

		String[] linksArray = new String[linkHandlerDefaultScreen.size()];
		linkHandlerDefaultScreen.toArray(linksArray);

		defaultLinkHandlerScreenSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_LINK_HANDLER_KEY));
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

												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings.APP_LINK_HANDLER_KEY);

												dialogInterfaceHomeScreen.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});
		// link handler

		// custom tabs switcher
		viewBinding.switchTabs.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_CUSTOM_BROWSER_KEY)));
		viewBinding.switchTabs.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							ctx,
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_CUSTOM_BROWSER_KEY);
					SnackBar.success(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});
		viewBinding.customTabsFrame.setOnClickListener(
				v -> viewBinding.switchTabs.setChecked(!viewBinding.switchTabs.isChecked()));
		// custom tabs switcher

		// crash reports switcher
		viewBinding.crashReportsSwitch.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_CRASH_REPORTS_KEY)));
		viewBinding.crashReportsSwitch.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							ctx,
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_CRASH_REPORTS_KEY);
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

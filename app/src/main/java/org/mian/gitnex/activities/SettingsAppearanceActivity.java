package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.util.TinyDB;

/**
 * Author M M Arif
 */

public class SettingsAppearanceActivity extends BaseActivity {

	private Context ctx;
	private View.OnClickListener onClickListener;

	private static String[] timeList = {"Pretty", "Normal"};
	private static int timeSelectedChoice = 0;

	private static String[] codeBlockList = {"Green - Black", "White - Black", "Grey - Black", "White - Grey", "Dark - White"};
	private static int codeBlockSelectedChoice = 0;

	private static String[] homeScreenList = {"My Repositories", "Starred Repositories", "Organizations", "Repositories", "Profile"};
	private static int homeScreenSelectedChoice = 0;

	private static String[] customFontList = {"Roboto", "Manrope", "Source Code Pro"};
	private static int customFontSelectedChoice = 0;

	private static String[] themeList = {"Dark", "Light", "Auto (Day/Night)"};
	private static int themeSelectedChoice = 0;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_appearance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.ctx = getApplicationContext();
		final TinyDB tinyDb = new TinyDB(ctx);

		ImageView closeActivity = findViewById(R.id.close);

		final TextView tvDateTimeSelected = findViewById(R.id.tvDateTimeSelected); // setter for time
		final TextView codeBlockSelected = findViewById(R.id.codeBlockSelected); // setter for code block
		final TextView homeScreenSelected = findViewById(R.id.homeScreenSelected); // setter for home screen
		final TextView customFontSelected = findViewById(R.id.customFontSelected); // setter for custom font
		final TextView themeSelected = findViewById(R.id.themeSelected); // setter for theme

		LinearLayout timeFrame = findViewById(R.id.timeFrame);
		LinearLayout codeBlockFrame = findViewById(R.id.codeBlockFrame);
		LinearLayout homeScreenFrame = findViewById(R.id.homeScreenFrame);
		LinearLayout customFontFrame = findViewById(R.id.customFontFrame);
		LinearLayout themeFrame = findViewById(R.id.themeSelectionFrame);

		Switch counterBadgesSwitch = findViewById(R.id.switchCounterBadge);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		if(!tinyDb.getString("timeStr").isEmpty()) {
			tvDateTimeSelected.setText(tinyDb.getString("timeStr"));
		}

		if(!tinyDb.getString("codeBlockStr").isEmpty()) {
			codeBlockSelected.setText(tinyDb.getString("codeBlockStr"));
		}

		if(!tinyDb.getString("homeScreenStr").isEmpty()) {
			homeScreenSelected.setText(tinyDb.getString("homeScreenStr"));
		}

		if(!tinyDb.getString("customFontStr").isEmpty()) {
			customFontSelected.setText(tinyDb.getString("customFontStr"));
		}

		if(!tinyDb.getString("themeStr").isEmpty()) {
			themeSelected.setText(tinyDb.getString("themeStr"));
		}

		if(timeSelectedChoice == 0) {
			timeSelectedChoice = tinyDb.getInt("timeId");
		}

		if(codeBlockSelectedChoice == 0) {
			codeBlockSelectedChoice = tinyDb.getInt("codeBlockId");
		}

		if(homeScreenSelectedChoice == 0) {
			homeScreenSelectedChoice = tinyDb.getInt("homeScreenId");
		}

		if(customFontSelectedChoice == 0) {
			customFontSelectedChoice = tinyDb.getInt("customFontId", 1);
		}

		if(themeSelectedChoice == 0) {
			themeSelectedChoice = tinyDb.getInt("themeId");
		}

		if(tinyDb.getBoolean("enableCounterBadges")) {
			counterBadgesSwitch.setChecked(true);
		}
		else {
			counterBadgesSwitch.setChecked(false);
		}

		// counter badge switcher
		counterBadgesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if (isChecked) {
				tinyDb.putBoolean("enableCounterBadges", true);
				Toasty.info(ctx, getResources().getString(R.string.settingsSave));
			}
			else {
				tinyDb.putBoolean("enableCounterBadges", false);
				Toasty.info(ctx, getResources().getString(R.string.settingsSave));
			}

		});

		// theme selection dialog
		themeFrame.setOnClickListener(view -> {

			AlertDialog.Builder tsBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			tsBuilder.setTitle(getResources().getString(R.string.themeSelectorDialogTitle));
			if(themeSelectedChoice != -1) {
				tsBuilder.setCancelable(true);
			}
			else {
				tsBuilder.setCancelable(false);
			}

			tsBuilder.setSingleChoiceItems(themeList, themeSelectedChoice, (dialogInterfaceTheme, i) -> {

				themeSelectedChoice = i;
				themeSelected.setText(themeList[i]);
				tinyDb.putString("themeStr", themeList[i]);
				tinyDb.putInt("themeId", i);

				tinyDb.putBoolean("refreshParent", true);
				this.recreate();
				this.overridePendingTransition(0, 0);
				dialogInterfaceTheme.dismiss();
				Toasty.info(ctx, getResources().getString(R.string.settingsSave));

			});

			AlertDialog cfDialog = tsBuilder.create();
			cfDialog.show();

		});

		// custom font dialog
		customFontFrame.setOnClickListener(view -> {

			AlertDialog.Builder cfBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			cfBuilder.setTitle(R.string.settingsCustomFontSelectorDialogTitle);
			if(customFontSelectedChoice != -1) {
				cfBuilder.setCancelable(true);
			}
			else {
				cfBuilder.setCancelable(false);
			}

			cfBuilder.setSingleChoiceItems(customFontList, customFontSelectedChoice, (dialogInterfaceCustomFont, i) -> {

				customFontSelectedChoice = i;
				customFontSelected.setText(customFontList[i]);
				tinyDb.putString("customFontStr", customFontList[i]);
				tinyDb.putInt("customFontId", i);

				tinyDb.putBoolean("refreshParent", true);
				this.recreate();
				this.overridePendingTransition(0, 0);
				dialogInterfaceCustomFont.dismiss();
				Toasty.info(ctx, ctx.getResources().getString(R.string.settingsSave));

			});

			AlertDialog cfDialog = cfBuilder.create();
			cfDialog.show();

		});

		// home screen dialog
		homeScreenFrame.setOnClickListener(view -> {

			AlertDialog.Builder hsBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			hsBuilder.setTitle(R.string.settingshomeScreenSelectorDialogTitle);
			if(homeScreenSelectedChoice != -1) {
				hsBuilder.setCancelable(true);
			}
			else {
				hsBuilder.setCancelable(false);
			}

			hsBuilder.setSingleChoiceItems(homeScreenList, homeScreenSelectedChoice, (dialogInterfaceHomeScreen, i) -> {

				homeScreenSelectedChoice = i;
				homeScreenSelected.setText(homeScreenList[i]);
				tinyDb.putString("homeScreenStr", homeScreenList[i]);
				tinyDb.putInt("homeScreenId", i);

				dialogInterfaceHomeScreen.dismiss();
				Toasty.info(ctx, getResources().getString(R.string.settingsSave));

			});

			AlertDialog hsDialog = hsBuilder.create();
			hsDialog.show();

		});

		// code block dialog
		codeBlockFrame.setOnClickListener(view -> {

			AlertDialog.Builder cBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			cBuilder.setTitle(R.string.settingsCodeBlockSelectorDialogTitle);
			if(codeBlockSelectedChoice != -1) {
				cBuilder.setCancelable(true);
			}
			else {
				cBuilder.setCancelable(false);
			}

			cBuilder.setSingleChoiceItems(codeBlockList, codeBlockSelectedChoice, (dialogInterfaceCodeBlock, i) -> {

				codeBlockSelectedChoice = i;
				codeBlockSelected.setText(codeBlockList[i]);
				tinyDb.putString("codeBlockStr", codeBlockList[i]);
				tinyDb.putInt("codeBlockId", i);

				switch(codeBlockList[i]) {
					case "White - Black":
						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.white));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.black));
						break;
					case "Grey - Black":
						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.colorAccent));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.black));
						break;
					case "White - Grey":
						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.white));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.colorAccent));
						break;
					case "Dark - White":
						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.colorPrimary));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.white));
						break;
					default:
						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.colorLightGreen));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.black));
						break;
				}

				dialogInterfaceCodeBlock.dismiss();
				Toasty.info(ctx, getResources().getString(R.string.settingsSave));

			});

			AlertDialog cDialog = cBuilder.create();
			cDialog.show();

		});

		// time and date dialog
		timeFrame.setOnClickListener(view -> {

			AlertDialog.Builder tBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			tBuilder.setTitle(R.string.settingsTimeSelectorDialogTitle);
			if(timeSelectedChoice != -1) {
				tBuilder.setCancelable(true);
			}
			else {
				tBuilder.setCancelable(false);
			}

			tBuilder.setSingleChoiceItems(timeList, timeSelectedChoice, (dialogInterfaceTime, i) -> {

				timeSelectedChoice = i;
				tvDateTimeSelected.setText(timeList[i]);
				tinyDb.putString("timeStr", timeList[i]);
				tinyDb.putInt("timeId", i);

				if("Normal".equals(timeList[i])) {
					tinyDb.putString("dateFormat", "normal");
				}
				else {
					tinyDb.putString("dateFormat", "pretty");
				}

				dialogInterfaceTime.dismiss();
				Toasty.info(ctx, getResources().getString(R.string.settingsSave));

			});

			AlertDialog tDialog = tBuilder.create();
			tDialog.show();


		});

	}

	private void initCloseListener() {
		onClickListener = view -> {
			finish();
		};
	}

}

package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author M M Arif
 */

public class SettingsAppearanceActivity extends BaseActivity {

	private Context appCtx;
	private View.OnClickListener onClickListener;

	private static final String[] timeList = {"Pretty", "Normal"};
	private static int timeSelectedChoice = 0;

	private List<String> codeBlockList;
	private static int codeBlockSelectedChoice = 0;

	private static final String[] customFontList = {"Roboto", "Manrope", "Source Code Pro"};
	private static int customFontSelectedChoice = 0;

	private static final String[] themeList = {"Dark", "Light", "Auto (Light / Dark)", "Retro", "Auto (Retro / Dark)"};
	private static int themeSelectedChoice = 0;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_appearance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		final TinyDB tinyDb = new TinyDB(appCtx);

		ImageView closeActivity = findViewById(R.id.close);

		final TextView tvDateTimeSelected = findViewById(R.id.tvDateTimeSelected); // setter for time
		final TextView codeBlockSelected = findViewById(R.id.codeBlockSelected); // setter for code block
		final TextView customFontSelected = findViewById(R.id.customFontSelected); // setter for custom font
		final TextView themeSelected = findViewById(R.id.themeSelected); // setter for theme

		LinearLayout timeFrame = findViewById(R.id.timeFrame);
		LinearLayout codeBlockFrame = findViewById(R.id.codeBlockFrame);
		LinearLayout customFontFrame = findViewById(R.id.customFontFrame);
		LinearLayout themeFrame = findViewById(R.id.themeSelectionFrame);

		SwitchMaterial counterBadgesSwitch = findViewById(R.id.switchCounterBadge);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		// code block
		String[] codeBlockList_ = {getResources().getString(R.string.codeBlockGreenOnBlack), getResources().getString(R.string.codeBlockWhiteOnBlack),
			getResources().getString(R.string.codeBlockGreyOnBlack), getResources().getString(R.string.codeBlockWhiteOnGrey),
			getResources().getString(R.string.codeBlockDarkOnWhite)};

		codeBlockList = new ArrayList<>(Arrays.asList(codeBlockList_));
		String[] codeBlockArray = new String[codeBlockList.size()];
		codeBlockList.toArray(codeBlockArray);

		if(codeBlockSelectedChoice == 0) {

			codeBlockSelectedChoice = tinyDb.getInt("codeBlockId");
			codeBlockSelected.setText(getResources().getString(R.string.codeBlockGreenOnBlack));
		}

		if(codeBlockSelectedChoice == 1) {

			codeBlockSelected.setText(getResources().getString(R.string.codeBlockWhiteOnBlack));
		}
		else if(codeBlockSelectedChoice == 2) {

			codeBlockSelected.setText(getResources().getString(R.string.codeBlockGreyOnBlack));
		}
		else if(codeBlockSelectedChoice == 3) {

			codeBlockSelected.setText(getResources().getString(R.string.codeBlockWhiteOnGrey));
		}
		else if(codeBlockSelectedChoice == 4) {

			codeBlockSelected.setText(getResources().getString(R.string.codeBlockDarkOnWhite));
		}

		codeBlockFrame.setOnClickListener(codeBlock -> {

			AlertDialog.Builder cBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			cBuilder.setTitle(R.string.settingsCodeBlockSelectorDialogTitle);
			cBuilder.setCancelable(codeBlockSelectedChoice != -1);

			cBuilder.setSingleChoiceItems(codeBlockList_, codeBlockSelectedChoice, (dialogInterfaceCodeBlock, i) -> {

				codeBlockSelectedChoice = i;
				codeBlockSelected.setText(codeBlockList_[i]);
				tinyDb.putInt("codeBlockId", i);

				switch(i) {
					case 1: // white on black

						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.colorWhite));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.black));
						break;
					case 2: // grey on black

						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.colorAccent));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.black));
						break;
					case 3: // white on grey

						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.colorWhite));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.colorAccent));
						break;
					case 4: // dark on white

						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.colorPrimary));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.colorWhite));
						break;
					default: // green on black

						tinyDb.putInt("codeBlockColor", getResources().getColor(R.color.colorLightGreen));
						tinyDb.putInt("codeBlockBackground", getResources().getColor(R.color.black));
						break;
				}

				dialogInterfaceCodeBlock.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog cDialog = cBuilder.create();
			cDialog.show();
		});
		// code block

		if(!tinyDb.getString("timeStr").isEmpty()) {

			tvDateTimeSelected.setText(tinyDb.getString("timeStr"));
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

		if(customFontSelectedChoice == 0) {

			customFontSelectedChoice = tinyDb.getInt("customFontId", 1);
		}

		if(themeSelectedChoice == 0) {

			themeSelectedChoice = tinyDb.getInt("themeId");
		}

		counterBadgesSwitch.setChecked(tinyDb.getBoolean("enableCounterBadges"));

		// counter badge switcher
		counterBadgesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDb.putBoolean("enableCounterBadges", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});

		// theme selection dialog
		themeFrame.setOnClickListener(view -> {

			AlertDialog.Builder tsBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			tsBuilder.setTitle(getResources().getString(R.string.themeSelectorDialogTitle));
			tsBuilder.setCancelable(themeSelectedChoice != -1);

			tsBuilder.setSingleChoiceItems(themeList, themeSelectedChoice, (dialogInterfaceTheme, i) -> {

				themeSelectedChoice = i;
				themeSelected.setText(themeList[i]);
				tinyDb.putString("themeStr", themeList[i]);
				tinyDb.putInt("themeId", i);

				tinyDb.putBoolean("refreshParent", true);
				this.recreate();
				this.overridePendingTransition(0, 0);
				dialogInterfaceTheme.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog cfDialog = tsBuilder.create();
			cfDialog.show();
		});

		// custom font dialog
		customFontFrame.setOnClickListener(view -> {

			AlertDialog.Builder cfBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			cfBuilder.setTitle(R.string.settingsCustomFontSelectorDialogTitle);
			cfBuilder.setCancelable(customFontSelectedChoice != -1);

			cfBuilder.setSingleChoiceItems(customFontList, customFontSelectedChoice, (dialogInterfaceCustomFont, i) -> {

				customFontSelectedChoice = i;
				customFontSelected.setText(customFontList[i]);
				tinyDb.putString("customFontStr", customFontList[i]);
				tinyDb.putInt("customFontId", i);

				tinyDb.putBoolean("refreshParent", true);
				this.recreate();
				this.overridePendingTransition(0, 0);
				dialogInterfaceCustomFont.dismiss();
				Toasty.success(appCtx, appCtx.getResources().getString(R.string.settingsSave));
			});

			AlertDialog cfDialog = cfBuilder.create();
			cfDialog.show();
		});

		// time and date dialog
		timeFrame.setOnClickListener(view -> {

			AlertDialog.Builder tBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			tBuilder.setTitle(R.string.settingsTimeSelectorDialogTitle);
			tBuilder.setCancelable(timeSelectedChoice != -1);

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
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog tDialog = tBuilder.create();
			tDialog.show();
		});

	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}

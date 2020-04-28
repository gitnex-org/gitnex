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

public class SettingsFileViewerActivity extends BaseActivity {

	private Context appCtx;
	private View.OnClickListener onClickListener;

	private static String[] fileveiwerSourceCodeThemesList = {"Sublime", "Arduino Light", "Github", "Far ", "Ir Black", "Android Studio"};
	private static int fileveiwerSourceCodeThemesSelectedChoice = 0;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_fileview;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		final TinyDB tinyDb = new TinyDB(appCtx);

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		final TextView fileveiwerSourceCodeThemesSelected = findViewById(R.id.sourceCodeThemeSelected); // setter for fileviewer theme

		LinearLayout sourceCodeThemeFrame = findViewById(R.id.sourceCodeThemeFrame);

		Switch pdfModeSwitch = findViewById(R.id.switchPdfMode);

		if(!tinyDb.getString("fileviewerSourceCodeThemeStr").isEmpty()) {
			fileveiwerSourceCodeThemesSelected.setText(tinyDb.getString("fileviewerSourceCodeThemeStr"));
		}

		if(fileveiwerSourceCodeThemesSelectedChoice == 0) {
			fileveiwerSourceCodeThemesSelectedChoice = tinyDb.getInt("fileviewerThemeId");
		}

		if(tinyDb.getBoolean("enablePdfMode")) {
			pdfModeSwitch.setChecked(true);
		}
		else {
			pdfModeSwitch.setChecked(false);
		}

		// fileviewer srouce code theme selection dialog
		sourceCodeThemeFrame.setOnClickListener(view -> {

			AlertDialog.Builder fvtsBuilder = new AlertDialog.Builder(SettingsFileViewerActivity.this);

			fvtsBuilder.setTitle(R.string.fileviewerSourceCodeThemeSelectorDialogTitle);
			if(fileveiwerSourceCodeThemesSelectedChoice != -1) {
				fvtsBuilder.setCancelable(true);
			}
			else {
				fvtsBuilder.setCancelable(false);
			}

			fvtsBuilder.setSingleChoiceItems(fileveiwerSourceCodeThemesList, fileveiwerSourceCodeThemesSelectedChoice, (dialogInterfaceTheme, i) -> {

				fileveiwerSourceCodeThemesSelectedChoice = i;
				fileveiwerSourceCodeThemesSelected.setText(fileveiwerSourceCodeThemesList[i]);
				tinyDb.putString("fileviewerSourceCodeThemeStr", fileveiwerSourceCodeThemesList[i]);
				tinyDb.putInt("fileviewerSourceCodeThemeId", i);

				dialogInterfaceTheme.dismiss();
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));

			});

			AlertDialog cfDialog = fvtsBuilder.create();
			cfDialog.show();

		});

		// pdf night mode switcher
		pdfModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {
				tinyDb.putBoolean("enablePdfMode", true);
				tinyDb.putString("enablePdfModeInit", "yes");
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));
			}
			else {
				tinyDb.putBoolean("enablePdfMode", false);
				tinyDb.putString("enablePdfModeInit", "yes");
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));
			}

		});

	}

	private void initCloseListener() {
		onClickListener = view -> {
			finish();
		};
	}

}

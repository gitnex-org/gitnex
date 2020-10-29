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

/**
 * Author M M Arif
 */

public class SettingsFileViewerActivity extends BaseActivity {

	private Context appCtx;
	private View.OnClickListener onClickListener;

	private static final String[] fileViewerSourceCodeThemesList = {"Sublime", "Arduino Light", "Github", "Far ", "Ir Black", "Android Studio"};
	private static int fileViewerSourceCodeThemesSelectedChoice = 0;

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

		final TextView fileViewerSourceCodeThemesSelected = findViewById(R.id.sourceCodeThemeSelected); // setter for fileviewer theme

		LinearLayout sourceCodeThemeFrame = findViewById(R.id.sourceCodeThemeFrame);

		SwitchMaterial pdfModeSwitch = findViewById(R.id.switchPdfMode);

		if(!tinyDb.getString("fileviewerSourceCodeThemeStr").isEmpty()) {
			fileViewerSourceCodeThemesSelected.setText(tinyDb.getString("fileviewerSourceCodeThemeStr"));
		}

		if(fileViewerSourceCodeThemesSelectedChoice == 0) {
			fileViewerSourceCodeThemesSelectedChoice = tinyDb.getInt("fileviewerThemeId");
		}

		pdfModeSwitch.setChecked(tinyDb.getBoolean("enablePdfMode"));

		// fileviewer srouce code theme selection dialog
		sourceCodeThemeFrame.setOnClickListener(view -> {

			AlertDialog.Builder fvtsBuilder = new AlertDialog.Builder(SettingsFileViewerActivity.this);

			fvtsBuilder.setTitle(R.string.fileviewerSourceCodeThemeSelectorDialogTitle);
			fvtsBuilder.setCancelable(fileViewerSourceCodeThemesSelectedChoice != -1);

			fvtsBuilder.setSingleChoiceItems(fileViewerSourceCodeThemesList, fileViewerSourceCodeThemesSelectedChoice, (dialogInterfaceTheme, i) -> {

				fileViewerSourceCodeThemesSelectedChoice = i;
				fileViewerSourceCodeThemesSelected.setText(fileViewerSourceCodeThemesList[i]);
				tinyDb.putString("fileviewerSourceCodeThemeStr", fileViewerSourceCodeThemesList[i]);
				tinyDb.putInt("fileviewerSourceCodeThemeId", i);

				dialogInterfaceTheme.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));

			});

			AlertDialog cfDialog = fvtsBuilder.create();
			cfDialog.show();
		});

		// pdf night mode switcher
		pdfModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDb.putBoolean("enablePdfMode", isChecked);
			tinyDb.putString("enablePdfModeInit", "yes");
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}

package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;

/**
 * Author M M Arif
 */

public class SettingsTranslationActivity extends BaseActivity {

	private Context appCtx;
	private View.OnClickListener onClickListener;

	private static String[] langList = {"English", "Arabic", "Chinese", "Finnish", "French", "German", "Italian", "Latvian", "Persian", "Polish", "Portuguese/Brazilian", "Russian", "Serbian", "Spanish", "Turkish",
			"Ukrainian"};
	private static int langSelectedChoice = 0;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_translation;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		TinyDB tinyDb = new TinyDB(appCtx);

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		final TextView tvLanguageSelected = findViewById(R.id.tvLanguageSelected); // setter for en, fr
		TextView helpTranslate = findViewById(R.id.helpTranslate);

		LinearLayout langFrame = findViewById(R.id.langFrame);

		helpTranslate.setOnClickListener(v12 -> {

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			intent.setData(Uri.parse(getResources().getString(R.string.crowdInLink)));
			startActivity(intent);

		});

		if(!tinyDb.getString("localeStr").isEmpty()) {
			tvLanguageSelected.setText(tinyDb.getString("localeStr"));
		}

		if(langSelectedChoice == 0) {
			langSelectedChoice = tinyDb.getInt("langId");
		}

		// language dialog
		langFrame.setOnClickListener(view -> {

			AlertDialog.Builder lBuilder = new AlertDialog.Builder(SettingsTranslationActivity.this);

			lBuilder.setTitle(R.string.settingsLanguageSelectorDialogTitle);
			if(langSelectedChoice != -1) {
				lBuilder.setCancelable(true);
			}
			else {
				lBuilder.setCancelable(false);
			}

			lBuilder.setSingleChoiceItems(langList, langSelectedChoice, (dialogInterface, i) -> {

				langSelectedChoice = i;
				tvLanguageSelected.setText(langList[i]);
				tinyDb.putString("localeStr", langList[i]);
				tinyDb.putInt("langId", i);

				switch(langList[i]) {
					case "Arabic":
						tinyDb.putString("locale", "ar");
						break;
					case "Chinese":
						tinyDb.putString("locale", "zh");
						break;
					case "Finnish":
						tinyDb.putString("locale", "fi");
						break;
					case "French":
						tinyDb.putString("locale", "fr");
						break;
					case "German":
						tinyDb.putString("locale", "de");
						break;
					case "Italian":
						tinyDb.putString("locale", "it");
						break;
					case "Latvian":
						tinyDb.putString("locale", "lv");
						break;
					case "Persian":
						tinyDb.putString("locale", "fa");
						break;
					case "Polish":
						tinyDb.putString("locale", "pl");
						break;
					case "Portuguese/Brazilian":
						tinyDb.putString("locale", "pt");
						break;
					case "Russian":
						tinyDb.putString("locale", "ru");
						break;
					case "Serbian":
						tinyDb.putString("locale", "sr");
						break;
					case "Spanish":
						tinyDb.putString("locale", "es");
						break;
					case "Turkish":
						tinyDb.putString("locale", "tr");
						break;
					case "Ukrainian":
						tinyDb.putString("locale", "uk");
						break;
					default:
						tinyDb.putString("locale", "en");
						break;
				}

				tinyDb.putBoolean("refreshParent", true);
				this.recreate();
				this.overridePendingTransition(0, 0);
				dialogInterface.dismiss();
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));

			});

			lBuilder.setNegativeButton(getString(R.string.cancelButton), (dialog, which) -> dialog.dismiss());

			AlertDialog lDialog = lBuilder.create();
			lDialog.show();

		});

	}

	private void initCloseListener() {
		onClickListener = view -> {
			finish();
		};
	}

}

package org.mian.gitnex.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Toasty;

/**
 * Author M M Arif
 */

public class SettingsTranslationActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	private static String[] langList = {"English", "Arabic", "Chinese", "Czech", "Finnish", "French", "German", "Italian", "Latvian", "Persian",
		"Polish", "Portuguese/Brazilian", "Russian", "Serbian", "Spanish", "Turkish", "Ukrainian"};
	private static int langSelectedChoice = 0;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_translation;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

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

		if(!tinyDB.getString("localeStr").isEmpty()) {

			tvLanguageSelected.setText(tinyDB.getString("localeStr"));
		}

		if(langSelectedChoice == 0) {

			langSelectedChoice = tinyDB.getInt("langId");
		}

		// language dialog
		langFrame.setOnClickListener(view -> {

			AlertDialog.Builder lBuilder = new AlertDialog.Builder(SettingsTranslationActivity.this);

			lBuilder.setTitle(R.string.settingsLanguageSelectorDialogTitle);
			lBuilder.setCancelable(langSelectedChoice != -1);

			lBuilder.setSingleChoiceItems(langList, langSelectedChoice, (dialogInterface, i) -> {

				langSelectedChoice = i;
				tvLanguageSelected.setText(langList[i]);
				tinyDB.putString("localeStr", langList[i]);
				tinyDB.putInt("langId", i);

				switch(langList[i]) {
					case "Arabic":

						tinyDB.putString("locale", "ar");
						break;
					case "Chinese":

						tinyDB.putString("locale", "zh");
						break;
					case "Czech":

						tinyDB.putString("locale", "cs");
						break;
					case "Finnish":

						tinyDB.putString("locale", "fi");
						break;
					case "French":

						tinyDB.putString("locale", "fr");
						break;
					case "German":

						tinyDB.putString("locale", "de");
						break;
					case "Italian":

						tinyDB.putString("locale", "it");
						break;
					case "Latvian":

						tinyDB.putString("locale", "lv");
						break;
					case "Persian":

						tinyDB.putString("locale", "fa");
						break;
					case "Polish":

						tinyDB.putString("locale", "pl");
						break;
					case "Portuguese/Brazilian":

						tinyDB.putString("locale", "pt");
						break;
					case "Russian":

						tinyDB.putString("locale", "ru");
						break;
					case "Serbian":

						tinyDB.putString("locale", "sr");
						break;
					case "Spanish":

						tinyDB.putString("locale", "es");
						break;
					case "Turkish":

						tinyDB.putString("locale", "tr");
						break;
					case "Ukrainian":

						tinyDB.putString("locale", "uk");
						break;
					default:

						tinyDB.putString("locale", "en");
						break;
				}

				tinyDB.putBoolean("refreshParent", true);
				this.recreate();
				this.overridePendingTransition(0, 0);
				dialogInterface.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			lBuilder.setNeutralButton(getString(R.string.cancelButton), null);

			AlertDialog lDialog = lBuilder.create();
			lDialog.show();
		});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

}

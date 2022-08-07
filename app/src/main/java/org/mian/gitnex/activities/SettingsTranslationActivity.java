package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsTranslationBinding;
import org.mian.gitnex.fragments.SettingsFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * @author M M Arif
 */

public class SettingsTranslationActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	private static int langSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		LinkedHashMap<String, String> langs = new LinkedHashMap<>();
		langs.put("", getString(R.string.settingsLanguageSystem));
		for(String langCode : getResources().getStringArray(R.array.languages)) {
			langs.put(langCode, getLanguageDisplayName(langCode));
		}

		ActivitySettingsTranslationBinding activitySettingsTranslationBinding = ActivitySettingsTranslationBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsTranslationBinding.getRoot());

		ImageView closeActivity = activitySettingsTranslationBinding.close;

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		final TextView tvLanguageSelected = activitySettingsTranslationBinding.tvLanguageSelected; // setter for en, fr
		TextView helpTranslate = activitySettingsTranslationBinding.helpTranslate;

		LinearLayout langFrame = activitySettingsTranslationBinding.langFrame;

		helpTranslate.setOnClickListener(v12 -> {
			AppUtil.openUrlInBrowser(this, getResources().getString(R.string.crowdInLink));
		});

		langSelectedChoice = tinyDB.getInt("langId");
		tvLanguageSelected.setText(langs.get(langs.keySet().toArray(new String[0])[langSelectedChoice]));

		// language dialog
		langFrame.setOnClickListener(view -> {

			MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(ctx)
				.setTitle(R.string.settingsLanguageSelectorDialogTitle)
				.setCancelable(langSelectedChoice != -1)
				.setNeutralButton(getString(R.string.cancelButton), null)
				.setSingleChoiceItems(langs.values().toArray(new String[0]), langSelectedChoice, (dialogInterface, i) -> {

					String selectedLanguage = langs.keySet().toArray(new String[0])[i];
					tinyDB.putInt("langId", i);
					tinyDB.putString("locale", selectedLanguage);

					SettingsFragment.refreshParent = true;
					this.overridePendingTransition(0, 0);
					dialogInterface.dismiss();
					Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
					this.recreate();
				});

			materialAlertDialogBuilder.create().show();
		});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private static String getLanguageDisplayName(String langCode) {
		Locale english = new Locale("en");
		Locale translated = new Locale(langCode);
		return String.format("%s (%s)", translated.getDisplayName(translated), translated.getDisplayName(english));
	}

}

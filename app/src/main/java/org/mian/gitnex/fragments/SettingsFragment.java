package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class SettingsFragment extends Fragment {

	private Context ctx = null;

	private static String[] langList = {"Arabic", "Chinese", "English", "Finnish", "French", "German", "Italian", "Latvian", "Persian", "Polish", "Portuguese/Brazilian", "Russian", "Serbian", "Spanish", "Turkish", "Ukrainian"};
	private static int langSelectedChoice = 0;

	private static String[] timeList = {"Pretty", "Normal"};
	private static int timeSelectedChoice = 0;

	private static String[] codeBlockList = {"Green - Black", "White - Black", "Grey - Black", "White - Grey", "Dark - White"};
	private static int codeBlockSelectedChoice = 0;

	private static String[] homeScreenList = {"My Repositories", "Starred Repositories", "Organizations", "Repositories", "Profile"};
	private static int homeScreenSelectedChoice = 0;

	private static String[] customFontList = {"Roboto", "Manrope", "Source Code Pro"};
	private static int customFontSelectedChoice = 0;

	private static String[] themeList = {"Dark", "Light"};
	private static int themeSelectedChoice = 0;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_settings, container, false);
		final TinyDB tinyDb = new TinyDB(getContext());

		final TextView tvLanguageSelected = v.findViewById(R.id.tvLanguageSelected); // setter for en, fr
		final TextView tvDateTimeSelected = v.findViewById(R.id.tvDateTimeSelected); // setter for time
		final TextView codeBlockSelected = v.findViewById(R.id.codeBlockSelected); // setter for code block
		final TextView homeScreenSelected = v.findViewById(R.id.homeScreenSelected); // setter for home screen
		final TextView customFontSelected = v.findViewById(R.id.customFontSelected); // setter for custom font
		final TextView themeSelected = v.findViewById(R.id.themeSelected); // setter for theme

		LinearLayout langFrame = v.findViewById(R.id.langFrame);
		LinearLayout timeFrame = v.findViewById(R.id.timeFrame);
		LinearLayout codeBlockFrame = v.findViewById(R.id.codeBlockFrame);
		LinearLayout homeScreenFrame = v.findViewById(R.id.homeScreenFrame);
		LinearLayout customFontFrame = v.findViewById(R.id.customFontFrame);
		LinearLayout themeFrame = v.findViewById(R.id.themeSelectionFrame);
		LinearLayout certsFrame = v.findViewById(R.id.certsFrame);

		Switch counterBadgesSwitch = v.findViewById(R.id.switchCounterBadge);
		Switch pdfModeSwitch = v.findViewById(R.id.switchPdfMode);
		TextView helpTranslate = v.findViewById(R.id.helpTranslate);

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

		if(langSelectedChoice == 0) {
			langSelectedChoice = tinyDb.getInt("langId");
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
			customFontSelectedChoice = tinyDb.getInt("customFontId");
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

		if(tinyDb.getBoolean("enablePdfMode")) {
			pdfModeSwitch.setChecked(true);
		}
		else {
			pdfModeSwitch.setChecked(false);
		}

		// certs deletion
		certsFrame.setOnClickListener(v1 -> {

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle(getResources().getString(R.string.settingsCertsPopupTitle));
			builder.setMessage(getResources().getString(R.string.settingsCertsPopupMessage));
			builder.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

				ctx.getSharedPreferences(MemorizingTrustManager.KEYSTORE_NAME, Context.MODE_PRIVATE).edit().remove(MemorizingTrustManager.KEYSTORE_KEY).apply();

				MainActivity.logout(Objects.requireNonNull(getActivity()), ctx);

			});

			builder.setNeutralButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.create().show();

		});

		// counter badge switcher
		counterBadgesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                tinyDb.putBoolean("enableCounterBadges", true);
                Toasty.info(getContext(), getResources().getString(R.string.settingsSave));
            }
            else {
                tinyDb.putBoolean("enableCounterBadges", false);
                Toasty.info(getContext(), getResources().getString(R.string.settingsSave));
            }

		});

		// pdf night mode switcher
		pdfModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {
				tinyDb.putBoolean("enablePdfMode", true);
				tinyDb.putString("enablePdfModeInit", "yes");
				Toasty.info(getContext(), getResources().getString(R.string.settingsSave));
			}
			else {
				tinyDb.putBoolean("enablePdfMode", false);
				tinyDb.putString("enablePdfModeInit", "yes");
				Toasty.info(getContext(), getResources().getString(R.string.settingsSave));
			}

		});

		// theme selection dialog
		themeFrame.setOnClickListener(view -> {

			AlertDialog.Builder tsBuilder = new AlertDialog.Builder(ctx);

			tsBuilder.setTitle(R.string.themeSelectorDialogTitle);
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

				Objects.requireNonNull(getActivity()).recreate();
				getActivity().overridePendingTransition(0, 0);
				dialogInterfaceTheme.dismiss();
				Toasty.info(getContext(), getResources().getString(R.string.settingsSave));

			});

			AlertDialog cfDialog = tsBuilder.create();
			cfDialog.show();

		});

		// custom font dialog
		customFontFrame.setOnClickListener(view -> {

			AlertDialog.Builder cfBuilder = new AlertDialog.Builder(ctx);

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

				Objects.requireNonNull(getActivity()).recreate();
				getActivity().overridePendingTransition(0, 0);
				dialogInterfaceCustomFont.dismiss();
				Toasty.info(getContext(), getResources().getString(R.string.settingsSave));

			});

			AlertDialog cfDialog = cfBuilder.create();
			cfDialog.show();

		});

		// home screen dialog
		homeScreenFrame.setOnClickListener(view -> {

			AlertDialog.Builder hsBuilder = new AlertDialog.Builder(ctx);

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
				Toasty.info(getContext(), getResources().getString(R.string.settingsSave));

			});

			AlertDialog hsDialog = hsBuilder.create();
			hsDialog.show();

		});

		// code block dialog
		codeBlockFrame.setOnClickListener(view -> {

			AlertDialog.Builder cBuilder = new AlertDialog.Builder(ctx);

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
				Toasty.info(getContext(), getResources().getString(R.string.settingsSave));

			});

			AlertDialog cDialog = cBuilder.create();
			cDialog.show();

		});

		// language dialog
		langFrame.setOnClickListener(view -> {

			AlertDialog.Builder lBuilder = new AlertDialog.Builder(ctx);

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

				dialogInterface.dismiss();
				Toasty.info(getContext(), getResources().getString(R.string.settingsSave));
				Objects.requireNonNull(getActivity()).recreate();
				getActivity().overridePendingTransition(0, 0);

			});

			lBuilder.setNegativeButton(getString(R.string.cancelButton), (dialog, which) -> dialog.dismiss());

			AlertDialog lDialog = lBuilder.create();
			lDialog.show();

		});

		// time n date dialog
		timeFrame.setOnClickListener(view -> {

			AlertDialog.Builder tBuilder = new AlertDialog.Builder(ctx);

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
				Toasty.info(getContext(), getResources().getString(R.string.settingsSave));

			});

			AlertDialog tDialog = tBuilder.create();
			tDialog.show();

		});

		return v;

	}

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);
		ctx = context;

	}

}

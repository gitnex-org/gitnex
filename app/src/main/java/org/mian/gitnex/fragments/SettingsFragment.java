package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

/**
 * Author M M Arif
 */

public class SettingsFragment extends Fragment {

    private Context ctx = null;

    private static String[] langList = {"Arabic", "Chinese", "English", "Finnish", "French", "German", "Italian", "Persian", "Russian", "Serbian", "Turkish"};
    private static int langSelectedChoice = 0;

    private static String[] timeList = {"Pretty", "Normal"};
    private static int timeSelectedChoice = 0;

    private static String[] codeBlockList = {"Green - Black", "White - Black", "Grey - Black", "White - Grey", "Dark - White"};
    private static int codeBlockSelectedChoice = 0;

    private static String[] homeScreenList = {"My Repositories", "Starred Repositories", "Organizations", "Repositories", "Profile"};
    private static int homeScreenSelectedChoice = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ((MainActivity) Objects.requireNonNull(getActivity())).setActionBarTitle(getResources().getString(R.string.pageTitleSettings));
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        final TinyDB tinyDb = new TinyDB(getContext());

        final TextView tvLanguageSelected = v.findViewById(R.id.tvLanguageSelected); // setter for en, fr
        final TextView tvDateTimeSelected = v.findViewById(R.id.tvDateTimeSelected); // setter for time
        final TextView codeBlockSelected = v.findViewById(R.id.codeBlockSelected); // setter for code block
        final TextView homeScreenSelected = v.findViewById(R.id.homeScreenSelected); // setter for home screen

        LinearLayout langFrame = v.findViewById(R.id.langFrame);
        LinearLayout timeFrame = v.findViewById(R.id.timeFrame);
        LinearLayout codeBlockFrame = v.findViewById(R.id.codeBlockFrame);
        LinearLayout homeScreenFrame = v.findViewById(R.id.homeScreenFrame);

        Switch issuesSwitch =  v.findViewById(R.id.switchIssuesBadge);
        TextView helpTranslate = v.findViewById(R.id.helpTranslate);

        helpTranslate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.crowdInLink)));
                startActivity(intent);
            }
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

        if(tinyDb.getBoolean("enableCounterIssueBadge")) {
            issuesSwitch.setChecked(true);
        }

        // issues badge switcher
        issuesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tinyDb.putBoolean("enableCounterIssueBadge", true);
                    tinyDb.putString("enableCounterIssueBadgeInit", "yes");
                    Toasty.info(getContext(), getResources().getString(R.string.settingsSave));
                } else {
                    tinyDb.putBoolean("enableCounterIssueBadge", false);
                    tinyDb.putString("enableCounterIssueBadgeInit", "yes");
                    Toasty.info(getContext(), getResources().getString(R.string.settingsSave));
                }
            }
        });

        // home screen dialog
        homeScreenFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder hsBuilder = new AlertDialog.Builder(ctx, R.style.confirmDialog);

                hsBuilder.setTitle(R.string.settingshomeScreenSelectorDialogTitle);
                if(homeScreenSelectedChoice != -1) {
                    hsBuilder.setCancelable(true);
                }
                else {
                    hsBuilder.setCancelable(false);
                }

                hsBuilder.setSingleChoiceItems(homeScreenList, homeScreenSelectedChoice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterfaceHomeScreen, int i) {

                        homeScreenSelectedChoice = i;
                        homeScreenSelected.setText(homeScreenList[i]);
                        tinyDb.putString("homeScreenStr", homeScreenList[i]);
                        tinyDb.putInt("homeScreenId", i);

                        dialogInterfaceHomeScreen.dismiss();
                        Toasty.info(getContext(), getResources().getString(R.string.settingsSave));

                    }
                });

                AlertDialog hsDialog = hsBuilder.create();
                hsDialog.show();

            }
        });

        // code block dialog
        codeBlockFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder cBuilder = new AlertDialog.Builder(ctx, R.style.confirmDialog);

                cBuilder.setTitle(R.string.settingsCodeBlockSelectorDialogTitle);
                if(codeBlockSelectedChoice != -1) {
                    cBuilder.setCancelable(true);
                }
                else {
                    cBuilder.setCancelable(false);
                }

                cBuilder.setSingleChoiceItems(codeBlockList, codeBlockSelectedChoice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterfaceCodeBlock, int i) {

                        codeBlockSelectedChoice = i;
                        codeBlockSelected.setText(codeBlockList[i]);
                        tinyDb.putString("codeBlockStr", codeBlockList[i]);
                        tinyDb.putInt("codeBlockId", i);

                        switch (codeBlockList[i]) {
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

                    }
                });

                AlertDialog cDialog = cBuilder.create();
                cDialog.show();

            }
        });

        // language dialog
        langFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder lBuilder = new AlertDialog.Builder(ctx, R.style.confirmDialog);

                lBuilder.setTitle(R.string.settingsLanguageSelectorDialogTitle);
                if(langSelectedChoice != -1) {
                    lBuilder.setCancelable(true);
                }
                else {
                    lBuilder.setCancelable(false);
                }

                lBuilder.setSingleChoiceItems(langList, langSelectedChoice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        langSelectedChoice = i;
                        tvLanguageSelected.setText(langList[i]);
                        tinyDb.putString("localeStr", langList[i]);
                        tinyDb.putInt("langId", i);

                        switch (langList[i]) {
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
                            case "Persian":
                                tinyDb.putString("locale", "fa");
                                break;
                            case "Russian":
                                tinyDb.putString("locale", "ru");
                                break;
                            case "Serbian":
                                tinyDb.putString("locale", "sr");
                                break;
                            case "Turkish":
                                tinyDb.putString("locale", "tr");
                                break;
                            default:
                                tinyDb.putString("locale", "en");
                                break;
                        }

                        dialogInterface.dismiss();
                        Toasty.info(getContext(), getResources().getString(R.string.settingsSave));
                        Objects.requireNonNull(getActivity()).recreate();
                        getActivity().overridePendingTransition(0, 0);

                    }
                });
                lBuilder.setNegativeButton(getString(R.string.cancelButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog lDialog = lBuilder.create();
                lDialog.show();

            }
        });

        // time n date dialog
        timeFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder tBuilder = new AlertDialog.Builder(ctx, R.style.confirmDialog);

                tBuilder.setTitle(R.string.settingsTimeSelectorDialogTitle);
                if(timeSelectedChoice != -1) {
                    tBuilder.setCancelable(true);
                }
                else {
                    tBuilder.setCancelable(false);
                }

                tBuilder.setSingleChoiceItems(timeList, timeSelectedChoice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterfaceTime, int i) {

                        timeSelectedChoice = i;
                        tvDateTimeSelected.setText(timeList[i]);
                        tinyDb.putString("timeStr", timeList[i]);
                        tinyDb.putInt("timeId", i);

                        if ("Normal".equals(timeList[i])) {
                            tinyDb.putString("dateFormat", "normal");
                        } else {
                            tinyDb.putString("dateFormat", "pretty");
                        }

                        dialogInterfaceTime.dismiss();
                        Toasty.info(getContext(), getResources().getString(R.string.settingsSave));

                    }
                });

                AlertDialog tDialog = tBuilder.create();
                tDialog.show();

            }
        });

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ctx = context;
    }

}

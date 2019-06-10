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

    private static String[] langList = {"English", "French", "German", "Russian"};
    private static int langSelectedChoice = 0;

    private static String[] timeList = {"Pretty", "Normal"};
    private static int timeSelectedChoice = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ((MainActivity) Objects.requireNonNull(getActivity())).setActionBarTitle(getResources().getString(R.string.pageTitleSettings));
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        final TinyDB tinyDb = new TinyDB(getContext());

        final TextView tvLanguageSelected = v.findViewById(R.id.tvLanguageSelected); // setter for en, fr
        final TextView tvDateTimeSelected = v.findViewById(R.id.tvDateTimeSelected); // setter for time
        LinearLayout langFrame = v.findViewById(R.id.langFrame);
        LinearLayout timeFrame = v.findViewById(R.id.timeFrame);
        Switch issuesSwitch =  v.findViewById(R.id.switchIssuesBadge);
        TextView helpTranslate = v.findViewById(R.id.helpTranslate);

        helpTranslate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getResources().getString(R.string.appRepoContributingLink)));
                startActivity(intent);
            }
        });

        if(!tinyDb.getString("localeStr").isEmpty()) {
            tvLanguageSelected.setText(tinyDb.getString("localeStr"));
        }

        if(!tinyDb.getString("timeStr").isEmpty()) {
            tvDateTimeSelected.setText(tinyDb.getString("timeStr"));
        }

        if(langSelectedChoice == 0) {
            langSelectedChoice = tinyDb.getInt("langId");
        }

        if(timeSelectedChoice == 0) {
            timeSelectedChoice = tinyDb.getInt("timeId");
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
                            case "English":
                                tinyDb.putString("locale", "en");
                                break;
                            case "French":
                                tinyDb.putString("locale", "fr");
                                break;
                            case "German":
                                tinyDb.putString("locale", "de");
                                break;
                            case "Russian":
                                tinyDb.putString("locale", "ru");
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

                        switch (timeList[i]) {
                            case "Pretty":
                                tinyDb.putString("dateFormat", "pretty");
                                break;
                            case "Normal":
                                tinyDb.putString("dateFormat", "normal");
                                break;
                            default:
                                tinyDb.putString("dateFormat", "pretty");
                                break;
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

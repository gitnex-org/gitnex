package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import org.gitnex.tea4j.models.Teams;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateTeamByOrgBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateTeamByOrgActivity extends BaseActivity implements View.OnClickListener {

    private View.OnClickListener onClickListener;
    private TextView teamName;
    private TextView teamDesc;
    private TextView teamPermission;
    private TextView teamPermissionDetail;
    private TextView teamAccessControls;
    private TextView teamAccessControlsArray;
    private Button createTeamButton;
    private final String[] permissionList = {"Read", "Write", "Admin"};
    public int permissionSelectedChoice = -1;

    private final String[] accessControlsList = new String[] {
            "Code",
            "Issues",
            "Pull Request",
            "Releases",
            "Wiki",
            "External Wiki",
            "External Issues"
    };

    private List<String> pushAccessList;

    private final boolean[] selectedAccessControlsTrueFalse = new boolean[]{
            false,
            false,
            false,
            false,
            false,
            false,
            false
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityCreateTeamByOrgBinding activityCreateTeamByOrgBinding = ActivityCreateTeamByOrgBinding.inflate(getLayoutInflater());
	    setContentView(activityCreateTeamByOrgBinding.getRoot());

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ImageView closeActivity = activityCreateTeamByOrgBinding.close;
        teamName = activityCreateTeamByOrgBinding.teamName;
        teamDesc = activityCreateTeamByOrgBinding.teamDesc;
        teamPermission = activityCreateTeamByOrgBinding.teamPermission;
        teamPermissionDetail = activityCreateTeamByOrgBinding.teamPermissionDetail;
        teamAccessControls = activityCreateTeamByOrgBinding.teamAccessControls;
        teamAccessControlsArray = activityCreateTeamByOrgBinding.teamAccessControlsArray;
        createTeamButton = activityCreateTeamByOrgBinding.createTeamButton;

        teamName.requestFocus();
        assert imm != null;
        imm.showSoftInput(teamName, InputMethodManager.SHOW_IMPLICIT);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        teamPermission.setOnClickListener(view -> {

            AlertDialog.Builder pBuilder = new AlertDialog.Builder(ctx);

            pBuilder.setTitle(R.string.newTeamPermission);
	        pBuilder.setCancelable(permissionSelectedChoice != -1);

            pBuilder.setSingleChoiceItems(permissionList, permissionSelectedChoice, (dialogInterface, i) -> {

                permissionSelectedChoice = i;
                teamPermission.setText(permissionList[i]);

	            switch(permissionList[i]) {
		            case "Read":

			            teamPermissionDetail.setVisibility(View.VISIBLE);
			            teamPermissionDetail.setText(R.string.newTeamPermissionRead);
			            break;
		            case "Write":

			            teamPermissionDetail.setVisibility(View.VISIBLE);
			            teamPermissionDetail.setText(R.string.newTeamPermissionWrite);
			            break;
		            case "Admin":

			            teamPermissionDetail.setVisibility(View.VISIBLE);
			            teamPermissionDetail.setText(R.string.newTeamPermissionAdmin);
			            break;
		            default:

			            teamPermissionDetail.setVisibility(View.GONE);
			            break;
	            }

                dialogInterface.dismiss();
            });

            AlertDialog pDialog = pBuilder.create();
            pDialog.show();
        });

        teamAccessControls.setOnClickListener(v -> {

            teamAccessControls.setText("");
            teamAccessControlsArray.setText("");
            pushAccessList = Arrays.asList(accessControlsList);

            AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(ctx);

            aDialogBuilder.setMultiChoiceItems(accessControlsList, selectedAccessControlsTrueFalse, (dialog, which, isChecked) -> {

            })
                .setCancelable(false)
                .setTitle(R.string.newTeamAccessControls)
                .setPositiveButton(R.string.okButton, (dialog, which) -> {

                    int selectedVal = 0;
                    while(selectedVal < selectedAccessControlsTrueFalse.length)
                    {
                        boolean value = selectedAccessControlsTrueFalse[selectedVal];

                        String repoCode = "";
                        if(selectedVal == 0) {
                            repoCode = "repo.code";
                        }
                        if(selectedVal == 1) {
                            repoCode = "repo.issues";
                        }
                        if(selectedVal == 2) {
                            repoCode = "repo.pulls";
                        }
                        if(selectedVal == 3) {
                            repoCode = "repo.releases";
                        }
                        if(selectedVal == 4) {
                            repoCode = "repo.wiki";
                        }
                        if(selectedVal == 5) {
                            repoCode = "repo.ext_wiki";
                        }
                        if(selectedVal == 6) {
                            repoCode = "repo.ext_issues";
                        }

                        if(value){

                            teamAccessControls.setText(getString(R.string.newTeamPermissionValues, teamAccessControls.getText(), pushAccessList.get(selectedVal)));
                            teamAccessControlsArray.setText(getString(R.string.newTeamPermissionValuesFinal, teamAccessControlsArray.getText(), repoCode));
                        }

                        selectedVal++;
                    }

                    String data = String.valueOf(teamAccessControls.getText());
                    if(!data.equals("")) {

                        teamAccessControls.setText(data.substring(0, data.length() - 2));
                    }

                    String dataArray = String.valueOf(teamAccessControlsArray.getText());

                    if(!dataArray.equals("")) {

                        teamAccessControlsArray.setText(dataArray.substring(0, dataArray.length() - 2));
                    }
                });

            AlertDialog aDialog = aDialogBuilder.create();
            aDialog.show();
        });

        createTeamButton.setEnabled(false);

        if(!connToInternet) {

            createTeamButton.setEnabled(false);
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(8);
            shape.setColor(ResourcesCompat.getColor(getResources(), R.color.hintColor, null));
            createTeamButton.setBackground(shape);
        }
        else {

            createTeamButton.setEnabled(true);
            createTeamButton.setOnClickListener(this);
        }
    }

    private void processCreateTeam() {

        AppUtil appUtil = new AppUtil();
        final TinyDB tinyDb = TinyDB.getInstance(appCtx);
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final String orgName = tinyDb.getString("orgName");;

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        String newTeamName = teamName.getText().toString();
        String newTeamDesc = teamDesc.getText().toString();
        String newTeamPermission = teamPermission.getText().toString().toLowerCase();
        String newTeamAccessControls = teamAccessControlsArray.getText().toString();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if (newTeamName.equals("")) {

            Toasty.error(ctx, getString(R.string.teamNameEmpty));
            return;
        }

        if(!appUtil.checkStringsWithAlphaNumericDashDotUnderscore(newTeamName)) {

            Toasty.warning(ctx, getString(R.string.teamNameError));
            return;
        }

        if(!newTeamDesc.equals("")) {

            if(!appUtil.checkStrings(newTeamDesc)) {

                Toasty.warning(ctx, getString(R.string.teamDescError));
                return;
            }

            if(newTeamDesc.length() > 100) {

                Toasty.warning(ctx, getString(R.string.teamDescLimit));
                return;
            }
        }

        if (newTeamPermission.equals("")) {

            Toasty.error(ctx, getString(R.string.teamPermissionEmpty));
            return;
        }

        List<String> newTeamAccessControls_ = new ArrayList<>(Arrays.asList(newTeamAccessControls.split(",")));

        for (int i = 0; i < newTeamAccessControls_.size(); i++) {

            newTeamAccessControls_.set(i, newTeamAccessControls_.get(i).trim());
        }

        createNewTeamCall(instanceToken, orgName, newTeamName, newTeamDesc, newTeamPermission, newTeamAccessControls_, loginUid);
    }

    private void createNewTeamCall(final String instanceToken, String orgName, String newTeamName, String newTeamDesc, String newTeamPermission, List<String> newTeamAccessControls, String loginUid) {

        Teams createNewTeamJson = new Teams(newTeamName, newTeamDesc, newTeamPermission, newTeamAccessControls);

        Call<Teams> call3;

        call3 = RetrofitClient
                .getApiInterface(ctx)
                .createTeamsByOrg(Authorization.get(ctx), orgName, createNewTeamJson);

        call3.enqueue(new Callback<Teams>() {

            @Override
            public void onResponse(@NonNull Call<Teams> call, @NonNull retrofit2.Response<Teams> response2) {

                if(response2.isSuccessful()) {

                    if(response2.code() == 201) {

                        TinyDB tinyDb = TinyDB.getInstance(appCtx);
                        tinyDb.putBoolean("resumeTeams", true);

                        Toasty.success(ctx, getString(R.string.teamCreated));
                        finish();
                    }
                }
                else if(response2.code() == 404) {

                    Toasty.warning(ctx, getString(R.string.apiNotFound));
                }
                else if(response2.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
                }
                else {

                    Toasty.error(ctx, getString(R.string.teamCreatedError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Teams> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    @Override
    public void onClick(View v) {

        if(v == createTeamButton) {

            processCreateTeam();
        }
    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

}

package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Teams;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.util.Log;

/**
 * Author M M Arif
 */

public class CreateTeamByOrgActivity extends BaseActivity implements View.OnClickListener {

    final Context ctx = CreateTeamByOrgActivity.this;
    private View.OnClickListener onClickListener;
    private TextView teamName;
    private TextView teamDesc;
    private TextView teamPermission;
    private TextView teamPermissionDetail;
    private TextView teamAccessControls;
    private TextView teamAccessControlsArray;
    private Button createTeamButton;
    private String[] permissionList = {"Read", "Write", "Admin"};
    public int permissionSelectedChoice = -1;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_create_team_by_org;
    }

    private String[] accessControlsList = new String[] {
            "Code",
            "Issues",
            "Pull Request",
            "Releases",
            "Wiki",
            "External Wiki",
            "External Issues"
    };

    private List<String> pushAccessList;

    private boolean[] selectedAccessControlsTrueFalse = new boolean[]{
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

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        ImageView closeActivity = findViewById(R.id.close);
        teamName = findViewById(R.id.teamName);
        teamDesc = findViewById(R.id.teamDesc);
        teamPermission = findViewById(R.id.teamPermission);
        teamPermissionDetail = findViewById(R.id.teamPermissionDetail);
        teamAccessControls = findViewById(R.id.teamAccessControls);
        teamAccessControlsArray = findViewById(R.id.teamAccessControlsArray);
        createTeamButton = findViewById(R.id.createTeamButton);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        teamPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder pBuilder = new AlertDialog.Builder(ctx);

                pBuilder.setTitle(R.string.newTeamPermission);
                if(permissionSelectedChoice != -1) {
                    pBuilder.setCancelable(true);
                }
                else {
                    pBuilder.setCancelable(false);
                }
                pBuilder.setSingleChoiceItems(permissionList, permissionSelectedChoice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        permissionSelectedChoice = i;
                        teamPermission.setText(permissionList[i]);

                        if(permissionList[i].equals("Read")) {
                            teamPermissionDetail.setVisibility(View.VISIBLE);
                            teamPermissionDetail.setText(R.string.newTeamPermissionRead);
                        }
                        else if(permissionList[i].equals("Write")) {
                            teamPermissionDetail.setVisibility(View.VISIBLE);
                            teamPermissionDetail.setText(R.string.newTeamPermissionWrite);
                        }
                        else if(permissionList[i].equals("Admin")) {
                            teamPermissionDetail.setVisibility(View.VISIBLE);
                            teamPermissionDetail.setText(R.string.newTeamPermissionAdmin);
                        }
                        else {
                            teamPermissionDetail.setVisibility(View.GONE);
                        }

                        dialogInterface.dismiss();

                    }
                });

                AlertDialog pDialog = pBuilder.create();
                pDialog.show();

            }
        });


        teamAccessControls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                teamAccessControls.setText("");
                teamAccessControlsArray.setText("");
                pushAccessList = Arrays.asList(accessControlsList);

                AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(ctx);

                aDialogBuilder.setMultiChoiceItems(accessControlsList, selectedAccessControlsTrueFalse, new DialogInterface.OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        }

                    })
                    .setCancelable(false)
                    .setTitle(R.string.newTeamAccessControls)
                    .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

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
                            //Log.i("orgName", String.valueOf(teamAccessControlsArray.getText()));

                        }


                    });

                AlertDialog aDialog = aDialogBuilder.create();
                aDialog.show();
            }
        });

        createTeamButton.setEnabled(false);

        if(!connToInternet) {

            createTeamButton.setEnabled(false);
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 8 );
            shape.setColor(getResources().getColor(R.color.hintColor));
            createTeamButton.setBackground(shape);

        } else {

            createTeamButton.setEnabled(true);
            createTeamButton.setOnClickListener(this);

        }

    }

    private void processCreateTeam() {

        AppUtil appUtil = new AppUtil();
        final TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final String orgName = tinyDb.getString("orgName");;

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        String newTeamName = teamName.getText().toString();
        String newTeamDesc = teamDesc.getText().toString();
        String newTeamPermission = teamPermission.getText().toString().toLowerCase();
        String newTeamAccessControls = teamAccessControlsArray.getText().toString();

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if (newTeamName.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.teamNameEmpty));
            return;

        }

        if(!appUtil.checkStringsWithAlphaNumericDashDotUnderscore(newTeamName)) {

            Toasty.info(getApplicationContext(), getString(R.string.teamNameError));
            return;

        }

        if(!newTeamDesc.equals("")) {

            if(!appUtil.checkStrings(newTeamDesc)) {
                Toasty.info(getApplicationContext(), getString(R.string.teamDescError));
                return;
            }

            if(newTeamDesc.length() > 100) {
                Toasty.info(getApplicationContext(), getString(R.string.teamDescLimit));
                return;
            }

        }

        if (newTeamPermission.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.teamPermissionEmpty));
            return;

        }

        List<String> newTeamAccessControls_ = new ArrayList<>(Arrays.asList(newTeamAccessControls.split(",")));

        for (int i = 0; i < newTeamAccessControls_.size(); i++) {
            newTeamAccessControls_.set(i, newTeamAccessControls_.get(i).trim());
        }

        createNewTeamCall(instanceUrl, instanceToken, orgName, newTeamName, newTeamDesc, newTeamPermission, newTeamAccessControls_, loginUid);

    }

    private void createNewTeamCall(final String instanceUrl, final String instanceToken, String orgName, String newTeamName, String newTeamDesc, String newTeamPermission, List<String> newTeamAccessControls, String loginUid) {

        Teams createNewTeamJson = new Teams(newTeamName, newTeamDesc, newTeamPermission, newTeamAccessControls);

        Call<Teams> call3;

        call3 = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .createTeamsByOrg(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), orgName, createNewTeamJson);

        call3.enqueue(new Callback<Teams>() {

            @Override
            public void onResponse(@NonNull Call<Teams> call, @NonNull retrofit2.Response<Teams> response2) {

                if(response2.isSuccessful()) {
                    if(response2.code() == 201) {

                        TinyDB tinyDb = new TinyDB(getApplicationContext());
                        tinyDb.putBoolean("resumeTeams", true);

                        Toasty.info(getApplicationContext(), getString(R.string.teamCreated));
                        finish();

                    }

                }
                else if(response2.code() == 404) {

                    Toasty.info(getApplicationContext(), getString(R.string.apiNotFound));

                }
                else if(response2.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else {

                    Toasty.info(getApplicationContext(), getString(R.string.teamCreatedError));

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
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }

}

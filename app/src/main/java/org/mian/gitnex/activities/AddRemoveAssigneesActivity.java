package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.MultiSelectDialog;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.models.MultiSelectModel;
import org.mian.gitnex.models.UpdateIssueAssignees;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class AddRemoveAssigneesActivity extends BaseActivity {

    private ArrayList<MultiSelectModel> listOfCollaborators = new ArrayList<>();
    private ArrayList<Integer> issueAssigneesIds = new ArrayList<>();
    private Boolean assigneesFlag = false;
    private MultiSelectDialog multiSelectDialogAssignees;
    final Context ctx = this;
    private Context appCtx;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_add_remove_assignees;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));

        TinyDB tinyDb = new TinyDB(appCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final int issueIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

        getAssignees(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);

    }

    private void getAssignees(final String instanceUrl, final String instanceToken, final String repoOwner, final String repoName, final int issueIndex, final String loginUid) {

        final TinyDB tinyDb = new TinyDB(appCtx);

        Call<List<Collaborators>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getCollaborators(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<List<Collaborators>>() {

            @Override
            public void onResponse(@NonNull final Call<List<Collaborators>> call, @NonNull final retrofit2.Response<List<Collaborators>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        final List<Collaborators> collaboratorsList_ = response.body();

                        assert collaboratorsList_ != null;
                        if(collaboratorsList_.size() > 0) {
                            for (int i = 0; i < collaboratorsList_.size(); i++) {

                                listOfCollaborators.add(new MultiSelectModel(collaboratorsList_.get(i).getId(), collaboratorsList_.get(i).getUsername().trim()));

                            }
                        }

                        // get current issue assignees
                        Call<Issues> callSingleIssueAssignees = RetrofitClient
                                .getInstance(instanceUrl, ctx)
                                .getApiInterface()
                                .getIssueByIndex(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex);

                        callSingleIssueAssignees.enqueue(new Callback<Issues>() {

                            @Override
                            public void onResponse(@NonNull Call<Issues> call, @NonNull retrofit2.Response<Issues> response) {

                                if(response.code() == 200) {

                                    Issues issueAssigneesList = response.body();

                                    assert issueAssigneesList != null;
                                    if (issueAssigneesList.getAssignees() != null) {
                                        if (issueAssigneesList.getAssignees().size() > 0) {
                                            for (int i = 0; i < issueAssigneesList.getAssignees().size(); i++) {

                                                issueAssigneesIds.add(issueAssigneesList.getAssignees().get(i).getId());

                                                if(issueAssigneesList.getAssignees().get(i).getUsername().equals(loginUid)) {
                                                    listOfCollaborators.add(new MultiSelectModel(issueAssigneesList.getAssignees().get(i).getId(), issueAssigneesList.getAssignees().get(i).getUsername().trim()));
                                                }

                                            }
                                            assigneesFlag = true;
                                        }
                                    }
                                    else {
                                        listOfCollaborators.add(new MultiSelectModel(tinyDb.getInt("userId"), loginUid));
                                    }

                                    if(assigneesFlag) {

                                        multiSelectDialogAssignees = new MultiSelectDialog()
                                                .title(getResources().getString(R.string.newIssueSelectAssigneesListTitle))
                                                .titleSize(25)
                                                .positiveText(getResources().getString(R.string.saveButton))
                                                .negativeText(getResources().getString(R.string.cancelButton))
                                                .setMinSelectionLimit(0)
                                                .preSelectIDsList(issueAssigneesIds)
                                                .setMaxSelectionLimit(listOfCollaborators.size())
                                                .multiSelectList(listOfCollaborators)
                                                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                                                    @Override
                                                    public void onSelected(List<Integer> selectedIds, List<String> selectedNames, String dataString) {

                                                        Log.i("selectedNames", String.valueOf(selectedNames));

                                                        updateIssueAssignees(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, loginUid, issueIndex, selectedNames);
                                                        tinyDb.putBoolean("singleIssueUpdate", true);
                                                        CloseActivity();
                                                    }

                                                    @Override
                                                    public void onCancel() {
                                                        CloseActivity();
                                                    }
                                                });

                                    }
                                    else {

                                        multiSelectDialogAssignees = new MultiSelectDialog()
                                                .title(getResources().getString(R.string.newIssueSelectAssigneesListTitle))
                                                .titleSize(25)
                                                .positiveText(getResources().getString(R.string.saveButton))
                                                .negativeText(getResources().getString(R.string.cancelButton))
                                                .setMinSelectionLimit(0)
                                                .setMaxSelectionLimit(listOfCollaborators.size())
                                                .multiSelectList(listOfCollaborators)
                                                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                                                    @Override
                                                    public void onSelected(List<Integer> selectedIds, List<String> selectedNames, String dataString) {

                                                        updateIssueAssignees(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, loginUid, issueIndex, selectedNames);
                                                        tinyDb.putBoolean("singleIssueUpdate", true);
                                                        CloseActivity();

                                                    }

                                                    @Override
                                                    public void onCancel() {
                                                        CloseActivity();
                                                    }
                                                });

                                    }

                                    multiSelectDialogAssignees.show(getSupportFragmentManager(), "issueMultiSelectDialog");

                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {
                                Log.e("onFailure", t.toString());
                            }

                        });
                        // get current issue assignees

                    }
                    else if(response.code() == 401) {

                        AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                                getResources().getString(R.string.alertDialogTokenRevokedMessage),
                                getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                                getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                    }
                    else if(response.code() == 403) {

                        Toasty.info(ctx, ctx.getString(R.string.authorizeError));

                    }
                    else if(response.code() == 404) {

                        Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

                    }
                    else {

                        Toasty.info(ctx, getString(R.string.genericError));

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void CloseActivity() {
        this.finish();
    }

    private void updateIssueAssignees(final String instanceUrl, final String instanceToken, String repoOwner, String repoName, String loginUid, int issueIndex, List<String> issueAssigneesList) {

        UpdateIssueAssignees updateAssigneeJson = new UpdateIssueAssignees(issueAssigneesList);

        Call<JsonElement> call3;

        call3 = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .patchIssueAssignees(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex, updateAssigneeJson);

        call3.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response2) {

                if(response2.code() == 201) {

                    Toasty.info(ctx, ctx.getString(R.string.assigneesUpdated));

                }
                else if(response2.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response2.code() == 403) {

                    Toasty.info(ctx, ctx.getString(R.string.authorizeError));

                }
                else if(response2.code() == 404) {

                    Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.info(ctx, getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

}

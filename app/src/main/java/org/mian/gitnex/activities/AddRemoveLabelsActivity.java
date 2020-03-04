package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.MultiSelectDialog;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.models.MultiSelectModel;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class AddRemoveLabelsActivity extends BaseActivity {

    private ArrayList<MultiSelectModel> listOfLabels = new ArrayList<>();
    private ArrayList<Integer> issueLabelIds = new ArrayList<>();
    private Boolean labelsFlag = false;
    private MultiSelectDialog multiSelectDialogLabels;
    final Context ctx = this;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_add_remove_labels;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final int issueIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

        getLabels(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid);

    }

    private void getLabels(final String instanceUrl, final String instanceToken, final String repoOwner, final String repoName, final int issueIndex, final String loginUid) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        Call<List<Labels>> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getlabels(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<List<Labels>>() {

            @Override
            public void onResponse(@NonNull Call<List<Labels>> call, @NonNull retrofit2.Response<List<Labels>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        List<Labels> labelsList_ = response.body();

                        assert labelsList_ != null;
                        if(labelsList_.size() > 0) {
                            for (int i = 0; i < labelsList_.size(); i++) {

                                listOfLabels.add(new MultiSelectModel(labelsList_.get(i).getId(), labelsList_.get(i).getName().trim()));

                            }
                        }

                        // get current issue labels
                        Call<List<Labels>> callSingleIssueLabels = RetrofitClient
                                .getInstance(instanceUrl, getApplicationContext())
                                .getApiInterface()
                                .getIssueLabels(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex);

                        callSingleIssueLabels.enqueue(new Callback<List<Labels>>() {

                            @Override
                            public void onResponse(@NonNull Call<List<Labels>> call, @NonNull retrofit2.Response<List<Labels>> response) {

                                if(response.code() == 200) {

                                    List<Labels> issueLabelsList = response.body();

                                    assert issueLabelsList != null;
                                    if(issueLabelsList.size() > 0) {
                                        for (int i = 0; i < issueLabelsList.size(); i++) {

                                            issueLabelIds.add(issueLabelsList.get(i).getId());

                                        }
                                        labelsFlag = true;
                                    }

                                    if(labelsFlag) {

                                        multiSelectDialogLabels = new MultiSelectDialog()
                                                .title(getResources().getString(R.string.newIssueSelectLabelsListTitle))
                                                .titleSize(25)
                                                .positiveText(getResources().getString(R.string.saveButton))
                                                .negativeText(getResources().getString(R.string.cancelButton))
                                                .setMinSelectionLimit(0)
                                                .preSelectIDsList(issueLabelIds)
                                                .setMaxSelectionLimit(listOfLabels.size())
                                                .multiSelectList(listOfLabels)
                                                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                                                    @Override
                                                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {

                                                        String labelIds = selectedIds.toString();
                                                        int[] integers;
                                                        if (selectedIds.size() > 0) {

                                                            String[] items = labelIds.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                                                            integers = new int[items.length];
                                                            for (int i = 0; i < integers.length; i++) {
                                                                integers[i] = Integer.parseInt(items[i]);
                                                            }

                                                        }
                                                        else {
                                                            integers = new int[0];
                                                        }

                                                        updateIssueLabels(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex, integers, loginUid);
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

                                        multiSelectDialogLabels = new MultiSelectDialog()
                                                .title(getResources().getString(R.string.newIssueSelectLabelsListTitle))
                                                .titleSize(25)
                                                .positiveText(getResources().getString(R.string.saveButton))
                                                .negativeText(getResources().getString(R.string.cancelButton))
                                                .setMinSelectionLimit(0)
                                                .setMaxSelectionLimit(listOfLabels.size())
                                                .multiSelectList(listOfLabels)
                                                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                                                    @Override
                                                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {

                                                        String labelIds = selectedIds.toString();
                                                        int[] integers;
                                                        if (selectedIds.size() > 0) {

                                                            String[] items = labelIds.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                                                            integers = new int[items.length];
                                                            for (int i = 0; i < integers.length; i++) {
                                                                integers[i] = Integer.parseInt(items[i]);
                                                            }

                                                        }
                                                        else {
                                                            integers = new int[0];
                                                        }

                                                        updateIssueLabels(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex, integers, loginUid);
                                                        tinyDb.putBoolean("singleIssueUpdate", true);
                                                        CloseActivity();

                                                    }

                                                    @Override
                                                    public void onCancel() {
                                                        CloseActivity();
                                                    }
                                                });

                                    }

                                    multiSelectDialogLabels.show(getSupportFragmentManager(), "issueMultiSelectDialog");

                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {
                                Log.e("onFailure", t.toString());
                            }

                        });
                        // get current issue labels

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

                        Toasty.info(getApplicationContext(), getString(R.string.genericError));

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void updateIssueLabels(final String instanceUrl, final String instanceToken, String repoOwner, String repoName, int issueIndex, int[] issueLabels, String loginUid) {

        Labels patchIssueLabels = new Labels(issueLabels);

        Call<JsonElement> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .updateIssueLabels(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex, patchIssueLabels);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.code() == 200) {

                    Toasty.info(ctx, ctx.getString(R.string.labelsUpdated));

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

                    Toasty.info(getApplicationContext(), getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void CloseActivity() {
        this.finish();
    }
}

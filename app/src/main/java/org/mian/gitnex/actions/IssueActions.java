package org.mian.gitnex.actions;

import android.content.Context;
import android.util.Log;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ReplyToIssueActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UpdateIssueState;
import org.mian.gitnex.models.IssueComments;
import org.mian.gitnex.util.TinyDB;
import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class IssueActions {

    public static void editIssueComment(final Context context, final int commentId, final String commentBody) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        IssueComments commentBodyJson = new IssueComments(commentBody);
        Call<IssueComments> call;

        call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .patchIssueComment(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName, commentId, commentBodyJson);

        call.enqueue(new Callback<IssueComments>() {

            @Override
            public void onResponse(@NonNull Call<IssueComments> call, @NonNull retrofit2.Response<IssueComments> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        tinyDb.putBoolean("commentEdited", true);
                        Toasty.info(context, context.getString(R.string.editCommentUpdatedText));
                        ((ReplyToIssueActivity)context).finish();

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 403) {

                    Toasty.info(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.info(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.info(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<IssueComments> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    public static void closeReopenIssue(final Context context, final int issueIndex, final String issueState) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        UpdateIssueState issueStatJson = new UpdateIssueState(issueState);
        Call<JsonElement> call;

        call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .closeReopenIssue(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName, issueIndex, issueStatJson);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 201) {

                        tinyDb.putBoolean("resumeIssues", true);
                        tinyDb.putBoolean("resumeClosedIssues", true);
                        if(issueState.equals("closed")) {
                            Toasty.info(context, context.getString(R.string.issueStateClosed));
                        }
                        else if(issueState.equals("open")) {
                            Toasty.info(context, context.getString(R.string.issueStateReopened));
                        }

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 403) {

                    Toasty.info(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.info(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.info(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

}

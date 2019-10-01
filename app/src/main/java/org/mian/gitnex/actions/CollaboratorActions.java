package org.mian.gitnex.actions;

import android.content.Context;
import android.util.Log;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AddCollaboratorToRepositoryActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.Permission;
import org.mian.gitnex.util.TinyDB;
import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CollaboratorActions {

    public static void deleteCollaborator(final Context context, final String searchKeyword, String userName) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<Collaborators> call;

        call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .deleteCollaborator(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName, userName);

        call.enqueue(new Callback<Collaborators>() {

            @Override
            public void onResponse(@NonNull Call<Collaborators> call, @NonNull retrofit2.Response<Collaborators> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

                        Toasty.info(context, context.getString(R.string.removeCollaboratorToastText));
                        ((AddCollaboratorToRepositoryActivity)context).finish();
                        //Log.i("addCollaboratorSearch", addCollaboratorSearch.getText().toString());
                        //tinyDb.putBoolean("updateDataSet", true);
                        //AddCollaboratorToRepositoryActivity usersSearchData = new AddCollaboratorToRepositoryActivity();
                        //usersSearchData.loadUserSearchList(instanceUrl, instanceToken, searchKeyword, context);

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
            public void onFailure(@NonNull Call<Collaborators> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    public static void addCollaborator(final Context context, String permission, String userName) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Permission permissionString = new Permission(permission);
        Call<Permission> call;

        call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .addCollaborator(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName, userName, permissionString);

        call.enqueue(new Callback<Permission>() {

            @Override
            public void onResponse(@NonNull Call<Permission> call, @NonNull retrofit2.Response<Permission> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

                        Toasty.info(context, context.getString(R.string.addCollaboratorToastText));
                        ((AddCollaboratorToRepositoryActivity)context).finish();
                        //AddCollaboratorToRepositoryActivity usersSearchData = new AddCollaboratorToRepositoryActivity();
                        //usersSearchData.loadUserSearchList(instanceUrl, instanceToken, searchKeyword, context);

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
            public void onFailure(@NonNull Call<Permission> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

}

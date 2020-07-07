package org.mian.gitnex.actions;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class RepositoryActions {

    public static void starRepository(final Context context) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<JsonElement> call;

        call = RetrofitClient
                .getInstance(instanceUrl, context)
                .getApiInterface()
                .starRepository(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

                        tinyDb.putBoolean("repoCreated", true);
                        Toasty.info(context, context.getString(R.string.starRepositorySuccess));

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

    public static void unStarRepository(final Context context) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<JsonElement> call;

        call = RetrofitClient
                .getInstance(instanceUrl, context)
                .getApiInterface()
                .unStarRepository(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

                        tinyDb.putBoolean("repoCreated", true);
                        Toasty.info(context, context.getString(R.string.unStarRepositorySuccess));

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

    public static void watchRepository(final Context context) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<JsonElement> call;

        call = RetrofitClient
                .getInstance(instanceUrl, context)
                .getApiInterface()
                .watchRepository(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        tinyDb.putBoolean("repoCreated", true);
                        Toasty.info(context, context.getString(R.string.watchRepositorySuccess));

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

    public static void unWatchRepository(final Context context) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<JsonElement> call;

        call = RetrofitClient
                .getInstance(instanceUrl, context)
                .getApiInterface()
                .unWatchRepository(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.code() == 204) {

                    tinyDb.putBoolean("repoCreated", true);
                    Toasty.info(context, context.getString(R.string.unWatchRepositorySuccess));

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

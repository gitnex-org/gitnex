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

        final TinyDB tinyDb = TinyDB.getInstance(context);

        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<JsonElement> call;

        call = RetrofitClient
                .getApiInterface(context)
                .starRepository(Authorization.get(context), repoOwner, repoName);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

                        tinyDb.putBoolean("repoCreated", true);
                        Toasty.success(context, context.getString(R.string.starRepositorySuccess));

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.cancelButton),
                            context.getResources().getString(R.string.navLogout));

                }
                else if(response.code() == 403) {

                    Toasty.error(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.warning(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.error(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    public static void unStarRepository(final Context context) {

        final TinyDB tinyDb = TinyDB.getInstance(context);

        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<JsonElement> call;

        call = RetrofitClient
                .getApiInterface(context)
                .unStarRepository(Authorization.get(context), repoOwner, repoName);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

                        tinyDb.putBoolean("repoCreated", true);
                        Toasty.success(context, context.getString(R.string.unStarRepositorySuccess));

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.cancelButton),
                            context.getResources().getString(R.string.navLogout));

                }
                else if(response.code() == 403) {

                    Toasty.error(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.warning(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.error(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    public static void watchRepository(final Context context) {

        final TinyDB tinyDb = TinyDB.getInstance(context);

        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<JsonElement> call;

        call = RetrofitClient
                .getApiInterface(context)
                .watchRepository(Authorization.get(context), repoOwner, repoName);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        tinyDb.putBoolean("repoCreated", true);
                        Toasty.success(context, context.getString(R.string.watchRepositorySuccess));

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.cancelButton),
                            context.getResources().getString(R.string.navLogout));

                }
                else if(response.code() == 403) {

                    Toasty.error(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.warning(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.error(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    public static void unWatchRepository(final Context context) {

        final TinyDB tinyDb = TinyDB.getInstance(context);

        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<JsonElement> call;

        call = RetrofitClient
                .getApiInterface(context)
                .unWatchRepository(Authorization.get(context), repoOwner, repoName);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.code() == 204) {

                    tinyDb.putBoolean("repoCreated", true);
                    Toasty.success(context, context.getString(R.string.unWatchRepositorySuccess));

                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.cancelButton),
                            context.getResources().getString(R.string.navLogout));

                }
                else if(response.code() == 403) {

                    Toasty.error(context, context.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.warning(context, context.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.error(context, context.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

}

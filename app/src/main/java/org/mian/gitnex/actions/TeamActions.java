package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AddNewTeamMemberActivity;
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

public class TeamActions {

	public static void removeTeamMember(final Context context, String userName, int teamId) {

		final TinyDB tinyDb = new TinyDB(context);
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		Call<JsonElement> call;

		call = RetrofitClient
				.getInstance(instanceUrl, context)
				.getApiInterface()
				.removeTeamMember(Authorization.returnAuthentication(context, loginUid, instanceToken), teamId, userName);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {

					if(response.code() == 204) {

						tinyDb.putBoolean("teamActionFlag", true);
						Toasty.success(context, context.getString(R.string.memberRemovedMessage));
						((AddNewTeamMemberActivity)context).finish();

					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
							context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
							context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

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

				Toasty.error(context, context.getResources().getString(R.string.genericServerResponseError));

			}
		});

	}

	public static void addTeamMember(final Context context, String userName, int teamId) {

		final TinyDB tinyDb = new TinyDB(context);
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		Call<JsonElement> call;

		call = RetrofitClient
				.getInstance(instanceUrl, context)
				.getApiInterface()
				.addTeamMember(Authorization.returnAuthentication(context, loginUid, instanceToken), teamId, userName);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {

					if(response.code() == 204) {

						tinyDb.putBoolean("teamActionFlag", true);
						Toasty.success(context, context.getString(R.string.memberAddedMessage));
						((AddNewTeamMemberActivity)context).finish();

					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
							context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
							context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

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

				Toasty.error(context, context.getResources().getString(R.string.genericServerResponseError));

			}
		});

	}

}

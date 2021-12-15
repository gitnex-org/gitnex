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

		final TinyDB tinyDb = TinyDB.getInstance(context);

		Call<JsonElement> call;

		call = RetrofitClient
				.getApiInterface(context)
				.removeTeamMember(Authorization.get(context), teamId, userName);

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

				Toasty.error(context, context.getResources().getString(R.string.genericServerResponseError));

			}
		});

	}

	public static void addTeamMember(final Context context, String userName, int teamId) {

		final TinyDB tinyDb = TinyDB.getInstance(context);

		Call<JsonElement> call = RetrofitClient
				.getApiInterface(context)
				.addTeamMember(Authorization.get(context), teamId, userName);

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

				Toasty.error(context, context.getResources().getString(R.string.genericServerResponseError));

			}
		});

	}

}

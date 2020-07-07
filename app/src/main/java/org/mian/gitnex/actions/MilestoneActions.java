package org.mian.gitnex.actions;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Milestones;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class MilestoneActions {

	static final private String TAG = "MilestoneActions : ";

	public static void closeMilestone(final Context ctx, int milestoneId_) {

		final TinyDB tinyDB = new TinyDB(ctx);

		final String instanceUrl = tinyDB.getString("instanceUrl");
		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDB.getString("loginUid");
		final String token = "token " + tinyDB.getString(loginUid + "-token");

		Milestones milestoneStateJson = new Milestones("closed");
		Call<JsonElement> call;

		call = RetrofitClient
				.getInstance(instanceUrl, ctx)
				.getApiInterface()
				.closeReopenMilestone(token, repoOwner, repoName, milestoneId_, milestoneStateJson);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {

					Toasty.info(ctx, ctx.getString(R.string.milestoneStatusUpdate));

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else {

					Toasty.info(ctx, ctx.getString(R.string.genericError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());

			}

		});


	}

	public static void openMilestone(final Context ctx, int milestoneId_) {

		final TinyDB tinyDB = new TinyDB(ctx);

		final String instanceUrl = tinyDB.getString("instanceUrl");
		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDB.getString("loginUid");
		final String token = "token " + tinyDB.getString(loginUid + "-token");

		Milestones milestoneStateJson = new Milestones("open");
		Call<JsonElement> call;

		call = RetrofitClient
				.getInstance(instanceUrl, ctx)
				.getApiInterface()
				.closeReopenMilestone(token, repoOwner, repoName, milestoneId_, milestoneStateJson);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {

					Toasty.info(ctx, ctx.getString(R.string.milestoneStatusUpdate));

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else {

					Toasty.info(ctx, ctx.getString(R.string.genericError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());

			}

		});

	}

}

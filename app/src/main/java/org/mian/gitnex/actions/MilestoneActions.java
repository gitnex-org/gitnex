package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.gitnex.tea4j.models.Milestones;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class MilestoneActions {

	public static void closeMilestone(final Context ctx, int milestoneId_, RepositoryContext repository) {

		Milestones milestoneStateJson = new Milestones("closed");
		Call<JsonElement> call;

		call = RetrofitClient
				.getApiInterface(ctx)
				.closeReopenMilestone(((BaseActivity) ctx).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), milestoneId_, milestoneStateJson);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {

					Toasty.success(ctx, ctx.getString(R.string.milestoneStatusUpdate));
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
						ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.cancelButton),
						ctx.getResources().getString(R.string.navLogout));
				}
				else {

					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	public static void openMilestone(final Context ctx, int milestoneId_, RepositoryContext repository) {

		Milestones milestoneStateJson = new Milestones("open");
		Call<JsonElement> call;

		call = RetrofitClient
				.getApiInterface(ctx)
				.closeReopenMilestone(((BaseActivity) ctx).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), milestoneId_, milestoneStateJson);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {

					Toasty.success(ctx, ctx.getString(R.string.milestoneStatusUpdate));
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
						ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.cancelButton),
						ctx.getResources().getString(R.string.navLogout));
				}
				else {

					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}
}

package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author qwerty287
 */

public class PullRequestActions {

	public static void deleteHeadBranch(Context context, String repoOwner, String repoName, String headBranch, boolean showToasts) {
		Call<JsonElement> call = RetrofitClient
				.getApiInterface(context)
				.deleteBranch(((BaseActivity) context).getAccount().getAuthorization(), repoOwner, repoName, headBranch);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.code() == 204) {

					if(showToasts) {
						Toasty.success(context, context.getString(R.string.deleteBranchSuccess));
					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
						context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
						context.getResources().getString(R.string.cancelButton), context.getResources().getString(R.string.navLogout));
				}
				else if(response.code() == 403) {

					if(showToasts) {
						Toasty.error(context, context.getString(R.string.authorizeError));
					}
				}
				else if(response.code() == 404) {

					if(showToasts) {
						Toasty.warning(context, context.getString(R.string.deleteBranchErrorNotFound));
					}
				}
				else {

					if(showToasts) {
						Toasty.error(context, context.getString(R.string.genericError));
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				if(showToasts) {
					Toasty.error(context, context.getString(R.string.deleteBranchError));
				}
			}

		});
	}

	public static void updatePr(Context context, String repoOwner, String repoName, String index, Boolean rebase) {

		String strategy;
		if(rebase == null) {
			strategy = null;
		}
		else if(!rebase) {
			strategy = "merge";
		}
		else {
			strategy = "rebase";
		}

		RetrofitClient.getApiInterface(context).updatePullRequest(((BaseActivity) context).getAccount().getAuthorization(), repoOwner, repoName, Integer.parseInt(index), strategy)
			.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) {

				if(response.isSuccessful()) {
					Toasty.success(context, context.getString(R.string.updatePrSuccess));
				}
				else {
					if(response.code() == 403) {
						Toasty.error(context, context.getString(R.string.authorizeError));
					}
					else if(response.code() == 409) {
						Toasty.error(context, context.getString(R.string.updatePrConflict));
					}
					else {
						Toasty.error(context, context.getString(R.string.genericError));
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call call, @NonNull Throwable t) {

				Toasty.error(context, context.getString(R.string.genericError));
			}
		});
	}
}

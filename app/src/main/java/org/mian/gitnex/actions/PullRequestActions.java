package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
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
		Call<Void> call = RetrofitClient
				.getApiInterface(context)
				.repoDeleteBranch(repoOwner, repoName, headBranch);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				if(response.code() == 204) {

					if(showToasts) {
						Toasty.success(context, context.getString(R.string.deleteBranchSuccess));
					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(context);
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
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

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
		RetrofitClient.getApiInterface(context).repoUpdatePullRequest(repoOwner, repoName, Long.valueOf(index), strategy)
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

package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author qwerty287
 */

public class PullRequestActions {

	public static void deleteHeadBranch(Context context, String repoOwner, String repoName, String headBranch, boolean showToasts) {
		Call<JsonElement> call = RetrofitClient
				.getApiInterface(context)
				.deleteBranch(Authorization.get(context), repoOwner, repoName, headBranch);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.code() == 204) {

					if(showToasts) Toasty.success(context, context.getString(R.string.deleteBranchSuccess));
				}
				else if(response.code() == 401) {

					AlertDialogs
						.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle), context.getResources().getString(R.string.alertDialogTokenRevokedMessage), context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
				}
				else if(response.code() == 403) {

					if(showToasts) Toasty.error(context, context.getString(R.string.authorizeError));
				}
				else if(response.code() == 404) {

					if(showToasts) Toasty.warning(context, context.getString(R.string.deleteBranchErrorNotFound));
				}
				else {

					if(showToasts) Toasty.error(context, context.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				if(showToasts) Toasty.error(context, context.getString(R.string.deleteBranchError));
			}

		});
	}

}

package org.mian.gitnex.actions;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ReplyToIssueActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.IssueComments;
import org.mian.gitnex.models.UpdateIssueState;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class IssueActions {

	public static void editIssueComment(final Context ctx, final int commentId, final String commentBody) {

		final TinyDB tinyDb = new TinyDB(ctx);
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		IssueComments commentBodyJson = new IssueComments(commentBody);
		Call<IssueComments> call;

		call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().patchIssueComment(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, commentId, commentBodyJson);

		call.enqueue(new Callback<IssueComments>() {

			@Override
			public void onResponse(@NonNull Call<IssueComments> call, @NonNull retrofit2.Response<IssueComments> response) {

				if(response.isSuccessful()) {
					if(response.code() == 200) {

						tinyDb.putBoolean("commentEdited", true);
						Toasty.success(ctx, ctx.getString(R.string.editCommentUpdatedText));
						((ReplyToIssueActivity) ctx).finish();

					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else if(response.code() == 403) {

					Toasty.error(ctx, ctx.getString(R.string.authorizeError));

				}
				else if(response.code() == 404) {

					Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));

				}
				else {

					Toasty.error(ctx, ctx.getString(R.string.genericError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<IssueComments> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	public static void closeReopenIssue(final Context ctx, final int issueIndex, final String issueState) {

		final TinyDB tinyDb = new TinyDB(ctx);
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		UpdateIssueState issueStatJson = new UpdateIssueState(issueState);
		Call<JsonElement> call;

		call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().closeReopenIssue(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex, issueStatJson);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {
					if(response.code() == 201) {

						tinyDb.putBoolean("resumeIssues", true);
						tinyDb.putBoolean("resumeClosedIssues", true);

						if(issueState.equals("closed")) {

							Toasty.success(ctx, ctx.getString(R.string.issueStateClosed));
							tinyDb.putString("issueState", "closed");

						}
						else if(issueState.equals("open")) {

							Toasty.success(ctx, ctx.getString(R.string.issueStateReopened));
							tinyDb.putString("issueState", "open");

						}

					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else if(response.code() == 403) {

					Toasty.error(ctx, ctx.getString(R.string.authorizeError));

				}
				else if(response.code() == 404) {

					Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));

				}
				else {

					Toasty.error(ctx, ctx.getString(R.string.genericError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	public static void subscribe(final Context ctx) {

		final TinyDB tinyDB = new TinyDB(ctx);

		final String instanceUrl = tinyDB.getString("instanceUrl");
		String[] repoFullName = tinyDB.getString("repoFullName").split("/");
		if(repoFullName.length != 2) {
			return;
		}
		final String userLogin = tinyDB.getString("userLogin");
		final String token = "token " + tinyDB.getString(tinyDB.getString("loginUid") + "-token");
		final int issueNr = Integer.parseInt(tinyDB.getString("issueNumber"));

		Call<Void> call;

		call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().addIssueSubscriber(token, repoFullName[0], repoFullName[1], issueNr, userLogin);

		call.enqueue(new Callback<Void>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				if(response.isSuccessful()) {

					if(response.code() == 201) {

						Toasty.success(ctx, ctx.getString(R.string.subscribedSuccessfully));
						tinyDB.putBoolean("issueSubscribed", true);

					}
					else if(response.code() == 200) {

						tinyDB.putBoolean("issueSubscribed", true);
						Toasty.success(ctx, ctx.getString(R.string.alreadySubscribed));

					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else {

					Toasty.error(ctx, ctx.getString(R.string.subscriptionError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

				Toasty.success(ctx, ctx.getString(R.string.unsubscribedSuccessfully));
			}
		});

	}

	public static void unsubscribe(final Context ctx) {

		final TinyDB tinyDB = new TinyDB(ctx);

		final String instanceUrl = tinyDB.getString("instanceUrl");
		String[] repoFullName = tinyDB.getString("repoFullName").split("/");
		if(repoFullName.length != 2) {
			return;
		}
		final String userLogin = tinyDB.getString("userLogin");
		final String token = "token " + tinyDB.getString(tinyDB.getString("loginUid") + "-token");
		final int issueNr = Integer.parseInt(tinyDB.getString("issueNumber"));

		Call<Void> call;

		call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().delIssueSubscriber(token, repoFullName[0], repoFullName[1], issueNr, userLogin);

		call.enqueue(new Callback<Void>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				if(response.isSuccessful()) {

					if(response.code() == 201) {

						Toasty.success(ctx, ctx.getString(R.string.unsubscribedSuccessfully));
						tinyDB.putBoolean("issueSubscribed", false);

					}
					else if(response.code() == 200) {

						tinyDB.putBoolean("issueSubscribed", false);
						Toasty.success(ctx, ctx.getString(R.string.alreadyUnsubscribed));

					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else {

					Toasty.error(ctx, ctx.getString(R.string.unsubscriptionError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.unsubscriptionError));
			}
		});
	}

}

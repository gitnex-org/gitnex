package org.mian.gitnex.actions;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ReplyToIssueActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.IssueComments;
import org.mian.gitnex.models.UpdateIssueState;
import org.mian.gitnex.util.TinyDB;
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
						Toasty.info(ctx, ctx.getString(R.string.editCommentUpdatedText));
						((ReplyToIssueActivity) ctx).finish();

					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else if(response.code() == 403) {

					Toasty.info(ctx, ctx.getString(R.string.authorizeError));

				}
				else if(response.code() == 404) {

					Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

				}
				else {

					Toasty.info(ctx, ctx.getString(R.string.genericError));

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

							Toasty.info(ctx, ctx.getString(R.string.issueStateClosed));
							tinyDb.putString("issueState", "closed");

						}
						else if(issueState.equals("open")) {

							Toasty.info(ctx, ctx.getString(R.string.issueStateReopened));
							tinyDb.putString("issueState", "open");

						}

					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else if(response.code() == 403) {

					Toasty.info(ctx, ctx.getString(R.string.authorizeError));

				}
				else if(response.code() == 404) {

					Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

				}
				else {

					Toasty.info(ctx, ctx.getString(R.string.genericError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	public static void subscribe(final Context ctx, final TextView subscribeIssue, final TextView unsubscribeIssue) {

		final TinyDB tinyDB = new TinyDB(ctx);

		final String instanceUrl = tinyDB.getString("instanceUrl");
		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDB.getString("loginUid");
		final String userLogin = tinyDB.getString("userLogin");
		final String token = "token " + tinyDB.getString(loginUid + "-token");
		final int issueNr = Integer.parseInt(tinyDB.getString("issueNumber"));

		Call<Void> call;

		call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().addIssueSubscriber(token, repoOwner, repoName, issueNr, userLogin);

		call.enqueue(new Callback<Void>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				if(response.isSuccessful()) {

					if(response.code() == 201) {

						unsubscribeIssue.setVisibility(View.VISIBLE);
						subscribeIssue.setVisibility(View.GONE);
						Toasty.info(ctx, ctx.getString(R.string.issueSubscribtion));
						tinyDB.putString("issueSubscriptionState", "unsubscribeToIssue");

					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else {

					Toasty.info(ctx, ctx.getString(R.string.issueSubscribtionError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

				Toasty.info(ctx, ctx.getString(R.string.issueSubscribtionError));
			}
		});

	}

	public static void unsubscribe(final Context ctx, final TextView subscribeIssue, final TextView unsubscribeIssue) {

		final TinyDB tinyDB = new TinyDB(ctx);

		final String instanceUrl = tinyDB.getString("instanceUrl");
		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDB.getString("loginUid");
		final String userLogin = tinyDB.getString("userLogin");
		final String token = "token " + tinyDB.getString(loginUid + "-token");
		final int issueNr = Integer.parseInt(tinyDB.getString("issueNumber"));

		Call<Void> call;

		call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().delIssueSubscriber(token, repoOwner, repoName, issueNr, userLogin);

		call.enqueue(new Callback<Void>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				if(response.isSuccessful()) {

					if(response.code() == 201) {

						unsubscribeIssue.setVisibility(View.GONE);
						subscribeIssue.setVisibility(View.VISIBLE);
						Toasty.info(ctx, ctx.getString(R.string.issueUnsubscribtion));
						tinyDB.putString("issueSubscriptionState", "subscribeToIssue");

					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else {

					Toasty.info(ctx, ctx.getString(R.string.issueUnsubscribtionError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

				Toasty.info(ctx, ctx.getString(R.string.issueUnsubscribtionError));
			}
		});
	}

}

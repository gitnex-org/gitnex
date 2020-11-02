package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.IssueComments;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.models.UpdateIssueState;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class IssueActions {

	public static ActionResult<Response<?>> edit(Context context, String comment, int commentId) {

		ActionResult<Response<?>> actionResult = new ActionResult<>();

		TinyDB tinyDb = TinyDB.getInstance(context);

		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");

		String repoOwner = parts[0];
		String repoName = parts[1];

		Call<IssueComments> call = RetrofitClient
			.getApiInterface(context)
			.patchIssueComment(Authorization.get(context), repoOwner, repoName, commentId, new IssueComments(comment));

		call.enqueue(new Callback<IssueComments>() {

			@Override
			public void onResponse(@NonNull Call<IssueComments> call, @NonNull retrofit2.Response<IssueComments> response) {

				switch(response.code()) {

					case 200:
						actionResult.finish(ActionResult.Status.SUCCESS);
						break;

					case 401:
						actionResult.finish(ActionResult.Status.FAILED, response);
						AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle), context.getResources().getString(R.string.alertDialogTokenRevokedMessage), context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
						break;

					default:
						actionResult.finish(ActionResult.Status.FAILED, response);
						break;

				}
			}

			@Override
			public void onFailure(@NonNull Call<IssueComments> call, @NonNull Throwable t) {

				actionResult.finish(ActionResult.Status.FAILED);
			}
		});

		return actionResult;

	}

	public static void closeReopenIssue(final Context ctx, final int issueIndex, final String issueState) {

		final TinyDB tinyDb = TinyDB.getInstance(ctx);

		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");

		final String repoOwner = parts[0];
		final String repoName = parts[1];

		UpdateIssueState issueStatJson = new UpdateIssueState(issueState);
		Call<JsonElement> call;

		call = RetrofitClient
			.getApiInterface(ctx)
			.closeReopenIssue(Authorization.get(ctx), repoOwner, repoName, issueIndex, issueStatJson);

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

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});

	}

	public static void subscribe(final Context ctx) {

		final TinyDB tinyDB = TinyDB.getInstance(ctx);

		String[] repoFullName = tinyDB.getString("repoFullName").split("/");

		if(repoFullName.length != 2) {
			return;
		}

		final String userLogin = tinyDB.getString("userLogin");
		final String token = "token " + tinyDB.getString(tinyDB.getString("loginUid") + "-token");
		final int issueNr = Integer.parseInt(tinyDB.getString("issueNumber"));

		Call<Void> call;

		call = RetrofitClient
			.getApiInterface(ctx)
			.addIssueSubscriber(token, repoFullName[0], repoFullName[1], issueNr, userLogin);

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

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});

	}

	public static void unsubscribe(final Context ctx) {

		final TinyDB tinyDB = TinyDB.getInstance(ctx);

		String[] repoFullName = tinyDB.getString("repoFullName").split("/");
		if(repoFullName.length != 2) {
			return;
		}
		final String userLogin = tinyDB.getString("userLogin");
		final String token = "token " + tinyDB.getString(tinyDB.getString("loginUid") + "-token");
		final int issueNr = Integer.parseInt(tinyDB.getString("issueNumber"));

		Call<Void> call;

		call = RetrofitClient.getApiInterface(ctx).delIssueSubscriber(token, repoFullName[0], repoFullName[1], issueNr, userLogin);

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

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	public static ActionResult<ActionResult.None> reply(Context context, String comment, int issueIndex) {

		ActionResult<ActionResult.None> actionResult = new ActionResult<>();

		TinyDB tinyDb = TinyDB.getInstance(context);

		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		String repoOwner = parts[0];
		String repoName = parts[1];

		Issues issueComment = new Issues(comment);

		Call<Issues> call = RetrofitClient
			.getApiInterface(context)
			.replyCommentToIssue(Authorization.get(context), repoOwner, repoName, issueIndex, issueComment);

		call.enqueue(new Callback<Issues>() {

			@Override
			public void onResponse(@NonNull Call<Issues> call, @NonNull retrofit2.Response<Issues> response) {

				if(response.code() == 201) {

					actionResult.finish(ActionResult.Status.SUCCESS);

					tinyDb.putBoolean("commentPosted", true);
					tinyDb.putBoolean("resumeIssues", true);
					tinyDb.putBoolean("resumePullRequests", true);

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(context, context.getString(R.string.alertDialogTokenRevokedTitle),
						context.getString(R.string.alertDialogTokenRevokedMessage),
						context.getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
						context.getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else {

					actionResult.finish(ActionResult.Status.FAILED);
				}
			}

			@Override
			public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {

				Toasty.error(context, context.getResources().getString(R.string.genericServerResponseError));
			}
		});

		return actionResult;

	}

}

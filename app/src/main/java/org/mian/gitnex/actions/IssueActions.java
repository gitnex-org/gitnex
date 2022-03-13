package org.mian.gitnex.actions;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonElement;
import org.gitnex.tea4j.models.IssueComments;
import org.gitnex.tea4j.models.Issues;
import org.gitnex.tea4j.models.UpdateIssueState;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class IssueActions {

	public static ActionResult<Response<?>> edit(Context context, String comment, int commentId, IssueContext issue) {

		ActionResult<Response<?>> actionResult = new ActionResult<>();

		Call<IssueComments> call = RetrofitClient
			.getApiInterface(context)
			.patchIssueComment(((BaseActivity) context).getAccount().getAuthorization(), issue.getRepository().getOwner(),
				issue.getRepository().getName(), commentId, new IssueComments(comment));

		call.enqueue(new Callback<IssueComments>() {

			@Override
			public void onResponse(@NonNull Call<IssueComments> call, @NonNull retrofit2.Response<IssueComments> response) {

				switch(response.code()) {

					case 200:
						actionResult.finish(ActionResult.Status.SUCCESS);
						break;

					case 401:
						actionResult.finish(ActionResult.Status.FAILED, response);
						AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle), context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							context.getResources().getString(R.string.cancelButton), context.getResources().getString(R.string.navLogout));
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

	public static void closeReopenIssue(final Context ctx, final String issueState, IssueContext issue) {

		UpdateIssueState issueStatJson = new UpdateIssueState(issueState);
		Call<JsonElement> call;

		call = RetrofitClient
			.getApiInterface(ctx)
			.closeReopenIssue(((BaseActivity) ctx).getAccount().getAuthorization(), issue.getRepository().getOwner(),
				issue.getRepository().getName(), issue.getIssueIndex(), issueStatJson);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.isSuccessful()) {
					if(response.code() == 201) {

						if (issue.hasIssue()) {
							IssuesFragment.resumeIssues = issue.getIssue().getPull_request() == null;
							PullRequestsFragment.resumePullRequests = issue.getIssue().getPull_request() != null;
						}
						if(issueState.equals("closed")) {
							Toasty.success(ctx, ctx.getString(R.string.issueStateClosed));
						}
						else if(issueState.equals("open")) {
							Toasty.success(ctx, ctx.getString(R.string.issueStateReopened));
						}

						((IssueDetailActivity) ctx).singleIssueUpdate = true;
						((IssueDetailActivity) ctx).onResume();
					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.cancelButton), ctx.getResources().getString(R.string.navLogout));

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

	public static void subscribe(final Context ctx, IssueContext issue) {

		Call<Void> call;

		call = RetrofitClient
			.getApiInterface(ctx)
			.addIssueSubscriber(((BaseActivity) ctx).getAccount().getAuthorization(), issue.getRepository().getOwner(),
				issue.getRepository().getName(), issue.getIssueIndex(), ((BaseActivity) ctx).getAccount().getAccount().getUserName());

		call.enqueue(new Callback<Void>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				if(response.isSuccessful()) {

					if(response.code() == 201) {

						Toasty.success(ctx, ctx.getString(R.string.subscribedSuccessfully));

					}
					else if(response.code() == 200) {

						Toasty.success(ctx, ctx.getString(R.string.alreadySubscribed));

					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.cancelButton), ctx.getResources().getString(R.string.navLogout));

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

	public static void unsubscribe(final Context ctx, IssueContext issue) {

		Call<Void> call;

		call = RetrofitClient.getApiInterface(ctx).delIssueSubscriber(((BaseActivity) ctx).getAccount().getAuthorization(), issue.getRepository().getOwner(),
			issue.getRepository().getName(), issue.getIssueIndex(), ((BaseActivity) ctx).getAccount().getAccount().getUserName());

		call.enqueue(new Callback<Void>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				if(response.isSuccessful()) {

					if(response.code() == 201) {

						Toasty.success(ctx, ctx.getString(R.string.unsubscribedSuccessfully));

					}
					else if(response.code() == 200) {

						Toasty.success(ctx, ctx.getString(R.string.alreadyUnsubscribed));

					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle), ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.cancelButton), ctx.getResources().getString(R.string.navLogout));

				}
				else {

					Toasty.error(ctx, ctx.getString(R.string.unSubscriptionError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	public static ActionResult<ActionResult.None> reply(Context context, String comment, IssueContext issue) {

		ActionResult<ActionResult.None> actionResult = new ActionResult<>();

		Issues issueComment = new Issues(comment);

		Call<Issues> call = RetrofitClient
			.getApiInterface(context)
			.replyCommentToIssue(((BaseActivity) context).getAccount().getAuthorization(), issue.getRepository().getOwner(),
				issue.getRepository().getName(), issue.getIssueIndex(), issueComment);

		call.enqueue(new Callback<Issues>() {

			@Override
			public void onResponse(@NonNull Call<Issues> call, @NonNull retrofit2.Response<Issues> response) {

				if(response.code() == 201) {
					actionResult.finish(ActionResult.Status.SUCCESS);

					if (issue.hasIssue()) {
						IssuesFragment.resumeIssues = issue.getIssue().getPull_request() == null;
						PullRequestsFragment.resumePullRequests = issue.getIssue().getPull_request() != null;
					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(context, context.getString(R.string.alertDialogTokenRevokedTitle),
						context.getString(R.string.alertDialogTokenRevokedMessage),
						context.getString(R.string.cancelButton),
						context.getString(R.string.navLogout));

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

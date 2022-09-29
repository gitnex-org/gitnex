package org.mian.gitnex.actions;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.Comment;
import org.gitnex.tea4j.v2.models.CreateIssueCommentOption;
import org.gitnex.tea4j.v2.models.EditIssueCommentOption;
import org.gitnex.tea4j.v2.models.EditIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
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
 * @author M M Arif
 */
public class IssueActions {

	public static ActionResult<Response<?>> edit(
			Context context, String comment, int commentId, IssueContext issue) {

		ActionResult<Response<?>> actionResult = new ActionResult<>();

		EditIssueCommentOption commentObj = new EditIssueCommentOption();
		commentObj.setBody(comment);

		Call<Comment> call =
				RetrofitClient.getApiInterface(context)
						.issueEditComment(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) commentId,
								commentObj);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Comment> call,
							@NonNull retrofit2.Response<Comment> response) {

						switch (response.code()) {
							case 200:
								actionResult.finish(ActionResult.Status.SUCCESS);
								break;

							case 401:
								actionResult.finish(ActionResult.Status.FAILED, response);
								AlertDialogs.authorizationTokenRevokedDialog(context);
								break;

							default:
								actionResult.finish(ActionResult.Status.FAILED, response);
								break;
						}
					}

					@Override
					public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {

						actionResult.finish(ActionResult.Status.FAILED);
					}
				});

		return actionResult;
	}

	public static void closeReopenIssue(
			final Context ctx, final String issueState, IssueContext issue) {

		EditIssueOption issueStatJson = new EditIssueOption();
		issueStatJson.setState(issueState);
		Call<Issue> call =
				RetrofitClient.getApiInterface(ctx)
						.issueEditIssue(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								issueStatJson);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Issue> call,
							@NonNull retrofit2.Response<Issue> response) {

						if (response.isSuccessful()) {
							if (response.code() == 201) {

								if (issue.hasIssue()) {
									IssuesFragment.resumeIssues =
											issue.getIssue().getPullRequest() == null;
									PullRequestsFragment.resumePullRequests =
											issue.getIssue().getPullRequest() != null;
								}
								if (issue.getIssueType().equalsIgnoreCase("Pull")) {
									if (issueState.equals("closed")) {
										Toasty.success(ctx, ctx.getString(R.string.prClosed));
									} else if (issueState.equals("open")) {
										Toasty.success(ctx, ctx.getString(R.string.prReopened));
									}
								} else {
									if (issueState.equals("closed")) {
										Toasty.success(
												ctx, ctx.getString(R.string.issueStateClosed));
									} else if (issueState.equals("open")) {
										Toasty.success(
												ctx, ctx.getString(R.string.issueStateReopened));
									}
								}

								IssueDetailActivity.singleIssueUpdate = true;
								((IssueDetailActivity) ctx).onResume();
								if (((Activity) ctx).getIntent().getStringExtra("openedFromLink")
												== null
										|| !((Activity) ctx)
												.getIntent()
												.getStringExtra("openedFromLink")
												.equals("true")) {
									RepoDetailActivity.updateRepo = true;
								}
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 403) {

							Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						} else if (response.code() == 404) {

							Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						} else {

							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

						Toasty.error(
								ctx,
								ctx.getResources().getString(R.string.genericServerResponseError));
					}
				});
	}

	public static void subscribe(final Context ctx, IssueContext issue) {

		Call<Void> call;

		call =
				RetrofitClient.getApiInterface(ctx)
						.issueAddSubscription(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								((BaseActivity) ctx).getAccount().getAccount().getUserName());

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.isSuccessful()) {

							if (response.code() == 201) {

								Toasty.success(ctx, ctx.getString(R.string.subscribedSuccessfully));
							} else if (response.code() == 200) {

								Toasty.success(ctx, ctx.getString(R.string.alreadySubscribed));
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							Toasty.error(ctx, ctx.getString(R.string.subscriptionError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

						Toasty.error(
								ctx,
								ctx.getResources().getString(R.string.genericServerResponseError));
					}
				});
	}

	public static void unsubscribe(final Context ctx, IssueContext issue) {

		Call<Void> call;

		call =
				RetrofitClient.getApiInterface(ctx)
						.issueDeleteSubscription(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								((BaseActivity) ctx).getAccount().getAccount().getUserName());

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.isSuccessful()) {

							if (response.code() == 201) {

								Toasty.success(
										ctx, ctx.getString(R.string.unsubscribedSuccessfully));
							} else if (response.code() == 200) {

								Toasty.success(ctx, ctx.getString(R.string.alreadyUnsubscribed));
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							Toasty.error(ctx, ctx.getString(R.string.unSubscriptionError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

						Toasty.error(
								ctx,
								ctx.getResources().getString(R.string.genericServerResponseError));
					}
				});
	}

	public static ActionResult<ActionResult.None> reply(
			Context context, String comment, IssueContext issue) {

		ActionResult<ActionResult.None> actionResult = new ActionResult<>();

		CreateIssueCommentOption issueComment = new CreateIssueCommentOption();
		issueComment.setBody(comment);

		Call<Comment> call =
				RetrofitClient.getApiInterface(context)
						.issueCreateComment(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								issueComment);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Comment> call,
							@NonNull retrofit2.Response<Comment> response) {

						if (response.code() == 201) {
							actionResult.finish(ActionResult.Status.SUCCESS);

							if (issue.hasIssue()) {
								IssuesFragment.resumeIssues =
										issue.getIssue().getPullRequest() == null;
								PullRequestsFragment.resumePullRequests =
										issue.getIssue().getPullRequest() != null;
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(context);

						} else {

							actionResult.finish(ActionResult.Status.FAILED);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {

						Toasty.error(
								context,
								context.getResources()
										.getString(R.string.genericServerResponseError));
					}
				});

		return actionResult;
	}
}

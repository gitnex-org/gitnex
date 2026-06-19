package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.PullRequestDetailActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssueOrPRCheckerViewModel {

	public static void checkAndOpen(
			Context context, RepositoryContext repo, long number, boolean openedFromLink) {

		Call<PullRequest> prCall =
				RetrofitClient.getApiInterface(context)
						.repoGetPullRequest(repo.getOwner(), repo.getName(), number);

		prCall.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<PullRequest> call,
							@NonNull Response<PullRequest> response) {
						if (response.isSuccessful() && response.body() != null) {
							openPR(context, repo, (int) number, openedFromLink);
						} else if (response.code() == 404) {
							checkIfIssue(context, repo, number, openedFromLink);
						} else {
							checkIfIssue(context, repo, number, openedFromLink);
						}
					}

					@Override
					public void onFailure(@NonNull Call<PullRequest> call, @NonNull Throwable t) {
						checkIfIssue(context, repo, number, openedFromLink);
					}
				});
	}

	private static void checkIfIssue(
			Context context, RepositoryContext repo, long number, boolean openedFromLink) {

		Call<Issue> issueCall =
				RetrofitClient.getApiInterface(context)
						.issueGetIssue(repo.getOwner(), repo.getName(), number);

		issueCall.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
						if (response.isSuccessful() && response.body() != null) {
							openIssue(context, repo, (int) number, openedFromLink);
						} else {
							Toasty.show(context, context.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
						Toasty.show(context, context.getString(R.string.genericError));
					}
				});
	}

	private static void openIssue(
			Context context, RepositoryContext repo, int number, boolean openedFromLink) {
		Intent intent = new Intent(context, IssueDetailActivity.class);
		intent.putExtra("owner", repo.getOwner());
		intent.putExtra("repo", repo.getName());
		intent.putExtra("issueNumber", number);
		intent.putExtra("fetchIssueObject", true);
		if (openedFromLink) {
			intent.putExtra("openedFromLink", "true");
		}
		context.startActivity(intent);
	}

	private static void openPR(
			Context context, RepositoryContext repo, int number, boolean openedFromLink) {
		Intent intent = new Intent(context, PullRequestDetailActivity.class);
		intent.putExtra("owner", repo.getOwner());
		intent.putExtra("repo", repo.getName());
		intent.putExtra("prNumber", number);
		if (openedFromLink) {
			intent.putExtra("openedFromLink", "true");
		}
		context.startActivity(intent);
	}
}

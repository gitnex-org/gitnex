package org.mian.gitnex.helpers;

import android.content.Context;

/**
 * Author M M Arif
 */

public abstract class StaticGlobalVariables {

	// generic values
	public static int resultLimitNewGiteaInstances = 25; // Gitea 1.12 and above
	public static int resultLimitOldGiteaInstances = 10; // Gitea 1.11 and below
	public static String defaultOldestTimestamp = "1970-01-01T00:00:00+00:00";

	public static int getCurrentResultLimit(Context context) {

		Version version = new Version(new TinyDB(context).getString("giteaVersion"));
		return version.higherOrEqual("1.12") ? resultLimitNewGiteaInstances : resultLimitOldGiteaInstances;

	}

	// tags
	public static String tagMilestonesFragment = "MilestonesFragment";
	public static String tagPullRequestsList = "PullRequestsListFragment";
	public static String tagIssuesList = "IssuesListFragment";
	public static String tagMilestonesAdapter = "MilestonesAdapter";
	public static String draftsRepository = "DraftsRepository";
	public static String repositoriesRepository = "RepositoriesRepository";
	public static String replyToIssueActivity = "ReplyToIssueActivity";
	public static String tagDraftsBottomSheet = "BottomSheetDraftsFragment";
	public static String userAccountsRepository = "UserAccountsRepository";

	// issues variables
	public static int issuesPageInit = 1;
	public static String issuesRequestType = "issues";

	// pull request
	public static int prPageInit = 1;

	// milestone
	public static int milestonesPageInit = 1;

	// drafts
	public static String draftTypeComment = "comment";
	public static String draftTypeIssue = "issue";

}

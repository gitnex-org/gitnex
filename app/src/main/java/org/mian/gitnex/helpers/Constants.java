package org.mian.gitnex.helpers;

import android.content.Context;

/**
 * Author M M Arif
 */

public class Constants {

	// generic values
	public static final int resultLimitNewGiteaInstances = 25; // Gitea 1.12 and above
	public static final int resultLimitOldGiteaInstances = 10; // Gitea 1.11 and below
	public static final String defaultOldestTimestamp = "1970-01-01T00:00:00+00:00";

	public static int getCurrentResultLimit(Context context) {

		Version version = new Version(TinyDB.getInstance(context).getString("giteaVersion"));
		return version.higherOrEqual("1.12") ? resultLimitNewGiteaInstances : resultLimitOldGiteaInstances;

	}

	// tags
	public static final String tagMilestonesFragment = "MilestonesFragment";
	public static final String tagPullRequestsList = "PullRequestsListFragment";
	public static final String tagIssuesList = "IssuesListFragment";
	public static final String tagMilestonesAdapter = "MilestonesAdapter";
	public static final String draftsApi = "DraftsApi";
	public static final String repositoriesApi = "RepositoriesApi";
	public static final String replyToIssueActivity = "ReplyToIssueActivity";
	public static final String tagDraftsBottomSheet = "BottomSheetDraftsFragment";
	public static final String userAccountsApi = "UserAccountsApi";

	// issues variables
	public static final int issuesPageInit = 1;
	public static final String issuesRequestType = "issues";

	// pull request
	public static final int prPageInit = 1;

	// milestone
	public static final int milestonesPageInit = 1;

	// drafts
	public static final String draftTypeComment = "comment";
	public static final String draftTypeIssue = "Issue";
	public static final String draftTypePull = "Pull";

	// polling - notifications
	public static final int minimumPollingDelay = 1;
	public static final int defaultPollingDelay = 15;
	public static final int maximumPollingDelay = 720;

	public static final int maximumFileViewerSize = 3 * 1024 * 1024;

	public static final String mainNotificationChannelId = "main_channel";
	public static final String downloadNotificationChannelId = "dl_channel";

	public static final String[] fallbackReactions = new String[]{"+1", "-1", "laugh", "hooray", "confused", "heart", "rocket", "eyes"};

}

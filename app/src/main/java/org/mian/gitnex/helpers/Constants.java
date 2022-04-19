package org.mian.gitnex.helpers;

import android.content.Context;
import org.mian.gitnex.activities.BaseActivity;

/**
 * @author M M Arif
 */

public class Constants {

	public static int getCurrentResultLimit(Context context) {
		return ((BaseActivity) context).getAccount().requiresVersion("1.15") ? ((BaseActivity) context).getAccount().getDefaultPageLimit() : ((BaseActivity) context).getAccount().getMaxPageLimit();
	}

	// tags
	public static final String tagPullRequestsList = "PullRequestFragment";
	public static final String tagIssuesList = "IssuesListFragment";
	public static final String tagMilestonesAdapter = "MilestonesAdapter";
	public static final String draftsApi = "DraftsApi";
	public static final String repositoriesApi = "RepositoriesApi";
	public static final String tagDraftsBottomSheet = "BottomSheetDraftsFragment";
	public static final String userAccountsApi = "UserAccountsApi";
	public static final String publicOrganizations = "PublicOrganizations";
	public static final String exploreUsers = "ExploreUsers";
	public static final String exploreIssues = "ExploreIssues";
	public static final String tagFollowers = "TagFollowers";
	public static final String tagFollowing = "TagFollowing";

	// issues variables
	public static final int issuesPageInit = 1;
	public static final String issuesRequestType = "issues";

	// pull request
	public static final int prPageInit = 1;

	// drafts
	public static final String draftTypeComment = "comment";
	public static final String draftTypeIssue = "Issue";
	public static final String draftTypePull = "Pull";

	// polling - notifications
	public static final int minimumPollingDelay = 1;
	public static final int defaultPollingDelay = 15;
	public static final int maximumPollingDelay = 720;

	// public organizations
	public static final int publicOrganizationsPageInit = 1;

	public static final int maximumFileViewerSize = 3 * 1024 * 1024;

	public static final String mainNotificationChannelId = "main_channel";
	public static final String downloadNotificationChannelId = "dl_channel";

	public static final long[] defaultVibrationPattern = new long[] { 1000, 1000 };

	public static final String[] fallbackReactions = new String[]{"+1", "-1", "laugh", "hooray", "confused", "heart", "rocket", "eyes"};

	// work managers
	public static final String notificationsWorkerId  = "notifications_worker";
}

package org.mian.gitnex.helpers;

/**
 * Author M M Arif
 */

public interface StaticGlobalVariables {

	// generic values
	int resultLimitNewGiteaInstances = 25; // Gitea 1.12 and above
	int resultLimitOldGiteaInstances = 10; // Gitea 1.11 and below

	// tags
	String tagMilestonesFragment = "MilestonesFragment";
	String tagPullRequestsList = "PullRequestsListFragment";
	String tagIssuesList = "IssuesListFragment";
	String tagMilestonesAdapter = "MilestonesAdapter";
	String draftsRepository = "DraftsRepository";
	String repositoriesRepository = "RepositoriesRepository";
	String replyToIssueActivity = "ReplyToIssueActivity";
	String tagDraftsBottomSheet = "BottomSheetDraftsFragment";
	String userAccountsRepository = "UserAccountsRepository";

	// issues variables
	int issuesPageInit = 1;
	String issuesRequestType = "issues";

	// pull request
	int prPageInit = 1;

	// milestone
	int milestonesPageInit = 1;

	// drafts
	String draftTypeComment = "comment";
	String draftTypeIssue = "issue";

}

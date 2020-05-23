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

	// issues variables
	int issuesPageInit = 1;
	String issuesRequestType = "issues";

	// pull request
	int prPageInit = 1;

	// milestone
	int milestonesPageInit = 1;

}

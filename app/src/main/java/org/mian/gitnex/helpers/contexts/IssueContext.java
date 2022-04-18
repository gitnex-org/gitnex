package org.mian.gitnex.helpers.contexts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.activities.BaseActivity;
import java.io.Serializable;

public class IssueContext implements Serializable {

	public static final String INTENT_EXTRA = "issue";

	public static IssueContext fromIntent(Intent intent) {
		return (IssueContext) intent.getSerializableExtra(INTENT_EXTRA);
	}

	public static IssueContext fromBundle(Bundle bundle) {
		return (IssueContext) bundle.getSerializable(INTENT_EXTRA);
	}

	private Issue issue;
	private PullRequest pullRequest;
	private boolean isSubscribed;
	private final RepositoryContext repository;
	private int issueIndex = 0;
	private String issueType;

	public IssueContext(RepositoryContext repository, int issueIndex, String issueType) {
		this.repository = repository;
		this.issueIndex = issueIndex;
		this.issueType = issueType;
	}

	public IssueContext(Issue issue, PullRequest pullRequest, RepositoryContext repository) {
		this.issue = issue;
		this.issueType = issue.getPullRequest() == null ?
			"Issue" : "Pull";
		this.pullRequest = pullRequest;
		this.repository = repository;
	}

	public IssueContext(PullRequest pullRequest, RepositoryContext repository) {
		this.issueType = "Pull";
		this.pullRequest = pullRequest;
		this.repository = repository;
	}

	public IssueContext(Issue issue, RepositoryContext repository) {
		this.issue = issue;
		this.issueType = issue.getPullRequest() == null ?
			"Issue" : "Pull";

		this.repository = repository;
	}

	public IssueContext(Issue issue, PullRequest pullRequest, Repository repository, Context context) {
		this.issue = issue;
		this.issueType = issue.getPullRequest() == null ?
			"Issue" : "Pull";
		this.pullRequest = pullRequest;

		this.repository = new RepositoryContext(repository, context);
	}

	public IssueContext(Issue issue, Repository repository, Context context) {
		this.issue = issue;
		this.issueType = issue.getPullRequest() == null ?
			"Issue" : "Pull";
		this.repository = new RepositoryContext(repository, context);
	}

	public PullRequest getPullRequest() {

		return pullRequest;
	}

	public Issue getIssue() {

		return issue;
	}

	public void setPullRequest(PullRequest pullRequest) {

		this.pullRequest = pullRequest;
	}

	public <T extends BaseActivity> Intent getIntent(Context context, Class<T> clazz) {
		Intent intent = new Intent(context, clazz);
		intent.putExtra(INTENT_EXTRA, this);
		return intent;
	}

	public Bundle getBundle() {
		Bundle bundle = new Bundle();
		bundle.putSerializable(INTENT_EXTRA, this);
		return bundle;
	}

	public boolean hasIssue() {

		return issue != null;
	}

	public RepositoryContext getRepository() {

		return repository;
	}

	public int getIssueIndex() {

		return Math.toIntExact(issueIndex != 0 ? issueIndex : issue != null ? issue.getNumber() : pullRequest.getNumber());
	}

	public boolean isSubscribed() {

		return isSubscribed;
	}

	public void setSubscribed(boolean subscribed) {

		isSubscribed = subscribed;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
		if(issue != null) {
			this.issueType = issue.getPullRequest() == null ? "Issue" : "Pull";
		}
	}

	public String getIssueType() {

		return issueType;
	}

	public boolean prIsFork() {
		if(pullRequest.getHead().getRepo() != null) {
			return !pullRequest.getHead().getRepo().getFullName().equals(getRepository().getFullName());
		}
		else {
			// PR was done from a deleted fork
			return true;
		}
	}

}

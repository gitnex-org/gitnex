package org.mian.gitnex.helpers.contexts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import org.gitnex.tea4j.models.Issues;
import org.gitnex.tea4j.models.PullRequests;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.R;
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

	private Issues issue;
	private PullRequests pullRequest;
	private boolean isSubscribed;
	private final RepositoryContext repository;
	private int issueIndex = 0;
	private String issueType;

	public IssueContext(RepositoryContext repository, int issueIndex, String issueType) {
		this.repository = repository;
		this.issueIndex = issueIndex;
		this.issueType = issueType;
	}

	public IssueContext(Issues issue, PullRequests pullRequest, RepositoryContext repository) {
		this.issue = issue;
		this.issueType = issue.getPull_request() == null ?
			"Issue" : "Pull";
		this.pullRequest = pullRequest;
		this.repository = repository;
	}

	public IssueContext(PullRequests pullRequest, RepositoryContext repository) {
		this.issueType = "Pull";
		this.pullRequest = pullRequest;
		this.repository = repository;
	}

	public IssueContext(Issues issue, RepositoryContext repository) {
		this.issue = issue;
		this.issueType = issue.getPull_request() == null ?
			"Issue" : "Pull";

		this.repository = repository;
	}

	public IssueContext(Issues issue, PullRequests pullRequest, UserRepositories repository, Context context) {
		this.issue = issue;
		this.issueType = issue.getPull_request() == null ?
			"Issue" : "Pull";
		this.pullRequest = pullRequest;

		this.repository = new RepositoryContext(repository, context);
	}

	public IssueContext(Issues issue, UserRepositories repository, Context context) {
		this.issue = issue;
		this.issueType = issue.getPull_request() == null ?
			"Issue" : "Pull";
		this.repository = new RepositoryContext(repository, context);
	}

	public PullRequests getPullRequest() {

		return pullRequest;
	}

	public Issues getIssue() {

		return issue;
	}

	public void setPullRequest(PullRequests pullRequest) {

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

		return issueIndex != 0 ?
			issueIndex :
			issue != null ?
			issue.getNumber() : pullRequest.getNumber();
	}

	public boolean isSubscribed() {

		return isSubscribed;
	}

	public void setSubscribed(boolean subscribed) {

		isSubscribed = subscribed;
	}

	public void setIssue(Issues issue) {
		this.issue = issue;
		if(issue != null) {
			this.issueType = issue.getPull_request() == null ? "Issue" : "Pull";
		}
	}

	public String getIssueType() {

		return issueType;
	}

	public boolean prIsFork() {
		if(pullRequest.getHead().getRepo() != null) {
			return !pullRequest.getHead().getRepo().getFull_name().equals(getRepository().getFullName());
		}
		else {
			// PR was done from a deleted fork
			return true;
		}
	}

}

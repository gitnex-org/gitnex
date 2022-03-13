package org.mian.gitnex.helpers.contexts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import java.io.Serializable;
import java.util.Objects;

public class RepositoryContext implements Serializable {

	public static final String INTENT_EXTRA = "repository";

	public static RepositoryContext fromIntent(Intent intent) {
		return (RepositoryContext) intent.getSerializableExtra(INTENT_EXTRA);
	}

	public static RepositoryContext fromBundle(Bundle bundle) {
		return (RepositoryContext) bundle.getSerializable(INTENT_EXTRA);
	}

	public enum State {
		OPEN,
		CLOSED;


		@NonNull
		@Override
		public String toString() {
			if(this == OPEN) {
				return "open";
			}
			return "closed";
		}
	}

	private final AccountContext account;
	private UserRepositories repository;
	private final String owner;
	private final String name;

	private State issueState = State.OPEN;
	private State prState = State.OPEN;
	private State milestoneState = State.OPEN;
	private boolean releasesViewTypeIsTag = false;

	private String branchRef;
	private String issueMilestoneFilterName;

	private boolean starred = false;
	private boolean watched = false;

	private int repositoryId = 0;
	private Repository repositoryModel = null;

	public RepositoryContext(UserRepositories repository, Context context) {
		this.account = ((BaseActivity) context).getAccount();
		this.repository = repository;
		this.name = repository.getName();
		this.owner = repository.getFullName().split("/")[0];
	}

	public RepositoryContext(String owner, String name, Context context) {
		this.account = ((BaseActivity) context).getAccount();
		this.owner = owner;
		this.name = name;
	}

	public State getIssueState() {

		return issueState;
	}

	public State getMilestoneState() {

		return milestoneState;
	}

	public State getPrState() {

		return prState;
	}

	public UserRepositories getRepository() {

		return repository;
	}

	public void setIssueState(State issueState) {

		this.issueState = issueState;
	}

	public void setMilestoneState(State milestoneState) {

		this.milestoneState = milestoneState;
	}

	public void setPrState(State prState) {

		this.prState = prState;
	}

	public String getBranchRef() {

		return branchRef;
	}

	public void setBranchRef(String branchRef) {

		this.branchRef = branchRef;
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

	public String getIssueMilestoneFilterName() {

		return issueMilestoneFilterName;
	}

	public void setIssueMilestoneFilterName(String issueMilestoneFilterName) {

		this.issueMilestoneFilterName = issueMilestoneFilterName;
	}

	public String getOwner() {
		return owner;
	}

	public String getFullName() {
		return owner + "/" + name;
	}

	public String getName() {
		return name;
	}

	public UserRepositories.permissionsObject getPermissions() {
		return repository != null ? repository.getPermissions() : new UserRepositories.permissionsObject();
	}

	public void setRepository(UserRepositories repository) {
		this.repository = repository;
		if(!repository.getFullName().equals(getFullName())) {
			throw new IllegalArgumentException("repo does not match owner and name");
		}
	}

	public boolean hasRepository() {
		return repository != null;
	}

	public boolean isStarred() {

		return starred;
	}

	public boolean isWatched() {

		return watched;
	}

	public void setStarred(boolean starred) {

		this.starred = starred;
	}

	public void setWatched(boolean watched) {

		this.watched = watched;
	}

	public int getRepositoryId() {

		return repositoryId;
	}

	public void setRepositoryId(int repositoryId) {

		this.repositoryId = repositoryId;
	}

	public Repository getRepositoryModel() {

		return repositoryModel;
	}

	public void setRepositoryModel(Repository repositoryModel) {

		this.repositoryModel = repositoryModel;
	}

	public Repository loadRepositoryModel(Context context) {
		repositoryModel = Objects.requireNonNull(BaseApi.getInstance(context, RepositoriesApi.class)).fetchRepositoryById(repositoryId);
		return repositoryModel;
	}

	public void checkAccountSwitch(Context context) {
		if(((BaseActivity) context).getAccount().getAccount().getAccountId() != account.getAccount().getAccountId() &&
			account.getAccount().getAccountId() == TinyDB.getInstance(context).getInt("currentActiveAccountId")) {
			// user changed account using a deep link or a submodule
			AppUtil.switchToAccount(context, account.getAccount());
		}
	}

	public boolean isReleasesViewTypeIsTag() {

		return releasesViewTypeIsTag;
	}

	public void setReleasesViewTypeIsTag(boolean releasesViewTypeIsTag) {

		this.releasesViewTypeIsTag = releasesViewTypeIsTag;
	}

}

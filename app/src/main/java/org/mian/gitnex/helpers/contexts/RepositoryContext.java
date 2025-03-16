package org.mian.gitnex.helpers.contexts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Permission;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;

/**
 * @author qwerty287
 */
public class RepositoryContext implements Serializable {

	public static final String INTENT_EXTRA = "repository";
	private final AccountContext account;
	private final String owner;
	private final String name;
	private org.gitnex.tea4j.v2.models.Repository repository;
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
	private String mentionedBy;

	public RepositoryContext(org.gitnex.tea4j.v2.models.Repository repository, Context context) {
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

	public static RepositoryContext fromIntent(Intent intent) {
		return (RepositoryContext) intent.getSerializableExtra(INTENT_EXTRA);
	}

	public static RepositoryContext fromBundle(Bundle bundle) {
		return (RepositoryContext) bundle.getSerializable(INTENT_EXTRA);
	}

	public State getIssueState() {
		return issueState;
	}

	public void setIssueState(State issueState) {
		this.issueState = issueState;
	}

	public State getMilestoneState() {
		return milestoneState;
	}

	public void setMilestoneState(State milestoneState) {
		this.milestoneState = milestoneState;
	}

	public State getPrState() {
		return prState;
	}

	public void setPrState(State prState) {
		this.prState = prState;
	}

	public org.gitnex.tea4j.v2.models.Repository getRepository() {
		return repository;
	}

	public void setRepository(org.gitnex.tea4j.v2.models.Repository repository) {
		this.repository = repository;
		if (!repository.getFullName().equalsIgnoreCase(getFullName())) {
			throw new IllegalArgumentException("repo does not match owner and name");
		}
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

	public Permission getPermissions() {
		return repository != null ? repository.getPermissions() : new Permission();
	}

	public boolean hasRepository() {
		return repository != null;
	}

	public boolean isStarred() {
		return starred;
	}

	public void setStarred(boolean starred) {
		this.starred = starred;
	}

	public boolean isWatched() {
		return watched;
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

	public String getMentionedBy() {
		return mentionedBy;
	}

	public void setMentionedBy(String mentionedBy) {
		this.mentionedBy = mentionedBy;
	}

	public Repository loadRepositoryModel(Context context) {
		repositoryModel =
				Objects.requireNonNull(BaseApi.getInstance(context, RepositoriesApi.class))
						.fetchRepositoryById(repositoryId);
		return repositoryModel;
	}

	public void checkAccountSwitch(Context context) {
		if (((BaseActivity) context).getAccount().getAccount().getAccountId()
						!= account.getAccount().getAccountId()
				&& account.getAccount().getAccountId()
						== TinyDB.getInstance(context).getInt("currentActiveAccountId")) {
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

	public void removeRepository() {
		repository = null;
	}

	public int saveToDB(Context context) {
		int currentActiveAccountId = TinyDB.getInstance(context).getInt("currentActiveAccountId");
		RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

		assert repositoryData != null;
		Repository getMostVisitedValue =
				repositoryData.getRepository(currentActiveAccountId, getOwner(), getName());

		if (getMostVisitedValue == null) {
			long id =
					repositoryData.insertRepository(
							currentActiveAccountId, getOwner(), getName(), 1);
			setRepositoryId((int) id);
			return (int) id;
		} else {
			Repository data =
					repositoryData.getRepository(currentActiveAccountId, getOwner(), getName());
			setRepositoryId(data.getRepositoryId());
			repositoryData.updateRepositoryMostVisited(
					getMostVisitedValue.getMostVisited() + 1, data.getRepositoryId());
			return data.getRepositoryId();
		}
	}

	public enum State {
		OPEN,
		CLOSED;

		@NonNull @Override
		public String toString() {
			if (this == OPEN) {
				return "open";
			}
			return "closed";
		}
	}

	@Serial
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.defaultWriteObject();
	}

	@Serial
	private void readObject(java.io.ObjectInputStream in)
			throws java.io.IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
}

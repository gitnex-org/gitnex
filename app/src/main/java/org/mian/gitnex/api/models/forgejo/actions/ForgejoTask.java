package org.mian.gitnex.api.models.forgejo.actions;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

/**
 * @author mmarif
 */
public class ForgejoTask implements Serializable {

	@SerializedName("created_at")
	private Date createdAt;

	@SerializedName("display_title")
	private String displayTitle;

	private String event;

	@SerializedName("head_branch")
	private String headBranch;

	@SerializedName("head_sha")
	private String headSha;

	private long id;
	private String name;

	@SerializedName("run_number")
	private long runNumber;

	@SerializedName("run_started_at")
	private Date runStartedAt;

	private String status;

	@SerializedName("updated_at")
	private Date updatedAt;

	private String url;

	@SerializedName("workflow_id")
	private String workflowId;

	// Getters and setters
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getDisplayTitle() {
		return displayTitle;
	}

	public void setDisplayTitle(String displayTitle) {
		this.displayTitle = displayTitle;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getHeadBranch() {
		return headBranch;
	}

	public void setHeadBranch(String headBranch) {
		this.headBranch = headBranch;
	}

	public String getHeadSha() {
		return headSha;
	}

	public void setHeadSha(String headSha) {
		this.headSha = headSha;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getRunNumber() {
		return runNumber;
	}

	public void setRunNumber(long runNumber) {
		this.runNumber = runNumber;
	}

	public Date getRunStartedAt() {
		return runStartedAt;
	}

	public void setRunStartedAt(Date runStartedAt) {
		this.runStartedAt = runStartedAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}
}

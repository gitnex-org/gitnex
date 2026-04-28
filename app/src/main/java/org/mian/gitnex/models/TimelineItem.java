package org.mian.gitnex.models;

import java.util.Date;
import java.util.List;
import org.gitnex.tea4j.v2.models.Attachment;
import org.gitnex.tea4j.v2.models.Label;
import org.gitnex.tea4j.v2.models.Milestone;
import org.gitnex.tea4j.v2.models.Reaction;
import org.gitnex.tea4j.v2.models.User;

/**
 * @author mmarif
 */
public class TimelineItem {

	private long id;
	private String type;
	private User user;
	private String body;
	private Date createdAt;
	private Date updatedAt;
	private String htmlUrl;
	private List<Reaction> reactions;
	private List<Attachment> attachments;
	private Label label;
	private Milestone milestone;
	private Milestone oldMilestone;
	private User assignee;
	private boolean removedAssignee;
	private String oldTitle;
	private String newTitle;
	private String oldRef;
	private String newRef;
	private List<String> commitIds;

	public TimelineItem() {}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public List<Reaction> getReactions() {
		return reactions;
	}

	public void setReactions(List<Reaction> reactions) {
		this.reactions = reactions;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

	public Milestone getOldMilestone() {
		return oldMilestone;
	}

	public void setOldMilestone(Milestone oldMilestone) {
		this.oldMilestone = oldMilestone;
	}

	public User getAssignee() {
		return assignee;
	}

	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}

	public boolean isRemovedAssignee() {
		return removedAssignee;
	}

	public void setRemovedAssignee(boolean removedAssignee) {
		this.removedAssignee = removedAssignee;
	}

	public String getOldTitle() {
		return oldTitle;
	}

	public void setOldTitle(String oldTitle) {
		this.oldTitle = oldTitle;
	}

	public String getNewTitle() {
		return newTitle;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}

	public String getOldRef() {
		return oldRef;
	}

	public void setOldRef(String oldRef) {
		this.oldRef = oldRef;
	}

	public String getNewRef() {
		return newRef;
	}

	public void setNewRef(String newRef) {
		this.newRef = newRef;
	}

	public List<String> getCommitIds() {
		return commitIds;
	}

	public void setCommitIds(List<String> commitIds) {
		this.commitIds = commitIds;
	}
}

package org.mian.gitnex.models;

/**
 * Author opyale
 */

public class NotificationThread {
	private int id;
	private boolean pinned;
	private UserRepositories repository;
	private NotificationSubject subject;
	private boolean unread;
	private String updated_at;
	private String url;

	public NotificationThread(int id, boolean pinned, UserRepositories repository, NotificationSubject subject, boolean unread, String updated_at, String url) {

		this.id = id;
		this.pinned = pinned;
		this.repository = repository;
		this.subject = subject;
		this.unread = unread;
		this.updated_at = updated_at;
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public boolean isPinned() {
		return pinned;
	}

	public UserRepositories getRepository() {
		return repository;
	}

	public NotificationSubject getSubject() {
		return subject;
	}

	public boolean isUnread() {
		return unread;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public String getUrl() {
		return url;
	}

}

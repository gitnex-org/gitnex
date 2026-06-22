package org.mian.gitnex.database.models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * @author mmarif
 */
@Entity(tableName = "bookmarks")
public class Bookmark implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private int bookmarkId;

	private int accountId;
	private String type;
	@Nullable private String bookmarkAction = "";
	@Nullable private String owner = "";
	@Nullable private String repo = "";
	@Nullable private String identifier = "";
	@Nullable private String branch = "";
	@Nullable private String url = "";
	private String title;
	private long createdAt;

	public int getBookmarkId() {
		return bookmarkId;
	}

	public void setBookmarkId(int bookmarkId) {
		this.bookmarkId = bookmarkId;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Nullable public String getBookmarkAction() {
		return bookmarkAction;
	}

	public void setBookmarkAction(@Nullable String bookmarkAction) {
		this.bookmarkAction = bookmarkAction;
	}

	@Nullable public String getOwner() {
		return owner;
	}

	public void setOwner(@Nullable String owner) {
		this.owner = owner;
	}

	@Nullable public String getRepo() {
		return repo;
	}

	public void setRepo(@Nullable String repo) {
		this.repo = repo;
	}

	@Nullable public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(@Nullable String identifier) {
		this.identifier = identifier;
	}

	@Nullable public String getBranch() {
		return branch;
	}

	public void setBranch(@Nullable String branch) {
		this.branch = branch;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Nullable public String getUrl() {
		return url;
	}

	public void setUrl(@Nullable String url) {
		this.url = url;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}
}

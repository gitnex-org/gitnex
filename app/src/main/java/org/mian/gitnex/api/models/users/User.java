package org.mian.gitnex.api.models.users;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class User implements Serializable {

	@SerializedName("id")
	private Long id;

	@SerializedName("login")
	private String login;

	@SerializedName("login_name")
	private String loginName;

	@SerializedName("full_name")
	private String fullName;

	@SerializedName("email")
	private String email;

	@SerializedName("avatar_url")
	private String avatarUrl;

	@SerializedName("html_url")
	private String htmlUrl;

	@SerializedName("active")
	private boolean active;

	@SerializedName("is_admin")
	private boolean isAdmin;

	@SerializedName("prohibit_login")
	private boolean prohibitLogin;

	@SerializedName("restricted")
	private boolean restricted;

	@SerializedName("visibility")
	private String visibility;

	@SerializedName("created")
	private String created;

	@SerializedName("last_login")
	private String lastLogin;

	@SerializedName("location")
	private String location;

	@SerializedName("website")
	private String website;

	@SerializedName("description")
	private String description;

	@SerializedName("pronouns")
	private String pronouns;

	@SerializedName("language")
	private String language;

	@SerializedName("followers_count")
	private Long followersCount;

	@SerializedName("following_count")
	private Long followingCount;

	@SerializedName("starred_repos_count")
	private Long starredReposCount;

	@SerializedName("source_id")
	private Long sourceId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean admin) {
		isAdmin = admin;
	}

	public boolean isProhibitLogin() {
		return prohibitLogin;
	}

	public void setProhibitLogin(boolean prohibitLogin) {
		this.prohibitLogin = prohibitLogin;
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(String lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getPronouns() {
		return pronouns;
	}

	public void setPronouns(String pronouns) {
		this.pronouns = pronouns;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Long getFollowersCount() {
		return followersCount;
	}

	public void setFollowersCount(Long followersCount) {
		this.followersCount = followersCount;
	}

	public Long getFollowingCount() {
		return followingCount;
	}

	public void setFollowingCount(Long followingCount) {
		this.followingCount = followingCount;
	}

	public Long getStarredReposCount() {
		return starredReposCount;
	}

	public void setStarredReposCount(Long starredReposCount) {
		this.starredReposCount = starredReposCount;
	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}
}

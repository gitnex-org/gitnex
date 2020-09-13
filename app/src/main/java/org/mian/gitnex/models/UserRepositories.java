package org.mian.gitnex.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * Author M M Arif
 */

public class UserRepositories {

	private int id;
	private String name;
	private String full_name;
	private String description;
	@SerializedName("private")
	private boolean privateFlag;
	private String stars_count;
	private String watchers_count;
	private String open_issues_count;
	private String open_pr_counter;
	private String release_counter;
	private String html_url;
	private String default_branch;
	private Date created_at;
	private Date updated_at;
	private String clone_url;
	private long size;
	private String ssh_url;
	private String website;
	private String forks_count;
	private Boolean has_issues;
	private String avatar_url;
	private boolean archived;
	private boolean allow_merge_commits;
	private boolean allow_rebase;
	private boolean allow_rebase_explicit;
	private boolean allow_squash_merge;
	private boolean has_pull_requests;
	private boolean has_wiki;
	private boolean ignore_whitespace_conflicts;
	private boolean template;

	private permissionsObject permissions;
	private externalIssueTrackerObject external_tracker;
	private externalWikiObject external_wiki;
	private internalTimeTrackerObject internal_tracker;

	public UserRepositories(String body) {
		this.name = name;
	}

	public UserRepositories(String name, String website, String description,
		boolean repoPrivate, boolean repoAsTemplate, boolean repoEnableIssues,
		boolean repoEnableWiki, boolean repoEnablePr,
		boolean repoEnableMerge, boolean repoEnableRebase, boolean repoEnableSquash, boolean repoEnableForceMerge) {

		this.name = name;
		this.website = website;
		this.description = description;
		this.privateFlag = repoPrivate;
		this.template = repoAsTemplate;
		this.has_issues = repoEnableIssues;
		this.has_wiki = repoEnableWiki;
		this.has_pull_requests = repoEnablePr;
		this.allow_merge_commits = repoEnableMerge;
		this.allow_rebase = repoEnableRebase;
		this.allow_squash_merge = repoEnableSquash;
		this.allow_rebase_explicit = repoEnableForceMerge;
	}

	public UserRepositories(String name, String website, String description,
		boolean repoPrivate, boolean repoAsTemplate, boolean repoEnableIssues,
		boolean repoEnableWiki, boolean repoEnablePr, internalTimeTrackerObject repoEnableTimer,
		boolean repoEnableMerge, boolean repoEnableRebase, boolean repoEnableSquash, boolean repoEnableForceMerge) {

		this.name = name;
		this.website = website;
		this.description = description;
		this.privateFlag = repoPrivate;
		this.template = repoAsTemplate;
		this.has_issues = repoEnableIssues;
		this.has_wiki = repoEnableWiki;
		this.has_pull_requests = repoEnablePr;
		this.internal_tracker = repoEnableTimer;
		this.allow_merge_commits = repoEnableMerge;
		this.allow_rebase = repoEnableRebase;
		this.allow_squash_merge = repoEnableSquash;
		this.allow_rebase_explicit = repoEnableForceMerge;
	}

	public static class internalTimeTrackerObject {

		private boolean allow_only_contributors_to_track_time;
		private boolean enable_issue_dependencies;
		private boolean enable_time_tracker;

		public internalTimeTrackerObject(boolean enable_time_tracker) {

			this.enable_time_tracker = enable_time_tracker;
		}

		public boolean isAllow_only_contributors_to_track_time() {

			return allow_only_contributors_to_track_time;
		}

		public boolean isEnable_issue_dependencies() {

			return enable_issue_dependencies;
		}

		public boolean isEnable_time_tracker() {

			return enable_time_tracker;
		}

	}

	public static class externalWikiObject {

		private String external_wiki_url;

		public externalWikiObject(String external_wiki_url) {

			this.external_wiki_url = external_wiki_url;
		}

		public String getExternal_wiki_url() {

			return external_wiki_url;
		}

	}

	public static class externalIssueTrackerObject {

		private String external_tracker_format;
		private String external_tracker_style;
		private String external_tracker_url;

		public externalIssueTrackerObject(String external_tracker_url) {

			this.external_tracker_url = external_tracker_url;
		}

		public String getExternal_tracker_format() {

			return external_tracker_format;
		}

		public String getExternal_tracker_style() {

			return external_tracker_style;
		}

		public String getExternal_tracker_url() {

			return external_tracker_url;
		}

	}

	public static class permissionsObject {

		private boolean admin;
		private boolean push;
		private boolean pull;

		public boolean isAdmin() {

			return admin;
		}

		public boolean canPush() {

			return push;
		}

		public boolean canPull() {

			return pull;
		}

	}

	public int getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public String getFullName() {

		return full_name;
	}

	public String getDescription() {

		return description;
	}

	public permissionsObject getPermissions() {

		return permissions;
	}

	public Boolean getPrivateFlag() {

		return privateFlag;
	}

	public String getStars_count() {

		return stars_count;
	}

	public String getOpen_pull_count() {

		return open_pr_counter;
	}

	public String getRelease_count() {

		return release_counter;
	}

	public String getWatchers_count() {

		return watchers_count;
	}

	public String getOpen_issues_count() {

		return open_issues_count;
	}

	public String getHtml_url() {

		return html_url;
	}

	public String getDefault_branch() {

		return default_branch;
	}

	public Date getCreated_at() {

		return created_at;
	}

	public Date getUpdated_at() {

		return updated_at;
	}

	public String getClone_url() {

		return clone_url;
	}

	public long getSize() {

		return size;
	}

	public String getSsh_url() {

		return ssh_url;
	}

	public String getWebsite() {

		return website;
	}

	public String getForks_count() {

		return forks_count;
	}

	public Boolean getHas_issues() {

		return has_issues;
	}

	public String getAvatar_url() {

		return avatar_url;
	}

	public boolean isPrivateFlag() {

		return privateFlag;
	}

	public String getOpen_pr_counter() {

		return open_pr_counter;
	}

	public String getRelease_counter() {

		return release_counter;
	}

	public boolean isArchived() {

		return archived;
	}

	public String getFull_name() {

		return full_name;
	}

	public boolean isAllow_merge_commits() {

		return allow_merge_commits;
	}

	public boolean isAllow_rebase() {

		return allow_rebase;
	}

	public boolean isAllow_rebase_explicit() {

		return allow_rebase_explicit;
	}

	public boolean isAllow_squash_merge() {

		return allow_squash_merge;
	}

	public boolean isHas_pull_requests() {

		return has_pull_requests;
	}

	public boolean isHas_wiki() {

		return has_wiki;
	}

	public boolean isIgnore_whitespace_conflicts() {

		return ignore_whitespace_conflicts;
	}

	public boolean isTemplate() {

		return template;
	}

	public externalIssueTrackerObject getExternal_tracker() {

		return external_tracker;
	}

	public externalWikiObject getExternal_wiki() {

		return external_wiki;
	}

	public internalTimeTrackerObject getInternal_tracker() {

		return internal_tracker;
	}

}

package org.mian.gitnex.api.models.contents;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author mmarif
 */
public class RepoGetContentsList {

	@SerializedName("last_commit_when")
	private String lastCommitWhen;

	@SerializedName("last_committer_date")
	private Date lastCommitterDate;

	@SerializedName("last_author_date")
	private Date lastAuthorDate;

	@SerializedName("submodule_git_url")
	private String submoduleGitUrl;

	@SerializedName("_links")
	private Links links;

	@SerializedName("last_commit_sha")
	private String lastCommitSha;

	@SerializedName("type")
	private String type;

	@SerializedName("encoding")
	private String encoding;

	@SerializedName("sha")
	private String sha;

	@SerializedName("content")
	private String content;

	@SerializedName("url")
	private String url;

	@SerializedName("target")
	private String target;

	@SerializedName("path")
	private String path;

	@SerializedName("size")
	private int size;

	@SerializedName("html_url")
	private String htmlUrl;

	@SerializedName("name")
	private String name;

	@SerializedName("download_url")
	private String downloadUrl;

	@SerializedName("git_url")
	private String gitUrl;

	public String getLastCommitWhen() {
		return lastCommitWhen;
	}

	public Date getLastCommiterDate() {
		return lastCommitterDate;
	}

	public Date getLastAuthorDate() {
		return lastAuthorDate;
	}

	public String getSubmoduleGitUrl() {
		return submoduleGitUrl;
	}

	public Links getLinks() {
		return links;
	}

	public String getLastCommitSha() {
		return lastCommitSha;
	}

	public String getType() {
		return type;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getSha() {
		return sha;
	}

	public String getContent() {
		return content;
	}

	public String getUrl() {
		return url;
	}

	public String getTarget() {
		return target;
	}

	public String getPath() {
		return path;
	}

	public int getSize() {
		return size;
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public String getName() {
		return name;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getGitUrl() {
		return gitUrl;
	}

	public Date getCompatibleCommitDate() {
		// First try Gitea field
		if (lastCommitterDate != null) {
			return lastCommitterDate;
		}

		// Then try Forgejo field
		if (lastCommitWhen != null) {
			return parseForgejoDateString(lastCommitWhen);
		}

		return null;
	}

	private Date parseForgejoDateString(String dateString) {
		if (dateString == null) return null;

		String[] dateFormats = {
			"yyyy-MM-dd'T'HH:mm:ssXXX",
			"yyyy-MM-dd'T'HH:mm:ss'Z'",
			"yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
		};

		for (String format : dateFormats) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
				dateFormat.setLenient(false);
				return dateFormat.parse(dateString);
			} catch (ParseException e) {
				// Try next format
			}
		}
		return null;
	}
}

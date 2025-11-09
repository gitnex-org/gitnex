package org.mian.gitnex.api.models.contents;

import com.google.gson.annotations.SerializedName;

/**
 * @author mmarif
 */
public class Links {

	@SerializedName("git")
	private String git;

	@SerializedName("self")
	private String self;

	@SerializedName("html")
	private String html;

	public String getGit() {
		return git;
	}

	public String getSelf() {
		return self;
	}

	public String getHtml() {
		return html;
	}
}

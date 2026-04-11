package org.mian.gitnex.api.models.contents;

import com.google.gson.annotations.SerializedName;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Links implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

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

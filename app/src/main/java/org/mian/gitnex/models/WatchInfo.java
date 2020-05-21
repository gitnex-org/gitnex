package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class WatchInfo {

	private Boolean subscribed;
	private Boolean ignored; // = !subscribed
	private String reason;   // not used by gitea jet
	private String created_at;
	private String url;
	private String repository_url;

	public Boolean getSubscribed() {

		return subscribed;

	}

	public String getCreated_at() {

		return created_at;

	}

	public String getUrl() {

		return url;

	}

	public String getRepository_url() {

		return repository_url;

	}

}

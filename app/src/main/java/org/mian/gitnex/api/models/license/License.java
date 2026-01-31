package org.mian.gitnex.api.models.license;

import com.google.gson.annotations.SerializedName;

/**
 * @author mmarif
 */
public class License {

	@SerializedName("key")
	private String key;

	@SerializedName("name")
	private String name;

	@SerializedName("url")
	private String url;

	public License() {}

	public License(String key, String name, String url) {
		this.key = key;
		this.name = name;
		this.url = url;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}

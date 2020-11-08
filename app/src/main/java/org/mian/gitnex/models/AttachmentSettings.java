package org.mian.gitnex.models;

/**
 * Author opyale
 */

public class AttachmentSettings {

	private String allowed_types;
	private boolean enabled;
	private float max_files;
	private float max_size;

	public String getAllowed_types() {

		return allowed_types;
	}

	public boolean isEnabled() {

		return enabled;
	}

	public float getMax_files() {

		return max_files;
	}

	public float getMax_size() {

		return max_size;
	}

}

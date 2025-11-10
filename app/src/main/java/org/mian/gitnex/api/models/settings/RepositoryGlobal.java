package org.mian.gitnex.api.models.settings;

import com.google.gson.annotations.SerializedName;

/**
 * @author mmarif
 */
public class RepositoryGlobal {

	@SerializedName("forks_disabled")
	private boolean forksDisabled;

	@SerializedName("migrations_disabled")
	private boolean migrationsDisabled;

	@SerializedName("http_git_disabled")
	private boolean httpGitDisabled;

	@SerializedName("lfs_disabled")
	private boolean lfsDisabled;

	@SerializedName("mirrors_disabled")
	private boolean mirrorsDisabled;

	@SerializedName("time_tracking_disabled")
	private boolean timeTrackingDisabled;

	@SerializedName("stars_disabled")
	private boolean starsDisabled;

	public boolean isForksDisabled() {
		return forksDisabled;
	}

	public boolean isMigrationsDisabled() {
		return migrationsDisabled;
	}

	public boolean isHttpGitDisabled() {
		return httpGitDisabled;
	}

	public boolean isLfsDisabled() {
		return lfsDisabled;
	}

	public boolean isMirrorsDisabled() {
		return mirrorsDisabled;
	}

	public boolean isTimeTrackingDisabled() {
		return timeTrackingDisabled;
	}

	public boolean isStarsDisabled() {
		return starsDisabled;
	}
}

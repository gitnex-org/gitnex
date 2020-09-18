package org.mian.gitnex.models;

import com.google.gson.annotations.SerializedName;

/**
 * Author M M Arif
 */

public class NotificationCount {

	@SerializedName("new")
	private int counter;

	public int getCounter() {

		return counter;
	}

}

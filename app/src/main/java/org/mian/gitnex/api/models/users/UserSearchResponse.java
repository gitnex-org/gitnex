package org.mian.gitnex.api.models.users;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class UserSearchResponse implements Serializable {

	@SerializedName("data")
	private List<User> data;

	@SerializedName("ok")
	private boolean ok;

	public List<User> getData() {
		return data;
	}

	public void setData(List<User> data) {
		this.data = data;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}
}

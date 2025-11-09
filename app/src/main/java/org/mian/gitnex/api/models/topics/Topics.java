package org.mian.gitnex.api.models.topics;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author mmarif
 */
public class Topics {

	@SerializedName("topics")
	private List<String> topics;

	public List<String> getTopics() {
		return topics;
	}
}

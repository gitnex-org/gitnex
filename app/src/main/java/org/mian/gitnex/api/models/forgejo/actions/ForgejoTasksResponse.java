package org.mian.gitnex.api.models.forgejo.actions;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class ForgejoTasksResponse implements Serializable {

	@SerializedName("total_count")
	private long totalCount;

	@SerializedName("workflow_runs")
	private List<ForgejoTask> workflowRuns;

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public List<ForgejoTask> getWorkflowRuns() {
		return workflowRuns;
	}

	public void setWorkflowRuns(List<ForgejoTask> workflowRuns) {
		this.workflowRuns = workflowRuns;
	}
}

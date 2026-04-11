package org.mian.gitnex.models;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class IssueFilterState {

	public String query = "";
	public String state = "open";
	public List<String> selectedLabels = new ArrayList<>();
	public String milestoneTitle = null;
	public String mentionedBy = null;

	public void reset() {
		query = "";
		state = "open";
		selectedLabels.clear();
		milestoneTitle = null;
		mentionedBy = null;
	}
}

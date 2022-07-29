package org.mian.gitnex.helpers;

import java.io.Serializable;

/**
 * @author M M Arif
 */

public class MergePullRequestSpinner implements Serializable {

	private String id;
	private String mergerMethod;

	public MergePullRequestSpinner(String id, String mergerMethod) {
		this.id = id;
		this.mergerMethod = mergerMethod;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private String getMergerMethod() {
		return mergerMethod;
	}

	public void setName(String mergerMethod) {
		this.mergerMethod = mergerMethod;
	}

	@Override
	public String toString() {
		return mergerMethod;
	}

	@Override
	public boolean equals(Object obj) {

		if(obj instanceof MergePullRequestSpinner) {

			MergePullRequestSpinner spinner = (MergePullRequestSpinner )obj;
			return spinner.getMergerMethod().equals(mergerMethod) && spinner.getId().equals(id);

		}

		return false;
	}

}

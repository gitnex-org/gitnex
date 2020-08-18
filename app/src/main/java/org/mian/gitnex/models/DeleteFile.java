package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class DeleteFile {

	private String branch;
	private String message;
	private String new_branch;
	private String sha;

	public String getBranch() {

		return branch;
	}

	public void setBranch(String branch) {

		this.branch = branch;
	}

	public String getMessage() {

		return message;
	}

	public void setMessage(String message) {

		this.message = message;
	}

	public String getNew_branch() {

		return new_branch;
	}

	public void setNew_branch(String new_branch) {

		this.new_branch = new_branch;
	}

	public String getSha() {

		return sha;
	}

	public void setSha(String sha) {

		this.sha = sha;
	}

	public DeleteFile(String branch, String message, String new_branch, String sha) {
		this.branch = branch;
		this.message = message;
		this.new_branch = new_branch;
		this.sha = sha;
	}
}

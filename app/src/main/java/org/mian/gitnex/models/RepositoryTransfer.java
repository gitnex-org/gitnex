package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class RepositoryTransfer {

	private String new_owner;

	public RepositoryTransfer(String new_owner) {

		this.new_owner = new_owner;
	}

	public String getNew_owner() {

		return new_owner;
	}
}

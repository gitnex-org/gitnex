package org.mian.gitnex.models;

/**
 * Author opyale
 */

public class APISettings {

	private int default_git_trees_per_page;
	private int default_max_blob_size;
	private int default_paging_num;
	private int max_response_items;

	public int getDefault_git_trees_per_page() {

		return default_git_trees_per_page;
	}

	public int getDefault_max_blob_size() {

		return default_max_blob_size;
	}

	public int getDefault_paging_num() {

		return default_paging_num;
	}

	public int getMax_response_items() {

		return max_response_items;
	}

}

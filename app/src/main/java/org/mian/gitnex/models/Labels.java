package org.mian.gitnex.models;

import java.util.List;

/**
 * Author M M Arif
 */

public class Labels {

    private int id;
    private String name;
    private String color;
    private String url;
    private List<Integer> labels;

    public Labels(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Labels(List<Integer> labels) {
        this.labels = labels;
    }

	public Labels(int id, String name, String color) {
		this.id = id;
		this.name = name;
		this.color = color;
	}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getUrl() {
        return url;
    }
}

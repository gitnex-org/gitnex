package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class Labels {

    private int id;
    private String name;
    private String color;
    private String url;
    private int[] labels;

    public Labels(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Labels(int[] labels) {
        this.labels = labels;
    }

	public Labels(int id, String name) {
		this.id = id;
		this.name = name;
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

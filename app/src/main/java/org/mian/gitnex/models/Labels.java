package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class Labels {

    private int id;
    private String name;
    private String color;
    private String url;

    public Labels(String name, String color) {
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

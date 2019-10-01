package org.mian.gitnex.models;

import java.util.List;

/**
 * Author M M Arif
 */

public class Teams {

    private int id;
    private String name;
    private String description;
    private String permission;
    private List<String> units;

    public Teams(String name, String description, String permission, List<String> units) {
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.units = units;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }
}

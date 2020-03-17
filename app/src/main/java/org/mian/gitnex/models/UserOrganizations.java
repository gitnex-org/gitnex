package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class UserOrganizations {

    private int id;
    private String username;
    private String avatar_url;
    private String description;
    private String website;
    private String location;

    public UserOrganizations(String username, String avatar_url, String description, String website, String location) {
        this.username = username;
        this.avatar_url = avatar_url;
        this.description = description;
        this.website = website;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public String getLocation() {
        return location;
    }
}

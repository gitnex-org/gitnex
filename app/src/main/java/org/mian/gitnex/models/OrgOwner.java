package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class OrgOwner {

    private int id;
    private String username;

    public OrgOwner(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return username;
    }
}

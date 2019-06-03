package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class OrgOwner {

    private String username;

    public OrgOwner(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }
}

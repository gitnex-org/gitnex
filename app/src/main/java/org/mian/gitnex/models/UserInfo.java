package org.mian.gitnex.models;

import androidx.annotation.NonNull;

/**
 * Author M M Arif
 */

public class UserInfo {

    private int id;
    private String login;
    private String full_name;
    private String email;
    private String avatar_url;
    private String language;
    private String username;

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getFullname() {
        return full_name;
    }

    public String getEmail() {
        return email;
    }

    @NonNull
    public String getAvatar() {
        return avatar_url;
    }

    public String getLang() {
        return language;
    }

    public String getUsername() {
        return username;
    }

}

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

    private String login_name;
    private String password;
    private Boolean send_notify;
    private int source_id;

    public UserInfo(String email, String full_name, String login_name, String password, String username, int source_id, Boolean send_notify) {
        this.email = email;
        this.full_name = full_name;
        this.login_name = login_name;
        this.password = password;
        this.username = username;
        this.source_id = source_id;
        this.send_notify = send_notify;
    }

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

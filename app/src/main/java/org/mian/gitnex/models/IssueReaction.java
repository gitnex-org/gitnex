package org.mian.gitnex.models;

import java.util.Date;

/**
 * Author 6543
 */

public class IssueReaction {

    private String content;
    private userObject user;
    private Date created_at;

    public IssueReaction(String content) {
        this.content = content;
    }

    public static class userObject {

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

        public String getFull_name() {
            return full_name;
        }

        public String getEmail() {
            return email;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public String getLanguage() {
            return language;
        }

        public String getUsername() {
            return username;
        }

    }

    public String getContent() {
        return content;
    }

    public userObject getUser() {
        return user;
    }

    public Date getCreated_at() {
        return created_at;
    }

}

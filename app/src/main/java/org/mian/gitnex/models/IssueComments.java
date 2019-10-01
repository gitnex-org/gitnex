package org.mian.gitnex.models;

import java.util.Date;

/**
 * Author M M Arif
 */

public class IssueComments {

    private int id;
    private String html_url;
    private String pull_request_url;
    private String issue_url;
    private String body;
    private Date created_at;
    private Date created_date;
    private Date updated_at;

    private userObject user;

    public IssueComments(String body) {
        this.body = body;
    }

    public class userObject {

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

    public int getId() {
        return id;
    }

    public String getHtml_url() {
        return html_url;
    }

    public String getPull_request_url() {
        return pull_request_url;
    }

    public String getIssue_url() {
        return issue_url;
    }

    public String getBody() {
        return body;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public userObject getUser() {
        return user;
    }

    public Date getUpdated_at() {
        return updated_at;
    }
}

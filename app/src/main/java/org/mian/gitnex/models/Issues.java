package org.mian.gitnex.models;

import java.util.Date;
import java.util.List;

/**
 * Author M M Arif
 */

public class Issues {

    private int id;
    private String url;
    private String html_url;
    private int number;
    private String title;
    private String body;
    private String state;
    private int comments;
    private Date created_at;
    private Date updated_at;
    private Date due_date;
    private Date closed_at;

    private userObject user;
    private List<labelsObject> labels;
    private pullRequestObject pull_request;
    private milestoneObject milestone;
    private List<assigneesObject> assignees;

    public Issues(String body) {
        this.body = body;
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

    public static class labelsObject {

        private int id;
        private String name;
        private String color;
        private String url;

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

    public static class pullRequestObject {

        private boolean merged;
        private String merged_at;

        public boolean isMerged() {
            return merged;
        }

        public String getMerged_at() {
            return merged_at;
        }
    }

    public static class milestoneObject {

        private int id;
        private String title;
        private String description;
        private String state;
        private String open_issues;
        private String closed_issues;
        private String closed_at;
        private String due_on;

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getState() {
            return state;
        }

        public String getOpen_issues() {
            return open_issues;
        }

        public String getClosed_issues() {
            return closed_issues;
        }

        public String getClosed_at() {
            return closed_at;
        }

        public String getDue_on() {
            return due_on;
        }
    }

    public static class assigneesObject {

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

    public String getUrl() {
        return url;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getState() {
        return state;
    }

    public int getComments() {
        return comments;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public Date getDue_date() {
        return due_date;
    }

    public Date getClosed_at() {
        return closed_at;
    }

    public userObject getUser() {
        return user;
    }

    public List<labelsObject> getLabels() {
        return labels;
    }

    public pullRequestObject getPull_request() {
        return pull_request;
    }

    public milestoneObject getMilestone() {
        return milestone;
    }

    public List<assigneesObject> getAssignees() {
        return assignees;
    }

    public String getHtml_url() {

        return html_url;
    }

    public void setHtml_url(String html_url) {

        this.html_url = html_url;
    }

}

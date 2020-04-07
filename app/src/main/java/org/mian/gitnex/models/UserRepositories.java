package org.mian.gitnex.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * Author M M Arif
 */

public class UserRepositories {

    private int id;
    private String name;
    private String full_name;
    private String description;
    @SerializedName("private")
    private boolean privateFlag;
    private String stars_count;
    private String watchers_count;
    private String open_issues_count;
    private String open_pr_counter;
    private String release_counter;
    private String html_url;
    private String default_branch;
    private Date created_at;
    private Date updated_at;
    private String clone_url;
    private long size;
    private String ssh_url;
    private String website;
    private String forks_count;
    private Boolean has_issues;
    private String avatar_url;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFullname() {
        return full_name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getPrivateFlag() {
        return privateFlag;
    }

    public String getStars_count() {
        return stars_count;
    }

    public String getOpen_pull_count() {
        return open_pr_counter;
    }

    public String getRelease_count() {
        return release_counter;
    }

    public String getWatchers_count() {
        return watchers_count;
    }

    public String getOpen_issues_count() {
        return open_issues_count;
    }

    public String getHtml_url() {
        return html_url;
    }

    public String getDefault_branch() {
        return default_branch;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public String getClone_url() {
        return clone_url;
    }

    public long getSize() {
        return size;
    }

    public String getSsh_url() {
        return ssh_url;
    }

    public String getWebsite() {
        return website;
    }

    public String getForks_count() {
        return forks_count;
    }

    public Boolean getHas_issues() {
        return has_issues;
    }

    public String getAvatar_url() {
        return avatar_url;
    }
}

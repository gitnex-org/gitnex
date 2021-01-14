package org.mian.gitnex.models;

import java.util.Date;

/**
 * Author opyale
 */

public class Status {

    private String context;
    private Date created_at;
    private UserInfo creator;
    private String description;
    private int id;
    private String status;
    private String target_url;
    private Date updated_at;
    private String url;

    public String getContext() {
        return context;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getTarget_url() {
        return target_url;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public String getUrl() {
        return url;
    }
}

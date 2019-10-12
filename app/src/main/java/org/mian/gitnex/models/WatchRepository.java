package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class WatchRepository {

    private Boolean subscribed;
    private Boolean ignored;
    private String reason;
    private String created_at;
    private String url;
    private String repository_url;

    public Boolean getSubscribed() {
        return subscribed;
    }

    public Boolean getIgnored() {
        return ignored;
    }

    public String getReason() {
        return reason;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUrl() {
        return url;
    }

    public String getRepository_url() {
        return repository_url;
    }
}

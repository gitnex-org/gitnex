package org.mian.gitnex.models;

/**
 * Author opyale
 */

public class CreateStatusOption {

    private String context;
    private String description;
    private String statusState;
    private String target_url;

    public CreateStatusOption(String context, String description, String statusState, String target_url) {
        this.context = context;
        this.description = description;
        this.statusState = statusState;
        this.target_url = target_url;
    }

    public String getContext() {
        return context;
    }

    public String getDescription() {
        return description;
    }

    public String getStatusState() {
        return statusState;
    }

    public String getTarget_url() {
        return target_url;
    }
}

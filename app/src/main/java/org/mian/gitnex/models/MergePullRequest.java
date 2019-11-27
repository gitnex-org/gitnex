package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class MergePullRequest {

    private String Do;
    private String MergeMessageField;
    private String MergeTitleField;

    public MergePullRequest(String Do, String MergeMessageField, String MergeTitleField) {
        this.Do = Do;
        this.MergeMessageField = MergeMessageField;
        this.MergeTitleField = MergeTitleField;
    }

}

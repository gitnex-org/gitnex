package org.mian.gitnex.models;

import java.util.List;

/**
 * Author M M Arif
 */

public class UpdateIssueAssignee {

    private List<String> assignees;

    public UpdateIssueAssignee(List<String> assignees) {
        this.assignees = assignees;
    }

}

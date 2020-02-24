package org.mian.gitnex.models;

import java.util.List;

/**
 * Author M M Arif
 */

public class CreateIssue {

    private String body;
    private boolean closed;
    private String due_date;
    private int milestone;
    private String title;

    private List<String> assignees;
    private int[] labels;

    public CreateIssue(String assignee, String body, boolean closed, String due_date, int milestone, String title, List<String> assignees, int[] labels) {
        this.body = body;
        this.closed = closed;
        this.due_date = due_date;
        this.milestone = milestone;
        this.title = title;
        this.assignees = assignees;
        this.labels = labels;
    }

    public CreateIssue(String title, String body, String due_date, int milestone) {
        this.title = title;
        this.body = body;
        this.due_date = due_date;
        this.milestone = milestone;
    }

    private class Assignees {
    }

    private class Labels {
    }
}

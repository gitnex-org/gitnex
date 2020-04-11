package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class Milestones {

    private int id;
    private String title;
    private String description;
    private String state;
    private int open_issues;
    private int closed_issues;
    private String due_on;

    public Milestones(String description, String title, String due_on) {
        this.description = description;
        this.title = title;
        this.due_on = due_on;
    }

    public Milestones(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public Milestones(String state) {
        this.state = state;
    }

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

    public int getOpen_issues() {
        return open_issues;
    }

    public int getClosed_issues() {
        return closed_issues;
    }

    public String getDue_on() {
        return due_on;
    }

    @Override
    public String toString() {
        return title;
    }

}

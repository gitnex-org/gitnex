package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class Files {

    private String name;
    private String path;
    private String sha;
    private String type;
    private int size;
    private String encoding;
    private String content;
    private String target;
    private String url;
    private String html_url;
    private String git_url;
    private String download_url;
    private String submodule_git_url;

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getSha() {
        return sha;
    }

    public String getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getContent() {
        return content;
    }

    public String getTarget() {
        return target;
    }

    public String getUrl() {
        return url;
    }

    public String getHtml_url() {
        return html_url;
    }

    public String getGit_url() {
        return git_url;
    }

    public String getDownload_url() {
        return download_url;
    }

    public String getSubmodule_git_url() {
        return submodule_git_url;
    }
}

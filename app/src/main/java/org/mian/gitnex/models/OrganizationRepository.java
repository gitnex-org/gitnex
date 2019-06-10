package org.mian.gitnex.models;

import com.google.gson.annotations.SerializedName;

/**
 * Author M M Arif
 */

public class OrganizationRepository {

    private boolean auto_init;
    private String description;
    private String gitignores;
    private String license;
    private String name;
    private String readme;

    @SerializedName("private")
    private boolean is_private;

    public OrganizationRepository(boolean auto_init, String description, String gitignores, String license, String name, boolean is_private, String readme) {
        this.auto_init = auto_init;
        this.description = description;
        this.gitignores = gitignores;
        this.license = license;
        this.name = name;
        this.is_private = is_private;
        this.readme = readme;
    }

    public void setAuto_init(boolean auto_init) {
        this.auto_init = auto_init;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGitignores(String gitignores) {
        this.gitignores = gitignores;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIs_private(boolean is_private) {
        this.is_private = is_private;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }

    public boolean isAuto_init() {
        return auto_init;
    }

    public String getDescription() {
        return description;
    }

    public String getGitignores() {
        return gitignores;
    }

    public String getLicense() {
        return license;
    }

    public String getName() {
        return name;
    }

    public boolean isIs_private() {
        return is_private;
    }

    public String getReadme() {
        return readme;
    }

}

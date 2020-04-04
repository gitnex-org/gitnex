package org.mian.gitnex.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

/**
 * Author M M Arif
 */

public class PullRequests {

    private int id;
    private String body;
    private int comments;
    private String diff_url;
    private String html_url;
    private String merge_base;
    private String merge_commit_sha;
    private boolean mergeable;
    private boolean merged;
    private int number;
    private String patch_url;
    private String state;
    private String title;
    private String url;
    private Date closed_at;
    private Date created_at;
    private Date due_date;
    private Date merged_at;
    private Date updated_at;

    private userObject user;
    private List<labelsObject> labels;
    private List<assigneesObject> assignees;
    private mergedByObject merged_by;
    private milestoneObject milestone;
    private baseObject base;
    private headObject head;

    public PullRequests(String body) {
        this.body = body;
    }

    public class headObject {

        private int repo_id;
        private String label;
        private String ref;
        private String sha;

        private repoObject repo;

        public class repoObject {

            private int repo_id;
            private boolean allow_merge_commits;
            private boolean allow_rebase;
            private boolean allow_rebase_explicit;
            private boolean allow_squash_merge;
            private boolean archived;
            private boolean empty;
            private boolean fork;
            private boolean has_issues;
            private boolean has_pull_requests;
            private boolean has_wiki;
            private boolean ignore_whitespace_conflicts;
            @SerializedName("private")
            private boolean privateFlag;
            private boolean mirror;
            private String avatar_url;
            private String clone_url;
            private String default_branch;
            private String description;
            private String full_name;
            private String html_url;
            private String name;
            private String ssh_url;
            private String website;
            private int forks_count;
            private int id;
            private int open_issues_count;
            private int open_pr_counter;
            private int release_counter;
            private int size;
            private int stars_count;
            private int watchers_count;
            private Date created_at;
            private Date updated_at;

            private ownerObject owner;
            private permissionsObject permissions;

            public class ownerObject {

                private int repo_id;
                private boolean is_admin;
                private String avatar_url;
                private String email;
                private String full_name;
                private String language;
                private String login;
                private Date created;

                public int getRepo_id() {
                    return repo_id;
                }

                public boolean isIs_admin() {
                    return is_admin;
                }

                public String getAvatar_url() {
                    return avatar_url;
                }

                public String getEmail() {
                    return email;
                }

                public String getFull_name() {
                    return full_name;
                }

                public String getLanguage() {
                    return language;
                }

                public String getLogin() {
                    return login;
                }

                public Date getCreated() {
                    return created;
                }
            }

            public class permissionsObject {

                private boolean admin;
                private boolean pull;
                private boolean push;

                public boolean isAdmin() {
                    return admin;
                }

                public boolean isPull() {
                    return pull;
                }

                public boolean isPush() {
                    return push;
                }
            }

            public int getRepo_id() {
                return repo_id;
            }

            public boolean isAllow_merge_commits() {
                return allow_merge_commits;
            }

            public boolean isAllow_rebase() {
                return allow_rebase;
            }

            public boolean isAllow_rebase_explicit() {
                return allow_rebase_explicit;
            }

            public boolean isAllow_squash_merge() {
                return allow_squash_merge;
            }

            public boolean isArchived() {
                return archived;
            }

            public boolean isEmpty() {
                return empty;
            }

            public boolean isFork() {
                return fork;
            }

            public boolean isHas_issues() {
                return has_issues;
            }

            public boolean isHas_pull_requests() {
                return has_pull_requests;
            }

            public boolean isHas_wiki() {
                return has_wiki;
            }

            public boolean isIgnore_whitespace_conflicts() {
                return ignore_whitespace_conflicts;
            }

            public boolean isPrivateFlag() {
                return privateFlag;
            }

            public boolean isMirror() {
                return mirror;
            }

            public String getAvatar_url() {
                return avatar_url;
            }

            public String getClone_url() {
                return clone_url;
            }

            public String getDefault_branch() {
                return default_branch;
            }

            public String getDescription() {
                return description;
            }

            public String getFull_name() {
                return full_name;
            }

            public String getHtml_url() {
                return html_url;
            }

            public String getName() {
                return name;
            }

            public String getSsh_url() {
                return ssh_url;
            }

            public String getWebsite() {
                return website;
            }

            public int getForks_count() {
                return forks_count;
            }

            public int getId() {
                return id;
            }

            public int getOpen_issues_count() {
                return open_issues_count;
            }

            public int getOpen_pull_count() {
                return open_pr_counter;
            }

            public int getRelease_count() {
                return release_counter;
            }

            public int getSize() {
                return size;
            }

            public int getStars_count() {
                return stars_count;
            }

            public int getWatchers_count() {
                return watchers_count;
            }

            public Date getCreated_at() {
                return created_at;
            }

            public Date getUpdated_at() {
                return updated_at;
            }

            public ownerObject getOwner() {
                return owner;
            }

            public permissionsObject getPermissions() {
                return permissions;
            }
        }

    }

    public class baseObject {

        private int repo_id;
        private String label;
        private String ref;
        private String sha;

        private repoObject repo;

        public class repoObject {

            private int repo_id;
            private boolean allow_merge_commits;
            private boolean allow_rebase;
            private boolean allow_rebase_explicit;
            private boolean allow_squash_merge;
            private boolean archived;
            private boolean empty;
            private boolean fork;
            private boolean has_issues;
            private boolean has_pull_requests;
            private boolean has_wiki;
            private boolean ignore_whitespace_conflicts;
            @SerializedName("private")
            private boolean privateFlag;
            private boolean mirror;
            private String avatar_url;
            private String clone_url;
            private String default_branch;
            private String description;
            private String full_name;
            private String html_url;
            private String name;
            private String ssh_url;
            private String website;
            private int forks_count;
            private int id;
            private int open_issues_count;
            private int size;
            private int stars_count;
            private int watchers_count;
            private Date created_at;
            private Date updated_at;

            private ownerObject owner;
            private permissionsObject permissions;

            public class ownerObject {

                private int repo_id;
                private boolean is_admin;
                private String avatar_url;
                private String email;
                private String full_name;
                private String language;
                private String login;
                private Date created;

                public int getRepo_id() {
                    return repo_id;
                }

                public boolean isIs_admin() {
                    return is_admin;
                }

                public String getAvatar_url() {
                    return avatar_url;
                }

                public String getEmail() {
                    return email;
                }

                public String getFull_name() {
                    return full_name;
                }

                public String getLanguage() {
                    return language;
                }

                public String getLogin() {
                    return login;
                }

                public Date getCreated() {
                    return created;
                }
            }

            public class permissionsObject {

                private boolean admin;
                private boolean pull;
                private boolean push;

                public boolean isAdmin() {
                    return admin;
                }

                public boolean isPull() {
                    return pull;
                }

                public boolean isPush() {
                    return push;
                }
            }

            public int getRepo_id() {
                return repo_id;
            }

            public boolean isAllow_merge_commits() {
                return allow_merge_commits;
            }

            public boolean isAllow_rebase() {
                return allow_rebase;
            }

            public boolean isAllow_rebase_explicit() {
                return allow_rebase_explicit;
            }

            public boolean isAllow_squash_merge() {
                return allow_squash_merge;
            }

            public boolean isArchived() {
                return archived;
            }

            public boolean isEmpty() {
                return empty;
            }

            public boolean isFork() {
                return fork;
            }

            public boolean isHas_issues() {
                return has_issues;
            }

            public boolean isHas_pull_requests() {
                return has_pull_requests;
            }

            public boolean isHas_wiki() {
                return has_wiki;
            }

            public boolean isIgnore_whitespace_conflicts() {
                return ignore_whitespace_conflicts;
            }

            public boolean isPrivateFlag() {
                return privateFlag;
            }

            public boolean isMirror() {
                return mirror;
            }

            public String getAvatar_url() {
                return avatar_url;
            }

            public String getClone_url() {
                return clone_url;
            }

            public String getDefault_branch() {
                return default_branch;
            }

            public String getDescription() {
                return description;
            }

            public String getFull_name() {
                return full_name;
            }

            public String getHtml_url() {
                return html_url;
            }

            public String getName() {
                return name;
            }

            public String getSsh_url() {
                return ssh_url;
            }

            public String getWebsite() {
                return website;
            }

            public int getForks_count() {
                return forks_count;
            }

            public int getId() {
                return id;
            }

            public int getOpen_issues_count() {
                return open_issues_count;
            }

            public int getSize() {
                return size;
            }

            public int getStars_count() {
                return stars_count;
            }

            public int getWatchers_count() {
                return watchers_count;
            }

            public Date getCreated_at() {
                return created_at;
            }

            public Date getUpdated_at() {
                return updated_at;
            }

            public ownerObject getOwner() {
                return owner;
            }

            public permissionsObject getPermissions() {
                return permissions;
            }
        }

    }

    public class userObject {

        private int id;
        private String login;
        private String full_name;
        private String email;
        private String avatar_url;
        private String language;
        private boolean is_admin;

        public int getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }

        public String getFull_name() {
            return full_name;
        }

        public String getEmail() {
            return email;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public String getLanguage() {
            return language;
        }

        public boolean isIs_admin() {
            return is_admin;
        }
    }

    public class labelsObject {

        private int id;
        private String name;
        private String color;
        private String url;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }

        public String getUrl() {
            return url;
        }
    }

    public class assigneesObject {

        private int id;
        private String login;
        private String full_name;
        private String email;
        private String avatar_url;
        private String language;
        private boolean is_admin;

        public int getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }

        public String getFull_name() {
            return full_name;
        }

        public String getEmail() {
            return email;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public String getLanguage() {
            return language;
        }

        public boolean isIs_admin() {
            return is_admin;
        }
    }

    public class mergedByObject {

        private int id;
        private String login;
        private String full_name;
        private String email;
        private String avatar_url;
        private String language;
        private boolean is_admin;

        public int getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }

        public String getFull_name() {
            return full_name;
        }

        public String getEmail() {
            return email;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public String getLanguage() {
            return language;
        }

        public boolean isIs_admin() {
            return is_admin;
        }
    }

    public class milestoneObject {

        private int id;
        private String title;
        private String description;
        private String state;
        private String open_issues;
        private String closed_issues;
        private String closed_at;
        private String due_on;

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

        public String getOpen_issues() {
            return open_issues;
        }

        public String getClosed_issues() {
            return closed_issues;
        }

        public String getClosed_at() {
            return closed_at;
        }

        public String getDue_on() {
            return due_on;
        }
    }

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public int getComments() {
        return comments;
    }

    public String getDiff_url() {
        return diff_url;
    }

    public String getHtml_url() {
        return html_url;
    }

    public String getMerge_base() {
        return merge_base;
    }

    public String getMerge_commit_sha() {
        return merge_commit_sha;
    }

    public boolean isMergeable() {
        return mergeable;
    }

    public boolean isMerged() {
        return merged;
    }

    public int getNumber() {
        return number;
    }

    public String getPatch_url() {
        return patch_url;
    }

    public String getState() {
        return state;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Date getClosed_at() {
        return closed_at;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public Date getDue_date() {
        return due_date;
    }

    public Date getMerged_at() {
        return merged_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public userObject getUser() {
        return user;
    }

    public List<labelsObject> getLabels() {
        return labels;
    }

    public List<assigneesObject> getAssignees() {
        return assignees;
    }

    public mergedByObject getMerged_by() {
        return merged_by;
    }

    public milestoneObject getMilestone() {
        return milestone;
    }

    public baseObject getBase() {
        return base;
    }

    public headObject getHead() {
        return head;
    }
}

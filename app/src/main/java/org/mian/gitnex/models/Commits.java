package org.mian.gitnex.models;

import java.util.Date;
import java.util.List;

/**
 * Author M M Arif
 */

public class Commits {

    private String url;
    private String sha;
    private String html_url;
    private commitObject commit;
    private authorObject author;
    private committerObject committer;
    private List<parentObject> parent;

    public String getUrl() {
        return url;
    }

    public String getSha() {
        return sha;
    }

    public String getHtml_url() {
        return html_url;
    }

    public Commits(String url) {
        this.url = url;
    }

    public static class commitObject {

        private String url;
        private CommitAuthor author;
        private CommitCommitter committer;
        private String message;
        private CommitTree tree;

        public static class CommitAuthor {

            String name;
            String email;
            Date date;

            public String getName() {
                return name;
            }

            public String getEmail() {
                return email;
            }

            public Date getDate() {
                return date;
            }

        }

        public static class CommitCommitter {

            String name;
            String email;
            Date date;

            public String getName() {
                return name;
            }

            public String getEmail() {
                return email;
            }

            public Date getDate() {
                return date;
            }

        }

        public static class CommitTree {

            String url;
            String sha;

            public String getUrl() {
                return url;
            }

            public String getSha() {
                return sha;
            }

        }

        public String getUrl() {
            return url;
        }

        public String getMessage() {
            return message;
        }

        public CommitAuthor getAuthor() {
            return author;
        }

        public CommitCommitter getCommitter() {
            return committer;
        }

        public CommitTree getTree() {
            return tree;
        }
    }

    public static class authorObject {

        private int id;
        private String login;
        private String full_name;
        private String email;
        private String avatar_url;
        private String language;
        private Boolean is_admin;
        private String last_login;
        private String created;
        private String username;

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

        public Boolean getIs_admin() {
            return is_admin;
        }

        public String getLast_login() {
            return last_login;
        }

        public String getCreated() {
            return created;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class committerObject {

        private int id;
        private String login;
        private String full_name;
        private String email;
        private String avatar_url;
        private String language;
        private Boolean is_admin;
        private String last_login;
        private String created;
        private String username;

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

        public Boolean getIs_admin() {
            return is_admin;
        }

        public String getLast_login() {
            return last_login;
        }

        public String getCreated() {
            return created;
        }

        public String getUsername() {
            return username;
        }

    }

    public static class parentObject {

        private String url;
        private String sha;

        public String getUrl() {
            return url;
        }

        public String getSha() {
            return sha;
        }
    }

    public commitObject getCommit() {
        return commit;
    }

    public authorObject getAuthor() {
        return author;
    }

    public committerObject getCommitter() {
        return committer;
    }

    public List<parentObject> getParent() {
        return parent;
    }

}




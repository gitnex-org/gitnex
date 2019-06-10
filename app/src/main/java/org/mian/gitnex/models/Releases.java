package org.mian.gitnex.models;

import java.util.Date;
import java.util.List;

public class Releases {

    private int id;
    private String tag_name;
    private String tag_commitish;
    private String name;
    private String body;
    private String url;
    private String tarball_url;
    private String zipball_url;
    private String draft;
    private boolean prerelease;
    private Date created_at;
    private Date published_at;

    private authorObject author;
    private List<assetsObject> assets;

    public class authorObject {

        private int id;
        private String login;
        private String full_name;
        private String email;
        private String avatar_url;
        private String language;
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

        public String getUsername() {
            return username;
        }
    }

   public class assetsObject {

        private int id;
        private String name;
        private int size;
        private int download_count;
        private Date created_at;
        private String uuid;
        private String browser_download_url;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getSize() {
            return size;
        }

        public int getDownload_count() {
            return download_count;
        }

        public Date getCreated_at() {
            return created_at;
        }

        public String getUuid() {
            return uuid;
        }

        public String getBrowser_download_url() {
            return browser_download_url;
        }
    }

    public int getId() {
        return id;
    }

    public String getTag_name() {
        return tag_name;
    }

    public String getTag_commitish() {
        return tag_commitish;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public String getUrl() {
        return url;
    }

    public String getTarball_url() {
        return tarball_url;
    }

    public String getZipball_url() {
        return zipball_url;
    }

    public String getDraft() {
        return draft;
    }

    public boolean isPrerelease() {
        return prerelease;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public Date getPublished_at() {
        return published_at;
    }

    public authorObject getAuthor() {
        return author;
    }

    public List<assetsObject> getAssets() {
        return assets;
    }
}

package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class FileDiffView {

    private String fileName;
    private boolean fileType;
    private String fileInfo;
    private String fileContents;

    public FileDiffView(String fileName, boolean fileType, String fileInfo, String fileContents)
    {

        this.fileName = fileName;
        this.fileType = fileType;
        this.fileInfo = fileInfo;
        this.fileContents = fileContents;

    }

    public String getFileName() {
        return fileName;
    }

    public boolean isFileType() {
        return fileType;
    }

    public String getFileInfo() {
        return fileInfo;
    }

    public String getFileContents() {
        return fileContents;
    }
}

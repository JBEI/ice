package org.jbei.ice.web.pages;

public class DownloadPage extends ExportPage {
    private String fileName;
    private String content;
    private String mimeType;

    public DownloadPage(String fileName, String mimeType, String content) {
        super();

        this.fileName = fileName;
        this.content = content;
        this.mimeType = mimeType;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }
}

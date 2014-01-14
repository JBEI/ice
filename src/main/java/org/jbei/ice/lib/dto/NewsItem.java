package org.jbei.ice.lib.dto;

import java.util.Date;

import org.jbei.ice.lib.dao.IDataTransferModel;

public class NewsItem implements IDataTransferModel {

    private String id;
    private Date creationDate;
    private String header;
    private String body;

    public NewsItem() {
    }

    public NewsItem(String id, Date creationDate, String header, String body) {
        this.id = id;
        this.creationDate = creationDate;
        this.header = header;
        this.body = body;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

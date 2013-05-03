package org.jbei.ice.shared.dto;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.shared.dto.group.GroupInfo;

/**
 * DTO for message object
 *
 * @author Hector Plahar
 */
public class MessageInfo implements IDTOModel {

    private long id;
    private Date sent;
    private String from;
    private String message;
    private String title;
    private boolean read;
    private ArrayList<GroupInfo> groups;
    private ArrayList<AccountInfo> accounts;

    public MessageInfo() {
        groups = new ArrayList<GroupInfo>();
        accounts = new ArrayList<AccountInfo>();
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public ArrayList<GroupInfo> getGroups() {
        return groups;
    }

    public ArrayList<AccountInfo> getAccounts() {
        return accounts;
    }
}

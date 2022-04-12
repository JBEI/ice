package org.jbei.ice.dto.message;

import org.jbei.ice.account.Account;
import org.jbei.ice.dto.group.UserGroup;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * DTO for message object
 *
 * @author Hector Plahar
 */
public class MessageInfo implements IDataTransferModel {

    private long id;
    private long sent;
    private Account from;
    private String message;
    private String title;
    private boolean read;
    private final ArrayList<UserGroup> userGroups;
    private final ArrayList<Account> accounts;

    public MessageInfo() {
        from = new Account();
        userGroups = new ArrayList<>();
        accounts = new ArrayList<>();
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

    public Account getFrom() {
        return from;
    }

    public void setFrom(Account from) {
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

    public ArrayList<UserGroup> getUserGroups() {
        return userGroups;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }
}

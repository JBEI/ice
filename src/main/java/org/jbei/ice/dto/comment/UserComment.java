package org.jbei.ice.dto.comment;

import org.jbei.ice.account.Account;
import org.jbei.ice.dto.sample.PartSample;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * Comments are tied to specific entries
 * and the entryId field is used to uniquely identify the entry this comment is tied to
 *
 * @author Hector Plahar
 */
public class UserComment implements IDataTransferModel {

    private long id;
    private Account account;
    private String message;
    private long commentDate;
    private long modified;
    private long entryId;
    private ArrayList<PartSample> samples;

    public UserComment() {
        this.samples = new ArrayList<>();
    }

    public Account getAccountTransfer() {
        return account;
    }

    public void setAccountTransfer(Account account) {
        this.account = account;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(long commentDate) {
        this.commentDate = commentDate;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public ArrayList<PartSample> getSamples() {
        return samples;
    }

    public void setSamples(ArrayList<PartSample> samples) {
        this.samples = samples;
    }
}

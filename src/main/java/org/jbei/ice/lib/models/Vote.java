package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class Vote implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private Account account;
    private Entry entry;
    private int score;
    private String comment;
    private Date creationTime;

    public Vote() {

    }

    public Vote(Entry entry, Account account, int score, String comment) {

        this.setEntry(entry);
        this.setAccount(account);
        this.setComment(comment);
        this.setScore(score);
        this.setCreationTime(Calendar.getInstance().getTime());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

}

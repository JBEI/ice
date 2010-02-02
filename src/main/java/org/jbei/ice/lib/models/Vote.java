package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "votes")
@SequenceGenerator(name = "sequence", sequenceName = "votes_id_seq", allocationSize = 1)
public class Vote implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @ManyToOne
    @JoinColumn(name = "accounts_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "comment", nullable = true)
    private String comment;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
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

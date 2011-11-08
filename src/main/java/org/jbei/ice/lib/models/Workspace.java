package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;

/**
 * Store view history information about an {@link Entry}.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
@Entity
@Table(name = "workspace")
@SequenceGenerator(name = "sequence", sequenceName = "workspace_id_seq", allocationSize = 1)
public class Workspace implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entry_id", nullable = false)
    private Entry entry;

    @Column(name = "in_workspace", nullable = false)
    private boolean inWorkspace = false;

    @Column(name = "starred", nullable = false)
    private boolean starred = false;

    @Column(name = "number_visited", nullable = false)
    private long numberVisited = 0;

    @Column(name = "date_added", nullable = true)
    private long dateAdded = System.currentTimeMillis();

    @Column(name = "date_visited", nullable = true)
    private long dateVisited;

    public Workspace(Account account2, Entry entry2) {
        setAccount(account2);
        setEntry(entry2);
    }

    public Workspace() {
        super();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public boolean isInWorkspace() {
        return inWorkspace;
    }

    public void setInWorkspace(boolean inWorkspace) {
        this.inWorkspace = inWorkspace;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public long getNumberVisited() {
        return numberVisited;
    }

    public void setNumberVisited(long numberVisited) {
        this.numberVisited = numberVisited;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getDateVisited() {
        return dateVisited;
    }

    public void setDateVisited(long dateVisited) {
        this.dateVisited = dateVisited;
    }

}

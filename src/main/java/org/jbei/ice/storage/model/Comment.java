package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Store comments about an {@link Entry} object, with the associated {@link org.jbei.ice
 * .lib.account.model.Account}.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "comments")
@SequenceGenerator(name = "sequence", sequenceName = "comments_id_seq", allocationSize = 1)
public class Comment implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accounts_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    @Column(name = "body", nullable = false)
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String body;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "comment_sample", joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "sample_id"))

    private Set<Sample> samples = new HashSet<>();

    public Comment() {
    }

    public Comment(Entry entry, Account account, String body) {
        setEntry(entry);
        setAccount(account);
        setBody(body);
        setCreationTime(Calendar.getInstance().getTime());
    }

    public long getId() {
        return id;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    @Override
    public UserComment toDataTransferObject() {
        UserComment userComment = new UserComment();
        userComment.setId(this.id);
        userComment.setCommentDate(this.creationTime.getTime());
        if (modificationTime != null) {
            userComment.setModified(this.modificationTime.getTime());
        }
        userComment.setMessage(getBody());
        userComment.setAccountTransfer(getAccount().toDataTransferObject());
        userComment.setEntryId(getEntry().getId());
        if (this.samples != null) {
            for (Sample sample : this.samples) {
                userComment.getSamples().add(sample.toDataTransferObject());
            }
        }
        return userComment;
    }
}

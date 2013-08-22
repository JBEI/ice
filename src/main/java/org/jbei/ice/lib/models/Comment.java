package org.jbei.ice.lib.models;

import java.util.Calendar;
import java.util.Date;
import javax.persistence.*;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.shared.dto.comment.UserComment;

import org.hibernate.annotations.Type;

/**
 * Store comments about an {@link org.jbei.ice.lib.entry.model.Entry} object, with the associated {@link org.jbei.ice
 * .lib.account.model.Account}.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "comments")
@SequenceGenerator(name = "sequence", sequenceName = "comments_id_seq", allocationSize = 1)
public class Comment implements IModel {

    private static final long serialVersionUID = 1L;

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

    public static UserComment toDTO(Comment comment) {
        if (comment == null)
            return null;

        UserComment userComment = new UserComment();
        userComment.setCommentDate(comment.getCreationTime());
        userComment.setMessage(comment.getBody());
        userComment.setUser(Account.toDTO(comment.getAccount()));
        userComment.setEntryId(comment.getEntry().getId());
        return userComment;
    }
}

package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.jbei.ice.lib.dto.message.MessageInfo;
import org.jbei.ice.lib.message.MessageStatus;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates system and user messages which can be sent to groups or individuals.
 * Maintains a parent/child relationship in order to capture message threads. This requires traversing the object
 * to retrieve parents and constructing the hierarchy.
 *
 * @author Hector Plahar
 */
@Indexed(index = "Message")
@Entity
@Table(name = "MESSAGE")
@SequenceGenerator(name = "sequence", sequenceName = "message_id_seq", allocationSize = 1)
public class Message implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "from_user", length = 127)
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String fromEmail;

    @ManyToMany
    @JoinTable(name = "message_destination_accounts",
               joinColumns = {@JoinColumn(name = "message_id")},
               inverseJoinColumns = {@JoinColumn(name = "account_id")})
    private Set<Account> destinationAccounts = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "message_destination_groups",
               joinColumns = {@JoinColumn(name = "message_id")},
               inverseJoinColumns = {@JoinColumn(name = "group_id")})
    private Set<Group> destinationGroups = new HashSet<>();

    @Column(name = "message")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Field
    private String message;

    @Column(name = "title", length = 100)
    @Field
    private String title;

    @Column(name = "sent")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date dateSent;

    @Column(name = "read")
    @Temporal(value = TemporalType.TIMESTAMP)
    @DateBridge(resolution = Resolution.DAY)
    private Date dateRead;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private MessageStatus status = MessageStatus.INBOX;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    private Message parent;

    public long getId() {
        return this.id;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
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

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public Date getDateRead() {
        return dateRead;
    }

    public void setDateRead(Date dateRead) {
        this.dateRead = dateRead;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public Set<Account> getDestinationAccounts() {
        return destinationAccounts;
    }

    public void setDestinationAccounts(HashSet<Account> destinationAccounts) {
        this.destinationAccounts = destinationAccounts;
    }

    public Set<Group> getDestinationGroups() {
        return destinationGroups;
    }

    public void setDestinationGroups(HashSet<Group> destinationGroups) {
        this.destinationGroups = destinationGroups;
    }

    @Override
    public MessageInfo toDataTransferObject() {
        MessageInfo info = new MessageInfo();
        info.setId(getId());
        info.setFrom(getFromEmail());
        info.setMessage(getMessage());
        info.setTitle(getTitle());
        info.setRead(isRead());
        info.setSent(this.dateSent.getTime());
        return info;
    }
}

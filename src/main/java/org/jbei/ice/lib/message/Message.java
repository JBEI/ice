package org.jbei.ice.lib.message;

import java.util.Date;
import javax.persistence.*;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.shared.dto.MessageInfo;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;

/**
 * @author Hector Plahar
 */
@Indexed(index = "Message")
@Entity
@Table(name = "MESSAGE")
public class Message implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Field(store = Store.YES, analyze = Analyze.NO)
    private long id;

    @Column(name = "from_user", length = 127)
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String fromEmail;

    @Column(name = "to_user", length = 127)
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String toEmail;

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

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
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

    public static MessageInfo toDTO(Message message) {
        MessageInfo info = new MessageInfo();
        info.setId(message.getId());
        info.setFrom(message.getFromEmail());
        info.setMessage(message.getMessage());
        info.setTitle(message.getTitle());
        info.setRead(message.isRead());
        info.setSent(message.getDateSent());
        return info;
    }
}

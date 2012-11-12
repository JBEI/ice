package org.jbei.ice.lib.message;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.shared.dto.MessageInfo;

import org.hibernate.search.annotations.Indexed;

/**
 * @author Hector Plahar
 */
@Indexed
@Entity
@Table(name = "MESSAGE")
public class Message implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String fromEmail;
    private String toEmail;
    private String message;
    private String title;
    private Date dateSent;
    private Date dateRead;
    private boolean isRead;

    public long getId() {
        return this.id;
    }


    public static MessageInfo toDTO(Message message) {
        MessageInfo info = new MessageInfo();
        info.setId(message.getId());
        return info;
    }
}

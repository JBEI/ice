package org.jbei.ice.lib.net.model;

import java.util.Date;
import javax.persistence.*;

import org.jbei.ice.lib.dao.IModel;

/**
 * Stores information about partners that this registry is involved with in web of registries
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REMOTE_PARTNER")
@SequenceGenerator(name = "sequence", sequenceName = "remote_partner_id_seq", allocationSize = 1)
public class RemotePartner implements IModel {

    private static final long serialVersionUID = 1l;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "name", length = 127)
    private String name;

    @Column(name = "url", length = 127, unique = true, nullable = false)
    private String url;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private RemoteActionStatus actionStatus;

    @Column(name = "add_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date added;

    @Column(name = "last_contact_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastContact;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RemoteActionStatus getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(RemoteActionStatus approved) {
        this.actionStatus = approved;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    public Date getLastContact() {
        return lastContact;
    }

    public void setLastContact(Date lastContact) {
        this.lastContact = lastContact;
    }
}

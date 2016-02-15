package org.jbei.ice.storage.model;

import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import javax.persistence.*;

/**
 * Model of a client that could exist locally or remotely and could be a member of a group
 * The client is considered local if it links to a
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "clients")
@SequenceGenerator(name = "sequence", sequenceName = "clients_id_seq", allocationSize = 1)
public class ClientModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "email")
    private String email;

    @OneToOne
    @JoinColumn(name = "remote_partner_id", nullable = true)
    private RemotePartner remotePartner;

    @OneToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @Override
    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RemotePartner getRemotePartner() {
        return remotePartner;
    }

    public void setRemotePartner(RemotePartner remotePartner) {
        this.remotePartner = remotePartner;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;
    }
}

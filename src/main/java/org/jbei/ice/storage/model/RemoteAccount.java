package org.jbei.ice.storage.model;

import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Hector Plahar
 */
@Entity
@Table(name = "remote_account")
@SequenceGenerator(name = "sequence", sequenceName = "remote_account_id_seq", allocationSize = 1)
public class RemoteAccount implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    private String userId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "remote_partner_id", nullable = false)
    private RemotePartner remotePartner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "remote_account_group", joinColumns = @JoinColumn(name = "remote_account_id"),
               inverseJoinColumns = @JoinColumn(name = "group_id"))

    private Set<Group> groups = new LinkedHashSet<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public RemotePartner getRemotePartner() {
        return remotePartner;
    }

    public void setRemotePartner(RemotePartner remotePartner) {
        this.remotePartner = remotePartner;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

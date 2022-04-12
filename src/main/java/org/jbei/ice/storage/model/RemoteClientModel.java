package org.jbei.ice.storage.model;

import org.jbei.ice.account.Account;
import org.jbei.ice.dto.web.RegistryPartner;
import org.jbei.ice.dto.web.RemoteUser;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Abstract notion of a client. Represents an account that could exist remotely and could be a member of a group.
 * Whereas users are required to identify and the remote users, only a single instance is kepy
 * <p>Fields:</p>
 * <ul>
 * <li><code><b>email</b></code>: Unique account identifier. Can conflict with a local account email</li>
 * <li><code><b>remotePartner</b></code>: Remote partner where the account resides. Must be in a web of registries
 * configuration with this ICE instance (api key exchanged etc)</li>
 * <li><code><b>groups</b></code>: Optional list of local groups if this client belongs to any local group</li>
 * </ul>
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "clients")
@SequenceGenerator(name = "clients_id", sequenceName = "clients_id_seq", allocationSize = 1)
public class RemoteClientModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "clients_id")
    private long id;

    @Column(name = "email")
    private String email;

    @OneToOne
    @JoinColumn(name = "remote_partner_id", nullable = false)
    private org.jbei.ice.storage.model.RemotePartner remotePartner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "client_group", joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<org.jbei.ice.storage.model.Group> groups = new LinkedHashSet<>();

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

    public org.jbei.ice.storage.model.RemotePartner getRemotePartner() {
        return remotePartner;
    }

    public void setRemotePartner(RemotePartner remotePartner) {
        this.remotePartner = remotePartner;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    @Override
    public RemoteUser toDataTransferObject() {
        RemoteUser remoteUser = new RemoteUser();
        RegistryPartner partner = new RegistryPartner();
        partner.setId(remotePartner.getId());
        partner.setUrl(remotePartner.getUrl());
        partner.setName(remotePartner.getName());
        remoteUser.setPartner(partner);

        Account account = new Account();
        account.setEmail(email);
        remoteUser.setUser(account);
        return remoteUser;
    }
}

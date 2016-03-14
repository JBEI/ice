package org.jbei.ice.storage.model;

import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

/**
 * Stores access information for remote shares
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REMOTE_ACCESS")
@SequenceGenerator(name = "sequence", sequenceName = "remote_access_id_seq", allocationSize = 1)
public class RemoteAccessModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "token")
    private String token;   // for access

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false)
    private RemoteClientModel remoteClientModel;  // who is doing the sharing on the remote end

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    /**
     * unique identifier for what is being shared. currently the folder id
     */
    @Column(name = "identifier")
    private String identifier;

    @Override
    public long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public RemoteClientModel getRemoteClientModel() {
        return remoteClientModel;
    }

    public void setRemoteClientModel(RemoteClientModel remoteClientModel) {
        this.remoteClientModel = remoteClientModel;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public AccessPermission toDataTransferObject() {
        return permission.toDataTransferObject();
    }
}

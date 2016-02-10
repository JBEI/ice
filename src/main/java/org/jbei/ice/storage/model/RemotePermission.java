package org.jbei.ice.storage.model;

import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import javax.persistence.*;

/**
 * Remote access credentials that allows access to remote entries
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REMOTE_PERMISSION")
@SequenceGenerator(name = "sequence", sequenceName = "remote_permission_id_seq", allocationSize = 1)

public class RemotePermission implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "secret")
    private String secret; // private secret used to validate remote secret

    @Enumerated(value = EnumType.STRING)
    private AccessType accessType; //read or write

    @Column(name = "can_read")
    private boolean canRead;

    @Column(name = "can_write")
    private boolean canWrite;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientModel client;

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public ClientModel getClient() {
        return client;
    }

    public void setClient(ClientModel client) {
        this.client = client;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }
}

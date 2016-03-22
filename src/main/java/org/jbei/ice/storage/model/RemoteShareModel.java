package org.jbei.ice.storage.model;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

/**
 * Stores shares for entries or folders. {@see RemoteAccessModel} which the remote
 * partner uses to store information about which entities a user on their end can access
 * remotely
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REMOTE_SHARE")
@SequenceGenerator(name = "sequence", sequenceName = "remote_share_id_seq", allocationSize = 1)
public class RemoteShareModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    // access verification token
    @Column(name = "secret")
    private String secret;

    // who is sharing (must be a local account)
    @OneToOne
    @JoinColumn(name = "sharer_id", nullable = true)
    private Account sharer;

    // what is being shared
    @OneToOne(orphanRemoval = true, mappedBy = "remoteShare")
    @JoinColumn(name = "permission_id", nullable = true)
    private Permission permission;

    // who is being shared with
    @OneToOne
    @JoinColumn(name = "client_id", nullable = false)
    private RemoteClientModel client;

    @Override
    public long getId() {
        return id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Account getSharer() {
        return sharer;
    }

    public void setSharer(Account sharer) {
        this.sharer = sharer;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public RemoteClientModel getClient() {
        return client;
    }

    public void setClient(RemoteClientModel client) {
        this.client = client;
    }

    @Override
    public AccessPermission toDataTransferObject() {
        AccessPermission accessPermission = new AccessPermission();
        accessPermission.setArticle(AccessPermission.Article.REMOTE);
        accessPermission.setArticleId(this.permission.getId()); // for remote access permissions, the article id is the actual permission
        accessPermission.setId(this.id);
        accessPermission.setType(this.permission.isCanWrite() ? AccessPermission.Type.WRITE_FOLDER : AccessPermission.Type.READ_FOLDER);
        AccountTransfer accountTransfer = new AccountTransfer();
        accountTransfer.setEmail(this.client.getEmail());
        accessPermission.setPartner(this.client.getRemotePartner().toDataTransferObject());
        accessPermission.setDisplay(accountTransfer.getEmail());
        return accessPermission;
    }
}

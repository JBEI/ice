package org.jbei.ice.storage.model;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.ContainedIn;
import org.jbei.ice.lib.access.PermissionEntryBridge;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

/**
 * Permission object for storing permissions related to either folders or entries
 * on an account or group basis. Also stores information for remote sharing
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "PERMISSION")
@ClassBridge(name = "permission", analyze = Analyze.NO, impl = PermissionEntryBridge.class)
@SequenceGenerator(name = "permission_id", sequenceName = "permission_id_seq", allocationSize = 1)
public class Permission implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "permission_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "can_read")
    private boolean canRead;

    @Column(name = "can_write")
    private boolean canWrite;

    @ManyToOne
    @JoinColumn(name = "entry_id")
    @ContainedIn
    private Entry entry;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    @ContainedIn
    private Folder folder;

    @ManyToOne
    @JoinColumn(name = "upload_id")
    private BulkUpload upload;

    // access verification token
    @Column(name = "secret")
    private String secret;

    // who is sharing (must be a local account)
    @OneToOne
    @JoinColumn(name = "sharer_id", nullable = true)
    private Account sharer;

    // who it's being shared with
    @OneToOne
    @JoinColumn(name = "client_id")
    private RemoteClientModel client;

    public long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public BulkUpload getUpload() {
        return upload;
    }

    public void setUpload(BulkUpload upload) {
        this.upload = upload;
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

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
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

    public RemoteClientModel getClient() {
        return client;
    }

    public void setClient(RemoteClientModel client) {
        this.client = client;
    }

    @Override
    public AccessPermission toDataTransferObject() {
        AccessPermission access = new AccessPermission();
        access.setId(id);

        if (group != null) {
            access.setArticle(AccessPermission.Article.GROUP);
            access.setArticleId(group.getId());
            access.setGroup(group.toDataTransferObject());
            access.setDisplay(group.getLabel());
        } else if (account != null) {
            access.setArticle(AccessPermission.Article.ACCOUNT);
            access.setArticleId(account.getId());
            access.setAccount(account.toDataTransferObject());
            access.setDisplay(getAccount().getFullName());
        }

        AccessPermission.Type type = null;
        long id = 0;
        if (entry != null) {
            type = isCanWrite() ? AccessPermission.Type.WRITE_ENTRY : AccessPermission.Type.READ_ENTRY;
            id = getEntry().getId();
        } else if (getFolder() != null) {
            type = isCanWrite() ? AccessPermission.Type.WRITE_FOLDER : AccessPermission.Type.READ_FOLDER;
            id = getFolder().getId();
        } else if (upload != null) {
            type = isCanWrite() ? AccessPermission.Type.WRITE_UPLOAD : AccessPermission.Type.READ_UPLOAD;
            id = upload.getId();
        }
        access.setType(type);
        access.setTypeId(id);
        return access;
    }
}

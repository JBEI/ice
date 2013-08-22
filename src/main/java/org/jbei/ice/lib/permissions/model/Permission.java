package org.jbei.ice.lib.permissions.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.ContainedIn;

/**
 * Permission object for storing permissions related to either folders or entries
 * on an account or group basis
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "PERMISSION")
@ClassBridge(name = "permission", analyze = Analyze.NO, impl = PermissionEntryBridge.class)
@SequenceGenerator(name = "sequence", sequenceName = "permission_id_seq", allocationSize = 1)
public class Permission implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private int id;

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
    private Folder folder;

    public int getId() {
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

    public static AccessPermission toDTO(Permission permission) {
        if (permission == null)
            return null;

        AccessPermission access = new AccessPermission();
        if (permission.getGroup() != null) {
            access.setArticle(AccessPermission.Article.GROUP);
            access.setArticleId(permission.getGroup().getId());
            access.setDisplay(permission.getGroup().getLabel());
        } else {
            access.setArticle(AccessPermission.Article.ACCOUNT);
            access.setArticleId(permission.getAccount().getId());
            access.setDisplay(permission.getAccount().getFullName());
        }

        AccessPermission.Type type = null;
        long id = 0;
        if (permission.entry != null) {
            type = permission.isCanWrite() ? AccessPermission.Type.WRITE_ENTRY : AccessPermission.Type.READ_ENTRY;
            id = permission.getEntry().getId();
        } else if (permission.getFolder() != null) {
            type = permission.isCanWrite() ? AccessPermission.Type.WRITE_FOLDER : AccessPermission.Type.READ_FOLDER;
            id = permission.getFolder().getId();
        }
        access.setType(type);
        access.setTypeId(id);
        return access;
    }
}

package org.jbei.ice.lib.access;

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
import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.group.Group;

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
public class Permission implements IDataModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
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
    private Folder folder;

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

    @Override
    public AccessPermission toDataTransferObject() {
        AccessPermission access = new AccessPermission();
        access.setId(id);

        if (group != null) {
            access.setArticle(AccessPermission.Article.GROUP);
            access.setArticleId(group.getId());
            access.setDisplay(group.getLabel());
        } else if (account != null) {
            access.setArticle(AccessPermission.Article.ACCOUNT);
            access.setArticleId(account.getId());
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
        }
        access.setType(type);
        access.setTypeId(id);
        return access;
    }
}

package org.jbei.ice.storage.model;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.bulkupload.BulkUploadStatus;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Saved draft of bulk imports. Encapsulates a list of {@link Entry}s that are created and updated
 * as part of a draft
 *
 * @author Hector Plahar
 */

@Entity
@Table(name = "bulk_upload")

@SequenceGenerator(name = "sequence", sequenceName = "bulk_upload_id_seq", allocationSize = 1)
public class BulkUpload implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "import_type", length = 50)
    private String importType;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time", nullable = false)
    private Date creationTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_time", nullable = false)
    private Date lastUpdateTime;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private BulkUploadStatus status;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "bulk_upload_entry",
            joinColumns = {@JoinColumn(name = "bulk_upload_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "entry_id", nullable = false)})
    private Set<Entry> contents = new HashSet<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
    @JoinTable(name = "bulk_upload_permissions",
            joinColumns = {@JoinColumn(name = "bulk_upload_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "permission_id", nullable = false)})
    private Set<Permission> permissions = new HashSet<>();

    public long getId() {
        return this.id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Entry> getContents() {
        return contents;
    }

    public void setContents(List<Entry> contents) {
        this.contents.clear();
        if (contents != null)
            this.contents.addAll(contents);
    }

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public BulkUploadStatus getStatus() {
        return status;
    }

    public void setStatus(BulkUploadStatus status) {
        this.status = status;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public BulkUploadInfo toDataTransferObject() {
        BulkUploadInfo bulkUploadInfo = new BulkUploadInfo();
        bulkUploadInfo.setCreated(getCreationTime());
        bulkUploadInfo.setId(getId());
        bulkUploadInfo.setLastUpdate(getLastUpdateTime());
        bulkUploadInfo.setStatus(getStatus());

        // draft account
        Account draftAccount = getAccount();
        bulkUploadInfo.setName(getName());
        AccountTransfer accountTransfer = new AccountTransfer();
        accountTransfer.setEmail(draftAccount.getEmail());
        accountTransfer.setFirstName(draftAccount.getFirstName());
        accountTransfer.setLastName(draftAccount.getLastName());
        bulkUploadInfo.setAccount(accountTransfer);

        bulkUploadInfo.setType(getImportType());

        if (permissions != null) {
            for (Permission permission : permissions) {
                bulkUploadInfo.getPermissions().add(permission.toDataTransferObject());
            }
        }
        return bulkUploadInfo;
    }
}

package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.jbei.ice.account.Account;
import org.jbei.ice.bulkupload.BulkUploadInfo;
import org.jbei.ice.bulkupload.BulkUploadStatus;
import org.jbei.ice.storage.DataModel;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Saved draft of bulk imports. Encapsulates a list of {@link org.jbei.ice.storage.model.Entry}s that are created and updated
 * as part of a draft
 *
 * @author Hector Plahar
 */

@Entity
@Table(name = "bulk_upload")

@SequenceGenerator(name = "bulk_upload_id", sequenceName = "bulk_upload_id_seq", allocationSize = 1)
public class BulkUpload implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "bulk_upload_id")
    private long id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "import_type", length = 50)
    private String importType;

    @Column(name = "link_type", length = 50)
    private String linkType;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountModel account;

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
    private Set<org.jbei.ice.storage.model.Entry> contents = new HashSet<>();

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

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<org.jbei.ice.storage.model.Entry> getContents() {
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

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getLinkType() {
        return this.linkType;
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
        bulkUploadInfo.setCreated(creationTime);
        bulkUploadInfo.setId(id);
        bulkUploadInfo.setLastUpdate(lastUpdateTime);
        bulkUploadInfo.setStatus(status);
        bulkUploadInfo.setName(name);
        bulkUploadInfo.setType(this.importType);
        bulkUploadInfo.setLinkType(this.linkType);

        AccountModel draftAccount = getAccount();
        Account account = new Account();
        account.setEmail(draftAccount.getEmail());
        account.setFirstName(draftAccount.getFirstName());
        account.setLastName(draftAccount.getLastName());
        bulkUploadInfo.setAccount(account);

        if (permissions != null) {
            for (Permission permission : permissions) {
                bulkUploadInfo.getPermissions().add(permission.toDataTransferObject());
            }
        }
        return bulkUploadInfo;
    }
}

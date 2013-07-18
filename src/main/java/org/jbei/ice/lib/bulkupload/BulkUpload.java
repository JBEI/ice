package org.jbei.ice.lib.bulkupload;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.Preference;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.shared.dto.user.User;

/**
 * Saved draft of bulk imports. Encapsulates a list of {@link Entry}s that are created and updated
 * as part of a draft
 *
 * @author Hector Plahar
 */

@Entity
@Table(name = "bulk_upload")

@SequenceGenerator(name = "sequence", sequenceName = "bulk_upload_id_seq", allocationSize = 1)
public class BulkUpload implements IModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private Long id;

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

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "bulk_upload_preferences",
               joinColumns = {@JoinColumn(name = "bulk_upload_id", nullable = false)},
               inverseJoinColumns = {@JoinColumn(name = "preference_id", nullable = false)})
    private Set<Preference> preferences = new HashSet<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
    @JoinTable(name = "bulk_upload_permissions",
               joinColumns = {@JoinColumn(name = "bulk_upload_id", nullable = false)},
               inverseJoinColumns = {@JoinColumn(name = "permission_id", nullable = false)})
    private Set<Permission> permissions = new HashSet<>();


    public BulkUpload() {
    }

    public Long getId() {
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

    public Set<Preference> getPreferences() {
        return preferences;
    }

    public static BulkUploadInfo toDTO(BulkUpload draft) {
        if (draft == null)
            return null;

        BulkUploadInfo bulkUploadInfo = new BulkUploadInfo();
        bulkUploadInfo.setCreated(draft.getCreationTime());
        bulkUploadInfo.setId(draft.getId());
        bulkUploadInfo.setLastUpdate(draft.getLastUpdateTime());

        // draft account
        Account draftAccount = draft.getAccount();
        bulkUploadInfo.setName(draft.getName());
        User user = new User();
        user.setEmail(draftAccount.getEmail());
        user.setFirstName(draftAccount.getFirstName());
        user.setLastName(draftAccount.getLastName());
        bulkUploadInfo.setAccount(user);

        bulkUploadInfo.setType(EntryAddType.stringToType(draft.getImportType()));
        for (Permission permission : draft.getPermissions()) {
            bulkUploadInfo.getPermissions().add(Permission.toDTO(permission));
        }
        return bulkUploadInfo;
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
}

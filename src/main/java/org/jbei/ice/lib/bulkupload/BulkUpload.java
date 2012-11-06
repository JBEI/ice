package org.jbei.ice.lib.bulkupload;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.*;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private Long id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "import_type", length = 50)
    private String importType;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = true)
    private Group readGroup;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time", nullable = false)
    private Date creationTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_time", nullable = false)
    private Date lastUpdateTime;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinTable(name = "bulk_upload_entry",
               joinColumns = {@JoinColumn(name = "bulk_upload_id", nullable = false)},
               inverseJoinColumns = {@JoinColumn(name = "entry_id", nullable = false)})
    private List<Entry> contents = new LinkedList<Entry>();

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

    public List<Entry> getContents() {
        return contents;
    }

    public void setContents(List<Entry> contents) {
        this.contents = contents;
    }

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public Group getReadGroup() {
        return readGroup;
    }

    public void setReadGroup(Group readGroup) {
        this.readGroup = readGroup;
    }
}

package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Store Project information for user.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
@Entity
@Table(name = "projects")
@SequenceGenerator(name = "sequence", sequenceName = "projects_id_seq", allocationSize = 1)
public class Project implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accounts_id", unique = true, nullable = false)
    private Account account;

    @Column(name = "uuid", length = 36, nullable = false)
    private String uuid;

    @Column(name = "type", length = 31, nullable = false)
    private String type;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Column(name = "data")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String data;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    public Project() {
        super();
    }

    public Project(Account account, String uuid, String type, String name, String description,
            String data, Date creationTime, Date modificationTime) {
        super();
        this.account = account;
        this.uuid = uuid;
        this.type = type;
        this.name = name;
        this.description = description;
        this.data = data;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
    }

    public long setId() {
        return id;
    }

    public void getId(long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

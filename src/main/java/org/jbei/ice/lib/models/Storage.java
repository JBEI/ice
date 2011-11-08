package org.jbei.ice.lib.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.jbei.ice.lib.dao.IModel;

/**
 * Store sample storage location information as well as the hierarchical structure.
 * <p>
 * 
 * 
 * @author Timothy Ham, Hector Plahar
 * 
 */
@Entity
@Table(name = "storage")
@SequenceGenerator(name = "sequence", sequenceName = "storage_id_seq", allocationSize = 1)
public class Storage implements IModel {

    private static final long serialVersionUID = 1L;

    public enum StorageType {
        GENERIC, FREEZER, SHELF, BOX_INDEXED, BOX_UNINDEXED, PLATE96, WELL, TUBE, SCHEME
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private Storage parent;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "index", length = 31, nullable = true)
    private String index;

    @Column(name = "description", length = 1023)
    private String description;

    @Column(name = "uuid", length = 36, nullable = false)
    private String uuid;

    @Column(name = "storage_type")
    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @Column(name = "owner_email", length = 255, nullable = false)
    private String ownerEmail;

    @Column(name = "schemes")
    @Lob
    private ArrayList<Storage> schemes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "parent")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy("id")
    @JoinColumn(name = "parent_id")
    private final Set<Storage> children = null;

    public Storage() {
        super();
    }

    public Storage(String name, String description, StorageType storageType, String ownerEmail,
            Storage parent) {
        setName(name);
        setDescription(description);
        setStorageType(storageType);
        setOwnerEmail(ownerEmail);
        setParent(parent);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Storage getParent() {
        return parent;
    }

    public void setParent(Storage parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    /**
     * Make sure to get a copy from database before calling this method
     * 
     * @return
     */
    public Set<Storage> getChildren() {
        return children;
    }

    public static Map<String, String> getLocationTypeOptionsMap() {
        Map<String, String> result = new HashMap<String, String>();

        result.put(StorageType.GENERIC.toString(), "Generic");
        result.put(StorageType.FREEZER.toString(), "Freezer");
        result.put(StorageType.SHELF.toString(), "Shelf");
        result.put(StorageType.BOX_INDEXED.toString(), "Indexed Box");
        result.put(StorageType.BOX_UNINDEXED.toString(), "Unindexed Box");
        result.put(StorageType.PLATE96.toString(), "96 Well Plate");
        result.put(StorageType.WELL.toString(), "Numbered Well");
        result.put(StorageType.TUBE.toString(), "Labeled Tube");

        return result;
    }

    public void setSchemes(ArrayList<Storage> schemes) {
        this.schemes = schemes;
    }

    public ArrayList<Storage> getSchemes() {
        return schemes;
    }

    @Override
    public String toString() {
        if (getStorageType().equals(StorageType.SCHEME)) {
            return getName();
        } else {
            return getName() + " " + getIndex();
        }
    }
}

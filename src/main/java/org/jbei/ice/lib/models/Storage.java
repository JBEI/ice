package org.jbei.ice.lib.models;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.jbei.ice.lib.dao.IModel;

@Entity
@Table(name = "storage")
@SequenceGenerator(name = "sequence", sequenceName = "storage_id_seq", allocationSize = 1)
public class Storage implements IModel {

    private static final long serialVersionUID = 1L;

    public enum StorageType {
        FREEZER, SHELF, BOX_INDEXED, BOX_UNINDEXED, PLATE96, WELL, TUBE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Storage parent;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", length = 1023)
    private String description;

    @Column(name = "storage_type")
    @Enumerated(EnumType.STRING)
    private StorageType locationType;

    @Column(name = "owner_email", length = 255, nullable = false)
    private String ownerEmail;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "parent")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy("id")
    @JoinColumn(name = "parent_id")
    private final Set<Storage> children = null;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StorageType getLocationType() {
        return locationType;
    }

    public void setLocationType(StorageType locationType) {
        this.locationType = locationType;
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

        result.put(StorageType.FREEZER.toString(), "Freezer");
        result.put(StorageType.SHELF.toString(), "Shelf");
        result.put(StorageType.BOX_INDEXED.toString(), "Indexed Box");
        result.put(StorageType.BOX_UNINDEXED.toString(), "Unindexed Box");
        result.put(StorageType.PLATE96.toString(), "96 Well Plate");
        result.put(StorageType.WELL.toString(), "Numbered Well");
        result.put(StorageType.TUBE.toString(), "Labeled Tube");

        return result;
    }
}

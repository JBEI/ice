package org.jbei.ice.lib.models;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;

@Entity
@Table(name = "location_new")
//TODO rename this table after migration
@SequenceGenerator(name = "location_new", sequenceName = "location_new_id_seq", allocationSize = 1)
public class LocationNew implements IModel {

    private static final long serialVersionUID = 1L;

    public enum LocationType {
        FREEZER, SHELF, BOX, WELL96, WELL, TUBE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @Column(name = "parent_id")
    private LocationNew parent;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "name", length = 1023)
    private String description;

    @Column(name = "location_type")
    @Enumerated(EnumType.STRING)
    private LocationType locationType;

    @Column(name = "owner_email", length = 255)
    private String ownerEmail;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocationNew getParent() {
        return parent;
    }

    public void setParent(LocationNew parent) {
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

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public static Map<String, String> getLocationTypeOptionsMap() {
        Map<String, String> result = new HashMap<String, String>();

        result.put(LocationType.FREEZER.toString(), "Freezer");
        result.put(LocationType.SHELF.toString(), "Shelf");
        result.put(LocationType.BOX.toString(), "Box");
        result.put(LocationType.WELL96.toString(), "96 Well Plate");
        result.put(LocationType.WELL.toString(), "Well");
        result.put(LocationType.TUBE.toString(), "Labeled Tube");

        return result;
    }
}

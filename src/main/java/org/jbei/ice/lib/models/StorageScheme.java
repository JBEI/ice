package org.jbei.ice.lib.models;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.LocationNew.LocationType;

@Entity
@Table(name = "storage_scheme")
@SequenceGenerator(name = "sequence", sequenceName = "storage_scheme_id_seq", allocationSize = 1)
public class StorageScheme implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "schemes")
    @Lob
    private List<HashMap<String, LocationType>> schemes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<HashMap<String, LocationType>> getSchemes() {
        return schemes;
    }

    public void setSchemes(List<HashMap<String, LocationType>> schemes) {
        this.schemes = schemes;
    }

}

package org.jbei.ice.lib.models;

import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Storage.StorageType;

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
    private LinkedHashMap<String, StorageType> schemes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storage_id")
    private Storage parent;

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

    public LinkedHashMap<String, StorageType> getSchemes() {
        return schemes;
    }

    public void setSchemes(LinkedHashMap<String, StorageType> schemes) {
        this.schemes = schemes;
    }

    public void setParent(Storage parent) {
        this.parent = parent;
    }

    public Storage getParent() {
        return parent;
    }

}

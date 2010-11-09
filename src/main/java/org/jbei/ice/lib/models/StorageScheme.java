package org.jbei.ice.lib.models;

import java.util.ArrayList;
import java.util.List;

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

@Entity
@Table(name = "storage_scheme")
@SequenceGenerator(name = "sequence", sequenceName = "storage_scheme_id_seq", allocationSize = 1)
public class StorageScheme implements IModel {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_PLASMID_STORAGE_SCHEME = "Default Plasmid Storage Scheme";
    public static final String DEFAULT_STRAIN_STORAGE_SCHEME = "Default Strain Storage Scheme";
    public static final String DEFAULT_PART_STORAGE_SCHEME = "Default Part Storage Scheme";
    public static final String DEFAULT_ARABIDOPSIS_STORAGE_SCHEME = "Default Arabidopsis Storage Scheme";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "schemes")
    @Lob
    private ArrayList<Storage> schemes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storage_id")
    private Storage root;

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

    public List<Storage> getSchemes() {
        return schemes;
    }

    public void setSchemes(ArrayList<Storage> schemes) {
        this.schemes = schemes;
    }

    public void setRoot(Storage root) {
        this.root = root;
    }

    public Storage getRoot() {
        return root;
    }

}

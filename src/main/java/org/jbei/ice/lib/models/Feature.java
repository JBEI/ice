package org.jbei.ice.lib.models;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cascade;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.vo.IFeatureValueObject;

@Entity
@Table(name = "features")
@SequenceGenerator(name = "sequence", sequenceName = "features_id_seq", allocationSize = 1)
public class Feature implements IFeatureValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @Column(name = "name", length = 127)
    private String name;

    @Column(name = "description", length = 1023)
    private String description;

    @Column(name = "identification", length = 127)
    private String identification;

    @Column(name = "uuid", length = 36)
    private String uuid;

    @Column(name = "auto_find")
    private int autoFind;

    @Column(name = "genbank_type", length = 127)
    private String genbankType;

    @OneToOne(mappedBy = "feature", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private FeatureDNA featureDna;

    public Feature() {
        super();
    }

    public Feature(String name, String description, String identification, String uuid,
            int autoFind, String genbankType) {
        super();

        this.name = name;
        this.description = description;
        this.identification = identification;
        this.uuid = uuid;
        this.autoFind = autoFind;
        this.genbankType = genbankType;
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

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @XmlTransient
    public int getAutoFind() {
        return autoFind;
    }

    public void setAutoFind(int autoFind) {
        this.autoFind = autoFind;
    }

    public String getGenbankType() {
        return genbankType;
    }

    public void setGenbankType(String genbankType) {
        this.genbankType = genbankType;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlTransient
    public int getId() {
        return id;
    }

    public void setFeatureDna(FeatureDNA featureDna) {
        this.featureDna = featureDna;
    }

    public FeatureDNA getFeatureDna() {
        return featureDna;
    }
}

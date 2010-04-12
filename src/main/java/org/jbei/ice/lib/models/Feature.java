package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.IFeatureValueObject;
import org.jbei.ice.lib.utils.Utils;

@Entity
@Table(name = "features")
@SequenceGenerator(name = "sequence", sequenceName = "features_id_seq", allocationSize = 1)
public class Feature implements IFeatureValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    /**
     * This field is the human annotated name. It is to be used for auto fill.
     */
    @Column(name = "name", length = 127)
    private String name;

    /**
     * this is the human annotated description.
     */
    @Column(name = "description", length = 1023)
    private String description;

    @Column(name = "identification", length = 127)
    private String identification;

    @Column(name = "hash_sha", length = 40, nullable = false, unique = true)
    private String hash;

    @Column(name = "sequence", nullable = false)
    @Lob
    private String sequence;

    @Column(name = "auto_find")
    private int autoFind;

    @Column(name = "genbank_type", length = 127)
    private String genbankType;

    public Feature() {
        super();
    }

    public Feature(String name, String description, String identification, String sequence,
            int autoFind, String genbankType) {
        super();

        this.name = name;
        this.description = description;
        this.identification = identification;
        this.autoFind = autoFind;
        this.genbankType = genbankType;
        setSequence(sequence);
        this.hash = Utils.encryptSHA(sequence);

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

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setSequence(String sequence) {
        if (sequence != null) {
            this.sequence = sequence.toLowerCase();
        }
    }

    public String getSequence() {
        return sequence;
    }

}

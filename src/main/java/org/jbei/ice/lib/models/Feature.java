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
import org.jbei.ice.lib.utils.SequenceUtils;

@Entity
@Table(name = "features")
@SequenceGenerator(name = "sequence", sequenceName = "features_id_seq", allocationSize = 1)
public class Feature implements IFeatureValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "name", length = 127)
    private String name;

    @Column(name = "description")
    @Lob
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
        hash = SequenceUtils.calculateSequenceHash(sequence);

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getIdentification() {
        return identification;
    }

    @Override
    public void setIdentification(String identification) {
        this.identification = identification;
    }

    @Override
    @XmlTransient
    public int getAutoFind() {
        return autoFind;
    }

    @Override
    public void setAutoFind(int autoFind) {
        this.autoFind = autoFind;
    }

    @Override
    public String getGenbankType() {
        return genbankType;
    }

    @Override
    public void setGenbankType(String genbankType) {
        this.genbankType = genbankType;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    @XmlTransient
    public long getId() {
        return id;
    }

    @Override
    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public void setSequence(String sequence) {
        if (sequence != null) {
            this.sequence = sequence.toLowerCase();
        }
    }

    @Override
    public String getSequence() {
        return sequence;
    }

}

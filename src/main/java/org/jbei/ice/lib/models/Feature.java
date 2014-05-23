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

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.utils.SequenceUtils;

import org.hibernate.annotations.Type;

/**
 * Represents a unique sequence annotation known to this instance of gd-ice.
 * <p/>
 * Annotated sequences associated with entries are parsed, and their fields are saved into the
 * database as Features. These features have unique identifiers via hash. In the future, select set
 * of Features will be hand annotated with proper name, identification, genbankType, and then used
 * for automatic identification and annotation of unknown sequences.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "features")
@SequenceGenerator(name = "sequence", sequenceName = "features_id_seq", allocationSize = 1)
public class Feature implements IDataModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "name", length = 127)
    private String name;

    @Column(name = "identification", length = 127)
    private String identification;

    @Column(name = "hash_sha", length = 40, nullable = false, unique = true)
    private String hash;

    @Column(name = "sequence", nullable = false)
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String sequence;

    @Column(name = "auto_find")
    private int autoFind;

    @Column(name = "genbank_type", length = 127)
    private String genbankType;

    @Column(name = "uri")
    private String uri;

    public Feature() {
        super();
    }

    public Feature(String name, String identification, String sequence, int autoFind, String genbankType) {
        super();

        this.name = name;
        this.identification = identification;
        this.autoFind = autoFind;
        this.genbankType = genbankType;
        setSequence(sequence);
        hash = SequenceUtils.calculateSequenceHash(sequence);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setId(long id) {
        this.id = id;
    }

    @XmlTransient
    public long getId() {
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

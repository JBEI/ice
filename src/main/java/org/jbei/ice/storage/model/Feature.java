package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

/**
 * Represents a unique sequence annotation known to this instance of gd-ice.
 * <p>
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
public class Feature implements DataModel {

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

    @Column(name = "genbank_type", length = 127)
    private String genbankType;

    @Column(name = "uri")
    private String uri;

    @OneToOne
    private FeatureCurationModel curation;

    public Feature() {
    }

    public Feature(String name, String identification, String sequence, String genbankType) {
        this.name = name;
        this.identification = identification;
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

    public String getGenbankType() {
        return genbankType;
    }

    public void setGenbankType(String genbankType) {
        this.genbankType = genbankType;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public FeatureCurationModel getCuration() {
        return curation;
    }

    public void setCuration(FeatureCurationModel curation) {
        this.curation = curation;
    }

    @Override
    public DNAFeature toDataTransferObject() {
        DNAFeature dnaFeature = new DNAFeature();
        dnaFeature.setId(this.id);
        dnaFeature.setAnnotationType(this.genbankType);
        dnaFeature.setName(this.name);
        dnaFeature.setUri(this.uri);

        if (this.curation != null) {
            dnaFeature.setCuration(this.curation.toDataTransferObject());
        }
        return dnaFeature;
    }
}

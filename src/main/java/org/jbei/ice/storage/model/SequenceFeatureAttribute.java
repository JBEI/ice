package org.jbei.ice.storage.model;

import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import javax.persistence.*;

/**
 * Store genbank style attributes.
 *
 * @author Timothy Ham
 */
@Entity
@Table(name = "sequence_feature_attribute")
@SequenceGenerator(name = "sequence", sequenceName = "sequence_feature_attribute_id_seq", allocationSize = 1)
public class SequenceFeatureAttribute implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "key", length = 511)
    private String key;

    @Column(name = "value", length = 4095)
    private String value;

    @Column(name = "quoted", nullable = false)
    private Boolean quoted;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sequence_feature_id")
    private SequenceFeature sequenceFeature;

    public SequenceFeatureAttribute() {
        super();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getQuoted() {
        return quoted;
    }

    public void setQuoted(Boolean quoted) {
        this.quoted = quoted;
    }

    public SequenceFeature getSequenceFeature() {
        return sequenceFeature;
    }

    public void setSequenceFeature(SequenceFeature sequenceFeature) {
        this.sequenceFeature = sequenceFeature;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

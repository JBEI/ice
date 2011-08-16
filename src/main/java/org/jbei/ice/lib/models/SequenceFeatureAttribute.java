package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;

@Entity
@Table(name = "sequence_feature_attribute")
@SequenceGenerator(name = "sequence", sequenceName = "sequence_feature_attribute_id_seq", allocationSize = 1)
public class SequenceFeatureAttribute implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
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
}

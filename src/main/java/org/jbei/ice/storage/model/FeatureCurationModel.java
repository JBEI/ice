package org.jbei.ice.storage.model;

import org.jbei.ice.lib.dto.Curation;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

/**
 * @author Hector Plahar
 */
@Entity
@Table(name = "sequence_annotation_curation")
@SequenceGenerator(name = "sequence", sequenceName = "sequence_annotation_curation_id_seq", allocationSize = 1)
public class FeatureCurationModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @OneToOne
    private Feature feature;

    @Column(name = "exclude")
    private boolean exclude;

    @Override
    public long getId() {
        return id;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    @Override
    public Curation toDataTransferObject() {
        Curation curation = new Curation();
        curation.setExclude(this.exclude);
        return curation;
    }
}

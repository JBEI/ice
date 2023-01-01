package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.jbei.ice.dto.Curation;
import org.jbei.ice.storage.DataModel;

/**
 * @author Hector Plahar
 */
@Entity
@Table(name = "sequence_annotation_curation")
@SequenceGenerator(name = "sequence_annotation_curation_id", sequenceName = "sequence_annotation_curation_id_seq", allocationSize = 1)
public class FeatureCurationModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence_annotation_curation_id")
    private long id;

    @OneToOne
    private org.jbei.ice.storage.model.Feature feature;

    @Column(name = "exclude")
    private boolean exclude;

    @Override
    public long getId() {
        return id;
    }

    public org.jbei.ice.storage.model.Feature getFeature() {
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

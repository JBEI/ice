package org.jbei.ice.services.blazeds.VectorEditor.vo;

import java.io.Serializable;
import java.util.Set;

public class LightSequence implements Serializable {
    private static final long serialVersionUID = -5915993784977746959L;

    private String sequence;
    private Set<LightFeature> features;

    public LightSequence() {
    }

    public LightSequence(String sequence, Set<LightFeature> features) {
        super();
        this.sequence = sequence;
        this.features = features;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public Set<LightFeature> getFeatures() {
        return features;
    }

    public void setFeatures(Set<LightFeature> features) {
        this.features = features;
    }
}

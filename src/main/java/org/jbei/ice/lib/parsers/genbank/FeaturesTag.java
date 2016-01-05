package org.jbei.ice.lib.parsers.genbank;

import org.jbei.ice.lib.dto.DNAFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class FeaturesTag extends Tag {

    public FeaturesTag() {
        super(Type.FEATURES);
    }

    private List<DNAFeature> features = new ArrayList<>();

    public void setFeatures(List<DNAFeature> features) {
        this.features = features;
    }

    public List<DNAFeature> getFeatures() {
        return features;
    }
}

package org.jbei.ice.lib.parsers.genbank;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.vo.DNAFeature;

/**
 * @author Hector Plahar
 */
public class FeaturesTag extends Tag {

    public FeaturesTag() {
        super(Type.FEATURES);
    }

    private List<DNAFeature> features = new ArrayList<DNAFeature>();

    public void setFeatures(List<DNAFeature> features) {
        this.features = features;
    }

    public List<DNAFeature> getFeatures() {
        return features;
    }
}

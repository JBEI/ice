package org.jbei.ice.lib.dto;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around a list of {@link DNAFeature}s that have the same label
 *
 * @author Hector Plahar
 */
public class DNAFeatures implements IDataTransferModel {

    private final String label;
    private List<DNAFeature> features;

    public DNAFeatures(String label) {
        this.label = label;
        this.features = new ArrayList<>();
    }

    public String getLabel() {
        return label;
    }

    public List<DNAFeature> getFeatures() {
        return features;
    }
}

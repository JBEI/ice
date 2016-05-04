package org.jbei.ice.lib.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * Value object to hold {@link DNAFeature}s and some genbank file information.
 *
 * @author Zinovii Dmytriv
 */
public class FeaturedDNASequence extends DNASequence {

    private static final long serialVersionUID = 1L;

    private List<DNAFeature> features = new LinkedList<>();
    private String identifier = "";
    private String name = "";
    private boolean isCircular = true;
    private String description;
    private String uri;
    private String dcUri;
    private boolean canEdit;
    private long length;

    public FeaturedDNASequence() {
        super();
    }

    public FeaturedDNASequence(String sequence, List<DNAFeature> features) {
        super(sequence);
        this.features = features;
    }

    public FeaturedDNASequence(String sequence, String name, boolean isCircular,
            List<DNAFeature> features, String identifier) {
        super(sequence);

        this.name = name;
        this.isCircular = isCircular;
        this.features = features;
        this.identifier = identifier;
    }

    public List<DNAFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<DNAFeature> features) {
        this.features = features;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsCircular() {
        return isCircular;
    }

    public void setIsCircular(boolean isCircular) {
        this.isCircular = isCircular;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDcUri() {
        return dcUri;
    }

    public void setDcUri(String dcUri) {
        this.dcUri = dcUri;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}

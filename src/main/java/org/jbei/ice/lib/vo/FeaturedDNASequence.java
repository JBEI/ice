package org.jbei.ice.lib.vo;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FeaturedDNASequence extends SimpleDNASequence {
    private static final long serialVersionUID = 1L;

    private List<DNAFeature> features = new LinkedList<DNAFeature>();
    private String accessionNumber = "";
    private String identifier = "";

    public FeaturedDNASequence() {
        super();
    }

    public FeaturedDNASequence(String sequence, List<DNAFeature> features) {
        super(sequence);

        this.features = features;
    }

    public FeaturedDNASequence(String sequence, List<DNAFeature> features, String accessionNumber,
            String identifier) {
        super(sequence);

        this.features = features;
        this.accessionNumber = accessionNumber;
        this.identifier = identifier;
    }

    public List<DNAFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<DNAFeature> features) {
        this.features = features;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}

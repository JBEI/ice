package org.jbei.ice.lib.vo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.lib.models.SequenceFeature;

/**
 * Value object to hold a combination of {@link SequenceFeature} and
 * {@link org.jbei.ice.lib.models.Feature Feature} data.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public class DNAFeature implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type = "";
    private String name = "";
    private int strand = 1;
    private String annotationType;
    private List<DNAFeatureNote> notes = new LinkedList<DNAFeatureNote>();
    private List<DNAFeatureLocation> locations = new LinkedList<DNAFeatureLocation>();

    public DNAFeature() {
        super();
    }

    public DNAFeature(String type, String name, int strand, List<DNAFeatureNote> notes,
            String annotationType) {
        super();

        this.type = type;
        this.name = name;
        this.strand = strand;
        this.notes = notes;
        this.annotationType = annotationType; // Is this used?
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DNAFeatureNote> getNotes() {
        return notes;
    }

    public void setNotes(List<DNAFeatureNote> notes) {
        this.notes = notes;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public void addNote(DNAFeatureNote dnaFeatureNote) {
        notes.add(dnaFeatureNote);
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public void setLocations(List<DNAFeatureLocation> locations) {
        this.locations = locations;
    }

    public List<DNAFeatureLocation> getLocations() {
        return locations;
    }
}

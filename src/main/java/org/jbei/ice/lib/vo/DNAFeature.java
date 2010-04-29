package org.jbei.ice.lib.vo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class DNAFeature implements Serializable {
    private static final long serialVersionUID = 1L;

    private int start = 0;
    private int end = 0;
    private String type = "";
    private String name = "";
    private int strand = 1;
    private List<DNAFeatureNote> notes = new LinkedList<DNAFeatureNote>();

    public DNAFeature() {
        super();
    }

    public DNAFeature(int start, int end, String type, String name, int strand,
            List<DNAFeatureNote> notes) {
        super();

        this.start = start;
        this.end = end;
        this.type = type;
        this.name = name;
        this.strand = strand;
        this.notes = notes;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
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
}

package org.jbei.ice.services.blazeds.VectorEditor.vo;

import java.io.Serializable;

public class LightFeature implements Serializable {
    private static final long serialVersionUID = -6481017555225922163L;

    private String name;
    private int start;
    private int end;
    private int strand;
    private String type;
    private String description;

    public LightFeature() {
    }

    public LightFeature(String name, int start, int end, int strand, String type, String description) {
        super();
        this.name = name;
        this.start = start;
        this.end = end;
        this.strand = strand;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

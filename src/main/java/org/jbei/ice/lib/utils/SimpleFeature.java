package org.jbei.ice.lib.utils;

public class SimpleFeature {
    private String sequence;
    private int start;
    private int end;

    public SimpleFeature(String sequence, int start, int end) {
        setSequence(sequence);
        setStart(start);
        setEnd(end);
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
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
}
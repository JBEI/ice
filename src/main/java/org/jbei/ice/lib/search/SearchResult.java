package org.jbei.ice.lib.search;

import java.io.Serializable;

import org.jbei.ice.lib.models.Entry;

public class SearchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Entry entry;
    private float score;

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getScore() {
        return score;
    }

    public SearchResult(Entry entry, float score) {
        setEntry(entry);
        setScore(score);
    }
}

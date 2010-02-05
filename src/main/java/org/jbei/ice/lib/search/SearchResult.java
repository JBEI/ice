package org.jbei.ice.lib.search;

import java.io.Serializable;

public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String recordId;
    private float score;

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getScore() {
        return score;
    }

    public SearchResult(String recordId, float score) {
        setRecordId(recordId);
        setScore(score);
    }
}

package org.jbei.ice.lib.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import org.jbei.ice.lib.logging.Logger;

import edu.emory.mathcs.backport.java.util.Collections;

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

    public static ArrayList<SearchResult> sort(ArrayList<SearchResult> incoming) {
        class SearchResultComparator implements Comparator<SearchResult> {
            public int compare(SearchResult arg0, SearchResult arg1) {
                float temp = arg1.getScore() - arg0.getScore();
                int result = 0;
                if (temp == 0.0) {
                    result = 0;
                } else if (temp < 0) {
                    result = -1;
                } else if (temp > 0) {
                    result = 1;
                }
                return result;
            }
        }

        SearchResultComparator comparator = new SearchResultComparator();
        Collections.sort(incoming, comparator);

        return incoming;
    }

    public static ArrayList<SearchResult> sumSearchResults(ArrayList<SearchResult> target,
            ArrayList<SearchResult> object) {
        ArrayList<String> targetRecordIds = new ArrayList<String>();
        ArrayList<String> objectRecordIds = new ArrayList<String>();
        for (SearchResult searchResult : target) {
            targetRecordIds.add(searchResult.getRecordId());
        }
        for (SearchResult searchResult : object) {
            objectRecordIds.add(searchResult.getRecordId());
        }
        @SuppressWarnings("unchecked")
        ArrayList<String> tempTargetRecordIds = (ArrayList<String>) targetRecordIds.clone();
        tempTargetRecordIds.retainAll(objectRecordIds);
        if (tempTargetRecordIds.size() == 0) { // intersect is zero. Add object to target
            target.addAll(object);
        } else { // intersect is not zero. Sum the scores of object
            for (String recordId : tempTargetRecordIds) {
                int targetIndex = targetRecordIds.indexOf(recordId);
                int objectIndex = objectRecordIds.indexOf(recordId);
                SearchResult targetResult = target.get(targetIndex);
                SearchResult objectResult = object.get(objectIndex);
                if (targetResult.getRecordId().equals(objectResult.getRecordId())) {
                    targetResult.setScore(targetResult.getScore() + objectResult.getScore());
                } else {
                    String msg = "Algorithm Error in SearchResult.sumSearchResults!";
                    Logger.error(msg);
                }
            }
        }
        return SearchResult.sort(target);
    }

}

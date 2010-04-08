package org.jbei.ice.lib.search.lucene;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Entry;

public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Entry entry;
    private float score;

    public SearchResult() {
        entry = null;
        score = 0;
    }

    public SearchResult(Entry entry, float score) {
        setEntry(entry);
        setScore(score);
    }

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

    protected static ArrayList<SearchResult> sort(ArrayList<SearchResult> incoming) {
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

    /**
     * Add object search results to target, and return the target
     */
    @SuppressWarnings("unchecked")
    protected static ArrayList<SearchResult> sumSearchResults(ArrayList<SearchResult> target,
            ArrayList<SearchResult> object) {
        ArrayList<String> targetRecordIds = new ArrayList<String>();
        ArrayList<String> objectRecordIds = new ArrayList<String>();

        for (SearchResult searchResult : target) {
            targetRecordIds.add(searchResult.getEntry().getRecordId());
        }

        for (SearchResult searchResult : object) {
            objectRecordIds.add(searchResult.getEntry().getRecordId());
        }

        ArrayList<String> intersectiongRecordIds = (ArrayList<String>) targetRecordIds.clone();
        intersectiongRecordIds.retainAll(objectRecordIds);

        if (intersectiongRecordIds.size() == 0) { // intersect is zero. Add object to target
            target.addAll(object);
        } else { // intersect is not zero. Add the score of intersecting objects
            for (String recordId : intersectiongRecordIds) {
                int targetIndex = targetRecordIds.indexOf(recordId);
                int objectIndex = objectRecordIds.indexOf(recordId);

                SearchResult targetResult = target.get(targetIndex);
                SearchResult objectResult = object.get(objectIndex);

                if (targetResult.getEntry().getRecordId().equals(
                    objectResult.getEntry().getRecordId())) {
                    targetResult.setScore(targetResult.getScore() + objectResult.getScore());
                } else {
                    String msg = "Algorithm Error in SearchResult.sumSearchResults!";
                    Logger.error(msg, new Exception("Error"));
                }
            }
            // add the non-intersecting objects
            ArrayList<String> nonIntersectingObjectRecordIds = (ArrayList<String>) objectRecordIds
                    .clone();
            nonIntersectingObjectRecordIds.removeAll(intersectiongRecordIds);
            for (String recordId : nonIntersectingObjectRecordIds) {
                int objectIndex = objectRecordIds.indexOf(recordId);
                target.add(object.get(objectIndex));
            }
        }

        return SearchResult.sort(target);
    }
}

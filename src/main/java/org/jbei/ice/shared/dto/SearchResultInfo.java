package org.jbei.ice.shared.dto;

/**
 * DTO for searches
 *
 * @author Hector Plahar
 */
public class SearchResultInfo extends HasEntryInfo implements Comparable<SearchResultInfo> {

    private float score;

    public SearchResultInfo() {
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public int compareTo(SearchResultInfo searchResultInfo) {
        if (score == searchResultInfo.getScore())
            return 0;

        float diff = score - searchResultInfo.getScore();
        return diff < 0.0f ? -1 : 1;
    }
}

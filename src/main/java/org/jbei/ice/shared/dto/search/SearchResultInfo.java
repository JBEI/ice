package org.jbei.ice.shared.dto.search;

import org.jbei.ice.shared.dto.entry.HasEntryInfo;

/**
 * DTO for searches
 *
 * @author Hector Plahar
 */
public class SearchResultInfo extends HasEntryInfo implements Comparable<SearchResultInfo> {

    public static final long serialVersionUID = 1l;

    private float bitScore;
    private float eValue;
    private int alignmentLength;
    private float percentId;
    private int queryLength;
    private float relativeScore;

    private float score;
    private float maxScore;
    private String webPartnerName;
    private String webPartnerURL;

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

    public float getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(float maxScore) {
        this.maxScore = maxScore;
    }

    public int getAlignmentLength() {
        return alignmentLength;
    }

    public void setAlignmentLength(int alignmentLength) {
        this.alignmentLength = alignmentLength;
    }

    public float getPercentId() {
        return percentId;
    }

    public void setPercentId(float percentId) {
        this.percentId = percentId;
    }

    public float getBitScore() {
        return bitScore;
    }

    public void setBitScore(float bitScore) {
        this.bitScore = bitScore;
    }

    public float geteValue() {
        return eValue;
    }

    public void seteValue(float eValue) {
        this.eValue = eValue;
    }

    public int getQueryLength() {
        return queryLength;
    }

    public void setQueryLength(int queryLength) {
        this.queryLength = queryLength;
    }

    public String getWebPartnerName() {
        return webPartnerName;
    }

    public String getWebPartnerURL() {
        return webPartnerURL;
    }

    public void setWebPartnerURL(String webPartnerURL) {
        this.webPartnerURL = webPartnerURL;
    }

    public void setWebPartnerName(String webPartnerName) {
        this.webPartnerName = webPartnerName;
    }

    public float getRelativeScore() {
        return relativeScore;
    }

    public void setRelativeScore(float relativeScore) {
        this.relativeScore = relativeScore;
    }
}

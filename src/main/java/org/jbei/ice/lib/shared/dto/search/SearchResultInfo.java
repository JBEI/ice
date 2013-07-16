package org.jbei.ice.lib.shared.dto.search;

import java.util.LinkedList;

import org.jbei.ice.lib.shared.dto.entry.HasEntryData;

/**
 * DTO for searches
 *
 * @author Hector Plahar
 */
public class SearchResultInfo extends HasEntryData implements Comparable<SearchResultInfo> {

    public static final long serialVersionUID = 1l;

    private String eValue;
    private String alignment;
    private int queryLength;
    private int alignmentLength;
    private float bitScore;

    private float percentId;
    private float relativeScore;

    private float score;
    private float maxScore;
    private String webPartnerName;
    private String webPartnerURL;
    private LinkedList<String> matchDetails;

    public SearchResultInfo() {
        matchDetails = new LinkedList<String>();
        eValue = "0";
    }

    public float getRelativeScore() {
        return relativeScore;
    }

    public void setRelativeScore(float relativeScore) {
        this.relativeScore = relativeScore;
    }

    public float getPercentId() {
        return percentId;
    }

    public void setPercentId(float percentId) {
        this.percentId = percentId;
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

    public String geteValue() {
        return eValue;
    }

    public void seteValue(String eValue) {
        this.eValue = eValue;
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

    public LinkedList<String> getMatchDetails() {
        return matchDetails;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public int getQueryLength() {
        return queryLength;
    }

    public void setQueryLength(int queryLength) {
        this.queryLength = queryLength;
    }

    public int getAlignmentLength() {
        return alignmentLength;
    }

    public void setAlignmentLength(int alignmentLength) {
        this.alignmentLength = alignmentLength;
    }

    public float getBitScore() {
        return bitScore;
    }

    public void setBitScore(float bitScore) {
        this.bitScore = bitScore;
    }
}

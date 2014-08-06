package org.jbei.ice.lib.dto.search;

import java.util.LinkedList;

import org.jbei.ice.lib.dto.entry.HasEntryData;

/**
 * DTO for searches
 *
 * @author Hector Plahar
 */
public class SearchResult extends HasEntryData {

    public static final long serialVersionUID = 1l;

    private String eValue;
    private String alignment;
    private int queryLength;
    private float score;
    private float maxScore;
    private String webPartnerName;
    private String webPartnerURL;
    private LinkedList<String> matchDetails;

    public SearchResult() {
        matchDetails = new LinkedList<>();
        eValue = "0";
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
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
}

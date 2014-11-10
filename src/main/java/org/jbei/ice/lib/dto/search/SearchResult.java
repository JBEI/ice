package org.jbei.ice.lib.dto.search;

import java.util.LinkedList;

import org.jbei.ice.lib.dto.entry.HasEntryData;
import org.jbei.ice.lib.dto.web.RegistryPartner;

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
    private LinkedList<String> matchDetails;
    private RegistryPartner partner;

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

    public RegistryPartner getPartner() {
        return partner;
    }

    public void setPartner(RegistryPartner partner) {
        this.partner = partner;
    }
}

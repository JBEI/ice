package org.jbei.ice.lib.shared.dto.search;

import java.util.ArrayList;
import java.util.Arrays;

import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.entry.EntryType;

/**
 * @author Hector Plahar
 */
public class SearchQuery implements IDTOModel {

    public static final long serialVersionUID = 1L;

    private String queryString;
    private BlastQuery blastQuery;
    private BioSafetyOption bioSafetyOption;
    private ArrayList<EntryType> entryTypes;
    private Parameters parameters;

    /**
     * set the query default values
     */
    public SearchQuery() {
        entryTypes = new ArrayList<EntryType>(Arrays.asList(EntryType.values()));
        parameters = new Parameters();
    }

    public boolean hasBlastQuery() {
        return blastQuery != null && blastQuery.getSequence() != null && !blastQuery.getSequence().isEmpty();
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public BlastQuery getBlastQuery() {
        return blastQuery;
    }

    public void setBlastQuery(BlastQuery blastQuery) {
        this.blastQuery = blastQuery;
    }

    public BioSafetyOption getBioSafetyOption() {
        return bioSafetyOption;
    }

    public void setBioSafetyOption(BioSafetyOption bioSafetyOption) {
        this.bioSafetyOption = bioSafetyOption;
    }

    public ArrayList<EntryType> getEntryTypes() {
        return entryTypes;
    }

    public void setEntryTypes(ArrayList<EntryType> entryTypes) {
        this.entryTypes = entryTypes;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public static class Parameters implements IDTOModel {

        public static final long serialVersionUID = 1l;

        private ColumnField sortField;
        private boolean sortAscending;
        private int start;
        private int retrieveCount;
        private Boolean hasSequence;
        private Boolean hasAttachment;
        private Boolean hasSample;

        public Parameters() {
            start = 0;
            retrieveCount = 30;
            sortField = ColumnField.RELEVANCE;
            sortAscending = false;
        }

        public Boolean getHasSequence() {
            return hasSequence;
        }

        public void setHasSequence(Boolean hasSequence) {
            this.hasSequence = hasSequence;
        }

        public Boolean getHasAttachment() {
            return hasAttachment;
        }

        public void setHasAttachment(Boolean hasAttachment) {
            this.hasAttachment = hasAttachment;
        }

        public Boolean getHasSample() {
            return hasSample;
        }

        public void setHasSample(Boolean hasSample) {
            this.hasSample = hasSample;
        }

        public ColumnField getSortField() {
            return sortField;
        }

        public void setSortField(ColumnField sortField) {
            this.sortField = sortField;
        }

        public boolean isSortAscending() {
            return sortAscending;
        }

        public void setSortAscending(boolean sortAscending) {
            this.sortAscending = sortAscending;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getRetrieveCount() {
            return retrieveCount;
        }

        public void setRetrieveCount(int retrieveCount) {
            this.retrieveCount = retrieveCount;
        }
    }
}

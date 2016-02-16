package org.jbei.ice.lib.dto.search;

import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class SearchQuery implements IDataTransferModel {

    public static final long serialVersionUID = 1L;

    private String queryString;
    private BlastQuery blastQuery;
    private BioSafetyOption bioSafetyOption;
    private ArrayList<EntryType> entryTypes;
    private Parameters parameters;
    private ArrayList<FieldFilter> fieldFilters;

    /**
     * set the query default values
     */
    public SearchQuery() {
        entryTypes = new ArrayList<>(Arrays.asList(EntryType.values()));
        parameters = new Parameters();
    }

    public boolean hasBlastQuery() {
        return blastQuery != null && blastQuery.getSequence() != null && !blastQuery.getSequence().isEmpty();
    }

    public boolean hasFilter() {
        return parameters != null && (fieldFilters == null || fieldFilters.isEmpty()) &&
                (parameters.getHasAttachment() || parameters.getHasSample() ||
                        parameters.getHasSequence() || bioSafetyOption != null);
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

    public ArrayList<FieldFilter> getFieldFilters() {
        return fieldFilters;
    }

    public void setFieldFilters(ArrayList<FieldFilter> fieldFilters) {
        this.fieldFilters = fieldFilters;
    }

    public void setEntryTypes(List<EntryType> entryTypes) {
        this.entryTypes = new ArrayList<>();
        this.entryTypes.addAll(entryTypes);
    }

    public Parameters getParameters() {
        return parameters;
    }

    public static class Parameters implements IDataTransferModel {

        public static final long serialVersionUID = 1l;

        private ColumnField sortField;
        private boolean sortAscending;
        private int start;
        private int retrieveCount;
        private boolean hasSequence;
        private boolean hasAttachment;
        private boolean hasSample;

        public Parameters() {
            start = 0;
            retrieveCount = 15;
            sortField = ColumnField.RELEVANCE;
            sortAscending = false;
        }

        public boolean getHasSequence() {
            return hasSequence;
        }

        public void setHasSequence(boolean hasSequence) {
            this.hasSequence = hasSequence;
        }

        public boolean getHasAttachment() {
            return hasAttachment;
        }

        public void setHasAttachment(boolean hasAttachment) {
            this.hasAttachment = hasAttachment;
        }

        public boolean getHasSample() {
            return hasSample;
        }

        public void setHasSample(boolean hasSample) {
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

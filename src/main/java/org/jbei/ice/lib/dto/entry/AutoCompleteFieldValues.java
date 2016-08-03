package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Values for auto complete fields
 *
 * @author Hector Plahar
 */
public class AutoCompleteFieldValues {

    private final AutoCompleteField autoCompleteField;
    private final EntryDAO entryDAO;

    /**
     * Each instance of this object is tied to a specified field
     *
     * @param field user specified field
     */
    public AutoCompleteFieldValues(String field) {
        this.autoCompleteField = AutoCompleteField.valueOf(field);
        this.entryDAO = DAOFactory.getEntryDAO();
    }

    /**
     * Retrieves list of values that match the user specified token up to the specified limit
     *
     * @param token token to match values against
     * @param limit maximum number of matching values to return
     * @return list of matching values
     */
    public List<String> getMatchingValues(String token, int limit) {
        if (token.isEmpty())
            return new ArrayList<>();

        token = token.replaceAll("'", "");
        List<String> results;
        switch (this.autoCompleteField) {
            case SELECTION_MARKERS:
                results = entryDAO.getMatchingSelectionMarkers(token, limit);
                break;

            case ORIGIN_OF_REPLICATION:
            case PROMOTERS:
            case REPLICATES_IN:
                results = entryDAO.getMatchingPlasmidField(this.autoCompleteField, token, limit);
                break;

            case PART_NUMBER:
                results = entryDAO.getMatchingEntryPartNumbers(token, limit, null);
                break;

            default:
                return new ArrayList<>();
        }

        // process to remove commas
        HashSet<String> individualResults = new HashSet<>();
        for (String result : results) {
            for (String split : result.split(",")) {
                if (!split.contains(token))
                    continue;
                individualResults.add(split.trim());
            }
        }
        return new ArrayList<>(individualResults);
    }
}

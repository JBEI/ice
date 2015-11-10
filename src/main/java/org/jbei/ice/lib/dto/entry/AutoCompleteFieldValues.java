package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Values for auto complete fields
 *
 * @author Hector Plahar
 */
public class AutoCompleteFieldValues {

    public final AutoCompleteField autoCompleteField;
    private final EntryDAO entryDAO;


    public AutoCompleteFieldValues(String field) {
        this.autoCompleteField = AutoCompleteField.valueOf(field);
        this.entryDAO = DAOFactory.getEntryDAO();
    }

    public ArrayList<String> getMatchingValues(String token, int limit) {
        token = token.replaceAll("'", "");
        Set<String> results;
        switch (this.autoCompleteField) {
            case SELECTION_MARKERS:
                results = entryDAO.getMatchingSelectionMarkers(token, limit);
                break;

            case ORIGIN_OF_REPLICATION:
                results = entryDAO.getMatchingOriginOfReplication(token, limit);
                break;

            case PROMOTERS:
                results = entryDAO.getMatchingPromoters(token, limit);
                break;

            case REPLICATES_IN:
                results = entryDAO.getMatchingReplicatesIn(token, limit);
                break;

            case PART_NUMBER:
                results = entryDAO.getMatchingEntryPartNumbers(token, limit, null);
                break;

            default:
                results = new HashSet<>();
        }

        // process to remove commas
        HashSet<String> individualResults = new HashSet<>();
        for (String result : results) {
            for (String split : result.split(",")) {
                individualResults.add(split.trim());
            }
        }
        return new ArrayList<>(individualResults);
    }
}

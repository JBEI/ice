package org.jbei.ice.lib.entry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.servlet.ModelToInfoFactory;

/**
 * @author Hector Plahar
 */
public class EntryRetriever {

    private final EntryDAO dao;
    private final EntryAuthorization authorization;

    public EntryRetriever() {
        this.dao = DAOFactory.getEntryDAO();
        authorization = new EntryAuthorization();
    }

    public Set<String> getMatchingAutoCompleteField(AutoCompleteField field, String token, int limit) {
        token = token.replaceAll("'", "");
        Set<String> results;
        switch (field) {
            case SELECTION_MARKERS:
                results = dao.getMatchingSelectionMarkers(token, limit);
                break;

            case ORIGIN_OF_REPLICATION:
                results = dao.getMatchingOriginOfReplication(token, limit);
                break;

            case PROMOTERS:
                results = dao.getMatchingPromoters(token, limit);
                break;

            case REPLICATES_IN:
                results = dao.getMatchingReplicatesIn(token, limit);
                break;

            case PLASMID_NAME:
                results = dao.getMatchingPlasmidPartNumbers(token, limit);
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
        return individualResults;
    }

    /**
     * Retrieve {@link Entry} from the database by id.
     *
     * @param account account of user performing action
     * @param id      unique local identifier for entry
     * @return entry retrieved from the database.
     * @throws ControllerException
     */
    public Entry get(Account account, long id) throws ControllerException {
        Entry entry = dao.get(id);
        if (entry == null)
            return null;

        authorization.expectRead(account.getEmail(), entry);

        // get reverse for linked entries
        entry.getLinkedEntries().addAll(dao.getReverseLinkedEntries(id));
        return entry;
    }

    /**
     * Retrieves the IDs of all part records in the system
     *
     * @return list of ids
     * @throws ControllerException on DAOException retrieving the IDs
     */
    public LinkedList<Long> getAllEntryIds() {
        return dao.getAllEntryIds();
    }

    public String getEntrySummary(long id) {
        return dao.getEntrySummary(id);
    }

    /*
    * Generate the next part number string using system settings.
    *
    * @return The next part number.
    */


    /**
     * Retrieve {@link org.jbei.ice.lib.entry.model.Entry} from the database by recordId (uuid).
     *
     * @param recordId universally unique identifier that was assigned to entry on create
     * @return entry retrieved from the database.
     * @throws ControllerException
     */
    public Entry getByRecordId(Account account, String recordId) throws ControllerException {
        Entry entry = dao.getByRecordId(recordId);
        if (entry == null)
            return null;

//        authorization.expectRead(account.getEmail(), entry);
        return entry;
    }

    public PartData getPartByRecordId(Account account, String recordId) throws ControllerException {
        Entry entry;

        try {
            entry = dao.getByRecordId(recordId);
            if (entry == null)
                return null;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

//        authorization.expectRead(account.getEmail(), entry);

        PartData info = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = DAOFactory.getSequenceDAO().hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = DAOFactory.getSequenceDAO().hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

}

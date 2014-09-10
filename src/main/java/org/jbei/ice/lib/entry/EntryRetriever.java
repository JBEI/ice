package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.lib.access.Permission;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.utils.IceCSVSerializer;

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

    public String getListAsCSV(String userId, ArrayList<Long> list) {  // todo : use a file for large lists
        if (list == null || list.isEmpty() || userId.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder();
        for (Number item : list) {
            Entry entry = this.dao.get(item.longValue());
            if (entry == null || !authorization.canRead(userId, entry))
                continue;

            builder.append(IceCSVSerializer.serialize(entry)).append('\n');
        }

        return builder.toString();
    }

    public String getAsCSV(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);
        return IceCSVSerializer.serialize(entry);
    }

    public String getPartNumber(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);
        return entry.getPartNumber();
    }

    protected Entry getEntry(String id) {
        Entry entry = null;

        // check if numeric
        try {
            entry = dao.get(Long.decode(id));
        } catch (NumberFormatException nfe) {
            // fine to ignore
        }

        // check for part Id
        if (entry == null)
            entry = dao.getByPartNumber(id);

        // check for global unique id
        if (entry == null)
            entry = dao.getByRecordId(id);

        return entry;
    }

    public ArrayList<AccessPermission> getEntryPermissions(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        // viewing permissions requires write permissions
        authorization.expectWrite(userId, entry);

        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();
        Set<Permission> permissions = DAOFactory.getPermissionDAO().getEntryPermissions(entry);

        GroupController groupController = new GroupController();
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        for (Permission permission : permissions) {
            if (permission.getAccount() == null && permission.getGroup() == null)
                continue;
            if (permission.getGroup() != null && permission.getGroup() == publicGroup)
                continue;
            accessPermissions.add(permission.toDataTransferObject());
        }

        return accessPermissions;
    }

    // return list of part data with only partId and id filled in
    public ArrayList<PartData> getMatchingPartNumber(String token, int limit) {
        if (token == null)
            return new ArrayList<>();

        token = token.replaceAll("'", "");
        ArrayList<PartData> dataList = new ArrayList<>();
        for (Entry entry : dao.getMatchingEntryPartNumbers(token, limit)) {
            EntryType type = EntryType.nameToType(entry.getRecordType());
            PartData partData = new PartData(type);
            partData.setId(entry.getId());
            partData.setPartId(entry.getPartNumber());
            dataList.add(partData);
        }
        return dataList;
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

            case PLASMID_PART_NUMBER:
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
     * @param userId account identifier of user performing action
     * @param id     unique local identifier for entry
     * @return entry retrieved from the database.
     */
    public Entry get(String userId, long id) {
        Entry entry = dao.get(id);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);
        return entry;
    }

    public String getEntrySummary(long id) {
        return dao.getEntrySummary(id);
    }

    /**
     * Retrieve {@link org.jbei.ice.lib.entry.model.Entry} from the database by recordId (uuid).
     *
     * @param recordId universally unique identifier that was assigned to entry on create
     * @return entry retrieved from the database.
     */
    public Entry getByRecordId(String userId, String recordId) {
        Entry entry = getEntry(recordId);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);
        return entry;
    }

//    public PartData getPartByRecordId(Account account, String recordId) throws ControllerException {
//        Entry entry;
//
//        try {
//            entry = dao.getByRecordId(recordId);
//            if (entry == null)
//                return null;
//        } catch (DAOException e) {
//            throw new ControllerException(e);
//        }
//
////        authorization.expectRead(account.getEmail(), entry);
//
//        PartData info = ModelToInfoFactory.getInfo(entry);
//        boolean hasSequence = DAOFactory.getSequenceDAO().hasSequence(entry.getId());
//        info.setHasSequence(hasSequence);
//        boolean hasOriginalSequence = DAOFactory.getSequenceDAO().hasOriginalSequence(entry.getId());
//        info.setHasOriginalSequence(hasOriginalSequence);
//        return info;
//    }
}

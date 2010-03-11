package org.jbei.ice.lib.permissions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.query.SortField;

public class AuthenticatedEntryManager {
    public static Entry createEntry(Entry entry) throws PermissionException, ManagerException {
        Entry result = null;

        if (entry == null) {
            result = null;
        } else if (entry instanceof Plasmid) {
            result = createPlasmid((Plasmid) entry);
        } else if (entry instanceof Strain) {
            result = createStrain((Strain) entry);
        } else if (entry instanceof Part) {
            result = createPart((Part) entry);
        }

        return result;
    }

    public static Plasmid createPlasmid(Plasmid newPlasmid) throws PermissionException,
            ManagerException {
        // Creating new doesn't need permission. Method exists for completeness.
        return EntryManager.createPlasmid(newPlasmid);
    }

    public static Strain createStrain(Strain newStrain) throws PermissionException,
            ManagerException {
        return EntryManager.createStrain(newStrain);
    }

    public static Part createPart(Part newPart) throws PermissionException, ManagerException {
        return EntryManager.createPart(newPart);
    }

    public static void remove(Entry entry) throws PermissionException, ManagerException {
        if (PermissionManager.hasWritePermission(entry.getId())) {
            EntryManager.remove(entry);
        } else {
            throw new PermissionException("Remove not permitted");
        }
    }

    public static Entry get(int id) throws PermissionException, ManagerException {
        Entry result = null;
        if (PermissionManager.hasReadPermission(id)) {
            result = EntryManager.get(id);
        } else {
            throw new PermissionException("Get not permitted");
        }
        return result;
    }

    public static Entry getByRecordId(String recordId) throws PermissionException, ManagerException {
        Entry result = EntryManager.getByRecordId(recordId);
        if (result == null || !PermissionManager.hasReadPermission(result.getId())) {
            throw new PermissionException("Get not permitted");
        }
        return result;
    }

    public static Entry getByPartNumber(String partNumber) throws PermissionException,
            ManagerException {
        Entry result = EntryManager.getByPartNumber(partNumber);
        if (result == null || !PermissionManager.hasReadPermission(result.getId())) {
            throw new PermissionException("Get not permitted");
        }
        return result;
    }

    public static Entry getByName(String name) throws PermissionException, ManagerException {
        Entry result = EntryManager.getByName(name);
        if (result == null || !PermissionManager.hasReadPermission(result.getId())) {
            throw new PermissionException("Get not permitted");
        }
        return result;
    }

    public static Set<Entry> getByFilter(ArrayList<String[]> data, int offset, int limit,
            String sessionKey) throws PermissionException, ManagerException {
        LinkedHashSet<Entry> result = (LinkedHashSet<Entry>) EntryManager.getByFilter(data, offset,
                limit);
        result = cleanReadSet(result);
        return result;
    }

    public static LinkedHashSet<Entry> getByAccount(Account account, int offset, int limit,
            String sessionKey) throws ManagerException {
        LinkedHashSet<Entry> result = EntryManager.getByAccount(account, offset, limit);
        result = cleanReadSet(result);
        return result;
    }

    public static LinkedHashSet<Entry> getByAccountVisible(Account account, int offset, int limit,
            SortField[] sortedFields) throws ManagerException {
        LinkedHashSet<Entry> intermediate = EntryManager.getByAccount(account, 0, 1000,
                sortedFields);
        intermediate = cleanReadSet(intermediate);
        // TODO: Fix big ugly slow way
        ArrayList<Entry> temp = new ArrayList<Entry>(intermediate);
        LinkedHashSet<Entry> result = new LinkedHashSet<Entry>(temp.subList(offset, offset + limit));
        return result;
    }

    public static int getByAccountVisibleCount(Account account) {
        LinkedHashSet<Entry> result = EntryManager.getByAccount(account, 0, 3000);
        result = cleanReadSet(result);
        return result.size();
    }

    public static Entry save(Entry entry) throws ManagerException, PermissionException {
        Entry result = null;
        if (PermissionManager.hasWritePermission(entry.getId())) {
            result = EntryManager.save(entry);
        } else {
            throw new PermissionException("Save not permitted");
        }
        return result;
    }

    private static LinkedHashSet<Entry> cleanReadSet(LinkedHashSet<Entry> inEntries) {
        LinkedHashSet<Entry> result = new LinkedHashSet<Entry>();
        for (Entry entry : inEntries) {
            if (PermissionManager.hasReadPermission(entry.getId())) {
                result.add(entry);
            }
        }
        return result;
    }

}

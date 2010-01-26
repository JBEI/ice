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

public class AuthenticatedEntryManager {

    public static Plasmid createPlasmid(Plasmid newPlasmid, String sessionKey)
            throws PermissionException, ManagerException {
        // Creating new doesn't need permission. Method exists for completeness.
        return EntryManager.createPlasmid(newPlasmid);
    }

    public static Strain createStrain(Strain newStrain, String sessionKey)
            throws PermissionException, ManagerException {
        return EntryManager.createStrain(newStrain);
    }

    public static Part createPart(Part newPart, String sessionKey) throws PermissionException,
            ManagerException {
        return EntryManager.createPart(newPart);
    }

    public static void remove(Entry entry, String sessionKey) throws PermissionException,
            ManagerException {
        if (PermissionManager.hasWritePermission(entry.getId(), sessionKey)) {
            EntryManager.remove(entry);
        } else {
            throw new PermissionException("Remove not permitted");
        }
    }

    public static Entry get(int id, String sessionKey) throws PermissionException, ManagerException {
        Entry result = null;
        if (PermissionManager.hasReadPermission(id, sessionKey)) {
            result = EntryManager.get(id);
        } else {
            throw new PermissionException("Get not permitted");
        }
        return result;
    }

    public static Entry getByRecordId(String recordId, String sessionKey)
            throws PermissionException, ManagerException {
        Entry result = null;
        result = EntryManager.getByRecordId(recordId);
        if (PermissionManager.hasReadPermission(result.getId(), sessionKey)) {
        } else {
            throw new PermissionException("Get not permitted");
        }
        return result;
    }

    public static Entry getByPartNumber(String partNumber, String sessionKey)
            throws PermissionException, ManagerException {
        Entry result = null;
        result = EntryManager.getByPartNumber(partNumber);
        if (PermissionManager.hasReadPermission(result.getId(), sessionKey)) {
        } else {
            throw new PermissionException("Get not permitted");
        }
        return result;
    }

    public static Entry getByName(String name, String sessionKey) throws PermissionException,
            ManagerException {
        Entry result = null;
        result = EntryManager.getByName(name);
        if (PermissionManager.hasReadPermission(result.getId(), sessionKey)) {
        } else {
            throw new PermissionException("Get not permitted");
        }
        return result;
    }

    public static Set<Entry> getByFilter(ArrayList<String[]> data, int offset, int limit,
            String sessionKey) throws PermissionException, ManagerException {
        LinkedHashSet<Entry> result = (LinkedHashSet<Entry>) EntryManager.getByFilter(data, offset,
                limit);
        cleanReadSet(result, sessionKey);
        return result;
    }

    public static LinkedHashSet<Entry> getByAccount(Account account, int offset, int limit,
            String sessionKey) throws ManagerException {
        LinkedHashSet<Entry> result = EntryManager.getByAccount(account, offset, limit);
        cleanReadSet(result, sessionKey);
        return result;
    }

    public static Entry save(Entry entry, String sessionKey) throws ManagerException,
            PermissionException {
        Entry result = null;
        if (PermissionManager.hasWritePermission(entry.getId(), sessionKey)) {
            result = EntryManager.save(entry);
        } else {
            throw new PermissionException("Get not permitted");
        }
        return result;
    }

    private static void cleanReadSet(LinkedHashSet<Entry> result, String sessionKey) {
        for (Entry entry : result) {
            if (!PermissionManager.hasReadPermission(entry.getId(), sessionKey)) {
                result.remove(entry);
            }
        }
    }

}

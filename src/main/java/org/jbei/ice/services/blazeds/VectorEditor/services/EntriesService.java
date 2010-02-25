package org.jbei.ice.services.blazeds.VectorEditor.services;

import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionManager;

public class EntriesService {
    public Entry getEntry(String authToken, String entryId) {
        Account account = AccountManager.getAccountByAuthToken(authToken);

        if (account == null) {
            return null;
        }

        Entry entry = null;
        try {
            entry = EntryManager.getByRecordId(entryId);

            if (PermissionManager.hasReadPermission(entry)) {
                return entry;
            }
        } catch (ManagerException e) {
            e.printStackTrace();

            return null;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

        return entry;
    }
}

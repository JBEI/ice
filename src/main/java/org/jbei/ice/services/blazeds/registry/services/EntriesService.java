package org.jbei.ice.services.blazeds.registry.services;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.services.blazeds.common.BaseService;

public class EntriesService extends BaseService {
    public final static String ENTRIES_SERVICE_NAME = "EntriesService";

    public Entry getEntry(String authToken, String entryId) {
        Account account = getAccountByToken(authToken);

        if (account == null) {
            return null;
        }

        Entry entry = null;
        try {
            entry = EntryManager.getByRecordId(entryId);

            if (PermissionManager.hasReadPermission(entry, account)) {
                return entry;
            } else {
                Logger.warn(getLoggerPrefix() + "User " + account.getFullName()
                        + " tried to access entry without permissions.");
            }
        } catch (ManagerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return entry;
    }

    public boolean hasWritablePermissions(String authToken, String entryId) {
        boolean result = false;

        Account account = getAccountByToken(authToken);

        if (account == null) {
            return result;
        }

        try {
            Entry entry = EntryManager.getByRecordId(entryId);

            if (entry != null) {
                result = PermissionManager.hasWritePermission(entry, account);
            }
        } catch (ManagerException e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        }

        return result;
    }

    public boolean saveEntry(String authToken, Entry entry) {
        boolean result = false;

        Account account = getAccountByToken(authToken);

        if (account == null) {
            return result;
        }

        try {
            EntryManager.save(entry);

            Logger.info(getLoggerPrefix() + "Entry saved via " + getServiceName());

            result = true;
        } catch (ManagerException e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        }

        return result;
    }

    @Override
    protected String getServiceName() {
        return ENTRIES_SERVICE_NAME;
    }
}

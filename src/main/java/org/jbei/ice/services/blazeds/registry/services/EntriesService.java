package org.jbei.ice.services.blazeds.registry.services;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.services.blazeds.common.BaseService;

public class EntriesService extends BaseService {
    public final static String ENTRIES_SERVICE_NAME = "EntriesService";

    public Entry getEntry(String authToken, String entryId) {
        Account account = getAccountByToken(authToken);

        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController(account);

        Entry entry = null;
        try {
            entry = entryController.getByRecordId(entryId);
        } catch (ControllerException e) {
            Logger.error("Failed to get entry!", e);

            return null;
        } catch (PermissionException e) {
            Logger.warn(getLoggerPrefix() + "User " + account.getFullName()
                    + " tried to access entry without permissions.");

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

        EntryController entryController = new EntryController(account);

        try {
            Entry entry = entryController.getByRecordId(entryId);

            if (entry != null) {
                result = entryController.hasWritePermission(entry);
            }
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        } catch (PermissionException e) {
            Logger.warn(getLoggerPrefix() + "User " + account.getFullName()
                    + " tried to access entry without permissions.");

            return false;
        }

        return result;
    }

    public boolean saveEntry(String authToken, Entry entry) {
        boolean result = false;

        Account account = getAccountByToken(authToken);

        if (account == null) {
            return result;
        }

        EntryController entryController = new EntryController(account);

        try {
            entryController.save(entry);

            Logger.info(getLoggerPrefix() + "Entry saved via " + getServiceName());

            result = true;
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        } catch (PermissionException e) {
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

package org.jbei.ice.services.webservices;

import java.util.ArrayList;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;

public class EntryService extends JBEIService {
    public EntryService() {
    }

    public boolean hasReadPermissions(String sessionId, String entryId) throws SessionException,
            ServiceException, PermissionException {
        boolean result = false;

        EntryController entryController = getEntryController(sessionId);

        try {
            Entry entry = entryController.getByRecordId(entryId);

            result = entryController.hasReadPermission(entry);
        } catch (ControllerException e) {
            throw new ServiceException(e);
        }

        return result;
    }

    public boolean hasWritePermissions(String sessionId, String entryId) throws SessionException,
            ServiceException, PermissionException {
        boolean result = false;

        EntryController entryController = getEntryController(sessionId);

        try {
            Entry entry = entryController.getByRecordId(entryId);

            result = entryController.hasWritePermission(entry);
        } catch (ControllerException e) {
            throw new ServiceException(e);
        }

        return result;
    }

    public Entry saveOrUpdate(String sessionId, Entry entry) {
        return null;
    }

    public void remove(String sessionId, String entryId) throws SessionException, ServiceException,
            PermissionException {
        EntryController entryController = getEntryController(sessionId);

        try {
            Entry entry = entryController.getByRecordId(entryId);

            entryController.delete(entry);
        } catch (ControllerException e) {
            throw new ServiceException(e);
        }
    }

    public Entry getByRecordId(String sessionId, String entryId) throws SessionException,
            ServiceException, PermissionException {
        EntryController entryController = getEntryController(sessionId);

        Entry entry = null;

        try {
            entry = entryController.getByRecordId(entryId);
        } catch (ControllerException e) {
            throw new ServiceException(e);
        }

        return entry;
    }

    public Entry getByPartNumber(String sessionId, String partNumber) throws SessionException,
            ServiceException, PermissionException {
        EntryController entryController = getEntryController(sessionId);

        Entry entry = null;

        try {
            entry = entryController.getByPartNumber(partNumber);
        } catch (ControllerException e) {
            throw new ServiceException(e);
        }

        return entry;
    }

    public boolean existEntry(String sessionId, String entryId) {
        return false;
    }

    public int getNumberOfSamples(String sessionId, String entryId) throws SessionException,
            ServiceException, PermissionException {
        return 0;
    }

    public ArrayList<Sample> getSamples(String sessionId, String entryId) {
        return null;
    }

    public boolean hasSequence(String sessionId, String entryId) {
        return false;
    }

    public Sequence getSequence(String sessionId, String entryId) {
        return null;
    }

    public int getNumberOfAttachments(String sessionId, String entryId) {
        return 0;
    }

    public ArrayList<Attachment> getAttachments(String sessionId, String entryId) {
        return null;
    }

    public int getNumberOfTraceSequences(String sessionId, String entryId) {
        return 0;
    }

    public ArrayList<TraceSequence> getTraceSequences(String sessionId, String entryId) {
        return null;
    }

    protected EntryController getEntryController(String sessionId) throws SessionException {
        Account account = validateAccount(sessionId);

        return new EntryController(account);
    }
}
package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.Calendar;

import org.jbei.ice.ApplicationController;
import org.jbei.ice.lib.access.Permission;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.PermissionDAO;
import org.jbei.ice.lib.dao.hibernate.SequenceDAO;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.servlet.InfoToModelFactory;

/**
 * @author Hector Plahar
 */
public class EntryCreator {

    private final EntryDAO dao;
    private final PermissionDAO permissionDAO;
    private final SequenceDAO sequenceDAO;

    public EntryCreator() {
        dao = DAOFactory.getEntryDAO();
        permissionDAO = DAOFactory.getPermissionDAO();
        sequenceDAO = DAOFactory.getSequenceDAO();
    }

    /**
     * creates entry and assigns read permissions to all public groups that user creating the entry is a member of
     *
     * @param account account for user creating entry
     * @param entry   entry being created
     * @return created entry
     */
    public Entry createEntry(Account account, Entry entry) {
        PermissionsController permissionsController = new PermissionsController();
        ArrayList<AccessPermission> accessPermissions = permissionsController.getDefaultPermissions(account);
        return createEntry(account, entry, accessPermissions);
    }

    /**
     * Create an entry in the database.
     * <p/>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps.
     * Optionally set the record globally visible or schedule an index rebuild.
     *
     * @param account           account of user creating entry
     * @param entry             entry record being created
     * @param accessPermissions list of permissions to associate with created entry
     * @return entry that was saved in the database.
     */
    public Entry createEntry(Account account, Entry entry, ArrayList<AccessPermission> accessPermissions) {
        entry.setPartNumber(EntryUtil.getNextPartNumber());
        if (entry.getRecordId() == null) {
            entry.setRecordId(Utils.generateUUID());
            entry.setVersionId(entry.getRecordId());
        }
        entry.setCreationTime(Calendar.getInstance().getTime());
        entry.setModificationTime(entry.getCreationTime());
        entry.setOwner(account.getFullName());
        entry.setOwnerEmail(account.getEmail());

        if (entry.getSelectionMarkers() != null) {
            for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
                selectionMarker.setEntry(entry);
            }
        }

        if (entry.getLinks() != null) {
            for (Link link : entry.getLinks()) {
                link.setEntry(entry);
            }
        }

        if (entry.getStatus() == null)
            entry.setStatus("");

        if (entry.getBioSafetyLevel() == null)
            entry.setBioSafetyLevel(0);

        entry = dao.create(entry);

        // add write permissions for owner
        Permission permission = new Permission();
        permission.setCanWrite(true);
        permission.setEntry(entry);
        permission.setAccount(account);
        permissionDAO.create(permission);

        // add read permission for all public groups
        ArrayList<Group> groups = new GroupController().getAllPublicGroupsForAccount(account);
        for (Group group : groups) {
            Permission groupPermission = new Permission();
            groupPermission.setGroup(group);
            groupPermission.setEntry(entry);
            groupPermission.setCanRead(true);
            permissionDAO.create(groupPermission);
        }

        if (accessPermissions != null) {
            for (AccessPermission accessPermission : accessPermissions) {
                if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT) {
                    // TODO
                    // add account permission

                } else {
                    // add group permission
                }
            }
        }

        // rebuild blast database
        if (sequenceDAO.hasSequence(entry.getId())) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }

        return entry;
    }

    public long createPart(String userId, PartData part) {
        Entry entry = InfoToModelFactory.infoToEntry(part);
        EntryAuthorization authorization = new EntryAuthorization();
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        // linked entries can be a combination of new and existing parts
        if (part.getLinkedParts() != null && part.getLinkedParts().size() > 0) {
            for (PartData data : part.getLinkedParts()) {
                Entry linked;
                if (data.getId() > 0) {
                    linked = dao.get(data.getId());
                    if (linked == null)
                        continue;

                    if (!authorization.canRead(userId, linked)) {
                        continue;
                    }

                    // TODO : may contain new information e.g. if the sequence is uploaded before
                    // TODO : this entry was created then the general information is added here
                    if (authorization.canWrite(userId, linked)) {
                        // then update
                    }
                } else {
                    // create new linked (can only do one deep)
                    Entry linkedEntry = InfoToModelFactory.infoToEntry(data);
                    linked = createEntry(account, linkedEntry, data.getAccessPermissions());
                }

                entry.getLinkedEntries().add(linked);
            }
        }

        entry = createEntry(account, entry, part.getAccessPermissions());
        return entry.getId();
    }
}

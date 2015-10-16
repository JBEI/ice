package org.jbei.ice.lib.entry;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.servlet.InfoToModelFactory;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.Calendar;

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
     * Create an entry in the database.
     * <p>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps.
     * Optionally set the record globally visible or schedule an index rebuild.
     *
     * @param account           account of user creating entry
     * @param entry             entry record being created
     * @param accessPermissions list of permissions to associate with created entry
     * @return entry that was saved in the database.
     */
    public Entry createEntry(Account account, Entry entry, ArrayList<AccessPermission> accessPermissions) {
        if (entry.getRecordId() == null) {
            entry.setRecordId(Utils.generateUUID());
            entry.setVersionId(entry.getRecordId());
        }
        entry.setCreationTime(Calendar.getInstance().getTime());
        entry.setModificationTime(entry.getCreationTime());

        if (StringUtils.isEmpty(entry.getOwner()))
            entry.setOwner(account.getFullName());

        if (StringUtils.isEmpty(entry.getOwnerEmail()))
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

        // check for pi
        String piEmail = entry.getPrincipalInvestigatorEmail();
        if (StringUtils.isNotEmpty(piEmail)) {
            Account pi = DAOFactory.getAccountDAO().getByEmail(piEmail);
            if (pi != null) {
                // add write permission for the PI
                addWritePermission(pi, entry);
            }
        }

        // add write permissions for owner
        addWritePermission(account, entry);

        // add read permission for all public groups
        ArrayList<Group> groups = new GroupController().getAllPublicGroupsForAccount(account);
        for (Group group : groups) {
            addReadPermission(null, group, entry);
        }

        if (accessPermissions != null) {
            for (AccessPermission accessPermission : accessPermissions) {
                if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT) {
                    Account accessAccount = DAOFactory.getAccountDAO().get(accessPermission.getArticleId());
                    // add account read permission
                    addReadPermission(accessAccount, null, entry);
                } else {
                    // add group read permission
                    Group group = DAOFactory.getGroupDAO().get(accessPermission.getArticleId());
                    addReadPermission(null, group, entry);
                }
            }
        }

        // rebuild blast database
        if (sequenceDAO.hasSequence(entry.getId())) {
            BlastPlus.scheduleBlastIndexRebuildTask(true);
        }

        return entry;
    }

    protected void addReadPermission(Account account, Group group, Entry entry) {
        Permission permission = new Permission();
        if (group != null)
            permission.setGroup(group);
        else if (account != null)
            permission.setAccount(account);
        else
            return; // either group or account required

        permission.setEntry(entry);
        entry.getPermissions().add(permission); // triggers the permission class bridge
        permission.setCanRead(true);
        permissionDAO.create(permission);
    }

    protected void addWritePermission(Account account, Entry entry) {
        Permission permission = new Permission();
        permission.setCanWrite(true);
        permission.setEntry(entry);
        entry.getPermissions().add(permission); // triggers the permission class bridge
        permission.setAccount(account);
        permissionDAO.create(permission);
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
                    linked = InfoToModelFactory.updateEntryField(data, linked);

                    linked.setVisibility(Visibility.OK.getValue());

                    if (authorization.canWriteThoroughCheck(userId, linked)) {
                        // then update
                    }
                } else {
                    // create new linked (can only do one deep)
                    Entry linkedEntry = InfoToModelFactory.infoToEntry(data);
                    linked = createEntry(account, linkedEntry, data.getAccessPermissions());
                }

                if (entry != null)
                    entry.getLinkedEntries().add(linked);
            }
        }

        entry = createEntry(account, entry, part.getAccessPermissions());
        return entry.getId();
    }
}

package org.jbei.ice.lib.folder;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.hibernate.dao.RemoteAccessModelDAO;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ICE Folders
 *
 * @author Hector Plahar
 */
public class Folders {

    private final FolderDAO dao;
    private final String userId;
    private final RemoteAccessModelDAO remoteAccessModelDAO;
    private final AccountDAO accountDAO;
    private final FolderAuthorization authorization;

    public Folders(String userId) {
        this.dao = DAOFactory.getFolderDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.userId = userId;
        this.remoteAccessModelDAO = DAOFactory.getRemoteAccessModelDAO();
        this.authorization = new FolderAuthorization();
    }

    private Set<Group> getAccountGroups(Account account) {
        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        return accountGroups;
    }

    /**
     * Retrieves list of folders that specified user has write privileges on
     *
     * @return list of folders
     */
    public List<FolderDetails> getCanEditFolders() {
        Account account = this.accountDAO.getByEmail(userId);
        Set<Group> accountGroups = getAccountGroups(account);
        List<Folder> folders = dao.getCanEditFolders(account, accountGroups);
        folders.addAll(dao.getFoldersByOwner(account));
        ArrayList<FolderDetails> result = new ArrayList<>();

        for (Folder folder : folders) {
            FolderDetails details = folder.toDataTransferObject();
            if (folder.getType() == FolderType.REMOTE) {
                RemoteAccessModel model = remoteAccessModelDAO.getByFolder(account, folder);
                if (model == null) {
                    result.add(details);
                    continue;
                }

                AccountTransfer owner = new AccountTransfer();
                owner.setEmail(model.getRemoteClientModel().getEmail());
                details.setOwner(owner);
                RemotePartner remotePartner = model.getRemoteClientModel().getRemotePartner();
                details.setRemotePartner(remotePartner.toDataTransferObject());
            }

            result.add(details);
        }

        return result;
    }

    public Set<String> getCanReadFolderIds() {
        Account account = this.accountDAO.getByEmail(userId);
        Set<Group> accountGroups = getAccountGroups(account);
        Set<String> idStrings = new HashSet<>();
        List<Long> folderIds = dao.getCanReadFolderIds(account, accountGroups);
        if (folderIds.isEmpty())
            return idStrings;

        idStrings.addAll(folderIds.stream().map(Object::toString).collect(Collectors.toList()));
        return idStrings;
    }

    public List<FolderDetails> filter(String token, int limit) {
        List<Folder> list = dao.filterByName(token, limit);
        return list.stream().map(Folder::toDataTransferObject).collect(Collectors.toList());
    }

    public boolean updateFolderType(long folderId, FolderType type) {
        Folder folder = dao.get(folderId);
        if (folder == null)
            throw new IllegalArgumentException("No folder exists with id " + folderId);

        authorization.expectWrite(this.userId, folder);
        if (folder.getType() == type)
            return true;

        if (type != FolderType.SAMPLE)
            throw new IllegalArgumentException("Can only set to SAMPLE type for now");

        if (!validateFolderForSamples(folder))
            return false;

        folder.setType(type);
        return dao.update(folder) != null;
    }

    private boolean validateFolderForSamples(Folder folder) {
        List<Long> folderContents = dao.getEntryIds(folder);
        if (folderContents.isEmpty())
            return true;

        EntryDAO entryDAO = DAOFactory.getEntryDAO();

        for (long entryId : folderContents) {
            Entry entry = entryDAO.get(entryId);
            if (StringUtils.isEmpty(entry.getIntellectualProperty())) {
                Logger.info(entry.getPartNumber() + " is missing intellectual property");
                return false;
            }

            if (!isValidEntry(entry))
                return false;
        }

        return true;
    }

    /**
     * Checks if the specified entry matches pre-approved (read: hardcoded) specs for sample requests
     *
     * @param entry entry to check
     * @return true, if entry is approved for sampling, false otherwise
     */
    private boolean isValidEntry(Entry entry) {
        if (EntryType.PLASMID.getName().equalsIgnoreCase(entry.getRecordType())) {
            Plasmid plasmid = (Plasmid) entry;
            if (StringUtils.isEmpty(plasmid.getOriginOfReplication())) {
                Logger.info(entry.getPartNumber() + " is missing origin of replication");
                return false;
            }

            if (StringUtils.isEmpty(plasmid.getBackbone())) {
                Logger.info(entry.getPartNumber() + " is missing backbone");
                return false;
            }

            if (StringUtils.isEmpty(plasmid.getOriginOfReplication())) {
                Logger.info(entry.getPartNumber() + " is missing origin of replication information");
                return false;
            }
        }

        if (EntryType.STRAIN.getName().equalsIgnoreCase(entry.getRecordType())) {
            Strain strain = (Strain) entry;
            if (StringUtils.isEmpty(strain.getHost())) {
                Logger.info(entry.getPartNumber() + " is missing host information");
                return false;
            }

            if (StringUtils.isEmpty(strain.getIntellectualProperty())) {
                Logger.info(entry.getPartNumber() + " is missing intellectual property");
                return false;
            }
        }

        // only strains and plasmids are allowed
        return false;
    }
}

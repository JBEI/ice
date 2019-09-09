package org.jbei.ice.lib.folder;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.email.EmailFactory;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

import java.util.*;
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
     * Retrieves list of folders that specified user has write privileges on.
     * This excludes folders that are of type <code>SAMPLE</code>
     *
     * @return list of folders
     */
    public List<FolderDetails> getCanEditFolders() {
        Account account = this.accountDAO.getByEmail(userId);
        Set<Group> accountGroups = getAccountGroups(account);
        List<Folder> folders = dao.getCanEditFolders(account, accountGroups);
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
        folder = dao.update(folder);
        if (folder == null)
            return false;

        Account account = accountDAO.getByEmail(userId);

        // create model
        SampleCreateModel model = new SampleCreateModel();
        model.setRequested(new Date());
        model.setUpdated(new Date());
        model.setFolder(folder);
        model.setAccount(account);
        DAOFactory.getSampleCreateModelDAO().create(model);

        // send email notification
        String archiveEmail = Utils.getConfigValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
        try {
            if (!StringUtils.isEmpty(archiveEmail)) {
                String emailBody = createEmailBody(account, folderId);
                EmailFactory.getEmail().send(archiveEmail, "Sample creation requested", emailBody);
            }
        } catch (Exception e) {
            Logger.error("Exception sending email " + e);
        }

        return true;
    }

    public void setFolderApproval(long folderId, boolean approve) {
        Folder folder = dao.get(folderId);
        if (folder == null)
            throw new IllegalArgumentException("Cannot find folder with id " + folderId);

        if (folder.getType() != FolderType.SAMPLE)
            throw new IllegalStateException("Can only approve folders of type 'SAMPLE'");

        // only admins can approve or deny
        authorization.expectAdmin(userId);

        SampleCreateModelDAO createModelDAO = DAOFactory.getSampleCreateModelDAO();
        Optional<SampleCreateModel> optional = createModelDAO.getByFolder(folder);
        Account account = accountDAO.getByEmail(userId);

        if (optional.isPresent()) {
            // update
            SampleCreateModel model = optional.get();
            model.setStatus(approve ? SampleRequestStatus.APPROVED : SampleRequestStatus.REJECTED);
            model.setUpdated(new Date());
            createModelDAO.update(model);
        } else {
            // create new
            SampleCreateModel model = new SampleCreateModel();
            model.setAccount(account);
            model.setFolder(folder);
            model.setStatus(approve ? SampleRequestStatus.APPROVED : SampleRequestStatus.REJECTED);
            model.setRequested(new Date());
            model.setUpdated(new Date());
            createModelDAO.create(model);
        }

        // if a request is rejected then folder is returned to private state
        if (!approve) {
            folder.setType(FolderType.PRIVATE);
            folder.setModificationTime(new Date());
            dao.update(folder);
        } else {
            // send email
            String emailMessage = Utils.getConfigValue(ConfigurationKey.SAMPLE_CREATE_APPROVAL_MESSAGE);
            if (!StringUtils.isEmpty(emailMessage)) {
                String body = "Dear " + account.getFullName() + ",";
                body += "\n\n";
                body += emailMessage;
                body += "\n\n";
                body += "Thank you!";

                EmailFactory.getEmail().send(account.getEmail(), "Sample creation request approved", body);
            }
        }
    }

    private String createEmailBody(Account account, long folderId) {
        String body = "A sample creation request have been received from " + account.getFullName() + " for a folder";
        body += "\n\nPlease go to the following link to review its contents.\n";
        body += Utils.getConfigValue(ConfigurationKey.URI_PREFIX) + "/folders/" + folderId;
        return body;
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

            if (!isValidEntry(entry, folder))
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
    private boolean isValidEntry(Entry entry, Folder folder) {
        if (EntryType.PLASMID.getName().equalsIgnoreCase(entry.getRecordType())) {
            Plasmid plasmid = (Plasmid) entry;

            // check the plasmid parent strain. It must be in same folder
            List<Entry> parents = DAOFactory.getEntryDAO().getParents(entry.getId());
            for (Entry parent : parents) {
                if (parent.getRecordType().equalsIgnoreCase(EntryType.STRAIN.getDisplay()) && !parent.getFolders().contains(folder)) {
                    Logger.info(parent.getPartNumber() + " is a parent strain but is not contained in same folder");
                    return false;
                }
            }

            if (!entry.getFolders().contains(folder))
                return false;

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

            return true;
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

            return true;
        }

        // only strains and plasmids are allowed
        return false;
    }
}

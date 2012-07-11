package org.jbei.ice.lib.bulkimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.server.EntryToInfoFactory;
import org.jbei.ice.server.InfoToModelFactory;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.BulkImportInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.StrainInfo;
import org.jbei.ice.shared.dto.Visibility;

/**
 * Controller for dealing with bulk import drafts
 * 
 * @author Hector Plahar
 */
public class BulkImportController {

    private final BulkImportDAO dao;
    private final AccountController accountController;
    private final EntryController entryController;

    public BulkImportController() {
        dao = new BulkImportDAO();
        accountController = new AccountController();
        entryController = new EntryController();
    }

    /**
     * Retrieves list of bulk imports that are owned by the system. System ownership is assigned to
     * all bulk imports that are submitted by non-admins. Administrative privileges are required
     * for making this call
     * 
     * @param account account for user making call
     * @return list of bulk imports pending verification
     * @throws ControllerException
     * @throws PermissionException
     */
    public ArrayList<BulkImportInfo> retrievePendingImports(Account account)
            throws ControllerException, PermissionException {

        // check for admin privileges
        if (!accountController.isAdministrator(account))
            throw new PermissionException("Administrative privileges are required!");

        ArrayList<BulkImportInfo> infoList = new ArrayList<BulkImportInfo>();
        ArrayList<BulkImportDraft> results;

        try {
            results = dao.retrieveByAccount(accountController.getSystemAccount());
            if (results == null)
                return infoList;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        for (BulkImportDraft draft : results) {

            BulkImportInfo info = new BulkImportInfo();
            Account draftAccount = draft.getAccount();
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setEmail(draftAccount.getEmail());
            accountInfo.setFirstName(draftAccount.getFirstName());
            accountInfo.setLastName(draftAccount.getLastName());
            info.setAccount(accountInfo);

            info.setId(draft.getId());
            info.setLastUpdate(draft.getLastUpdateTime());
            int count = -1;
            try {
                count = dao.retrieveSavedDraftCount(draft.getId());
            } catch (DAOException e) {
                Logger.error(e); // we care about the data more than the count
            }
            info.setCount(count);
            info.setType(EntryAddType.stringToType(draft.getImportType()));
            info.setCreated(draft.getCreationTime());
            info.setName(draft.getName());

            infoList.add(info);
        }

        return infoList;
    }

    public BulkImportInfo retrieveById(Account account, long id) throws ControllerException,
            PermissionException {

        BulkImportDraft draft;

        try {
            draft = dao.retrieveByIdWithContents(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (draft == null)
            throw new ControllerException("Could not retrieve bulk import draft for id " + id);

        boolean isModerator = accountController.isAdministrator(account);
        boolean isOwner = account.equals(draft.getAccount());

        // check for permissions to retrieve this bulk import
        if (!isModerator && !isOwner) {
            throw new PermissionException("Insufficient privileges by " + account.getEmail()
                    + " to view bulk import for " + draft.getAccount().getEmail());
        }

        BulkImportInfo draftInfo = new BulkImportInfo();
        draftInfo.setCount(draft.getContents().size());
        draftInfo.setCreated(draft.getCreationTime());
        draftInfo.setLastUpdate(draft.getLastUpdateTime());
        draftInfo.setId(draft.getId());
        draftInfo.setType(EntryAddType.stringToType(draft.getImportType()));
        draftInfo.setName(draft.getName());

        AccountInfo accountInfo = new AccountInfo();
        Account draftAccount = draft.getAccount();
        accountInfo.setEmail(draftAccount.getEmail());
        accountInfo.setFirstName(draftAccount.getFirstName());
        accountInfo.setLastName(draftAccount.getLastName());
        draftInfo.setAccount(accountInfo);

        // retrieve the entries
        for (Entry entry : draft.getContents()) {
            EntryInfo info = EntryToInfoFactory.getInfo(account, entry, null, null, null, false);
            if (info != null)
                draftInfo.getEntryList().add(info);
        }

        return draftInfo;
    }

    /**
     * Retrieves list of user saved drafts
     * 
     * @param account account of requesting user
     * @param userAccount
     *            account whose saved drafts are being requested
     * @return list of draft infos representing saved drafts.
     * @throws ControllerException
     */
    public ArrayList<BulkImportInfo> retrieveByUser(Account account, Account userAccount)
            throws ControllerException {

        ArrayList<BulkImportDraft> results;

        try {
            results = dao.retrieveByAccount(userAccount);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        ArrayList<BulkImportInfo> infoArrayList = new ArrayList<BulkImportInfo>();

        for (BulkImportDraft draft : results) {
            Account draftAccount = draft.getAccount();

            boolean isOwner = account.equals(draftAccount);
            boolean isAdmin = accountController.isAdministrator(account);
            if (!isOwner && !isAdmin)
                continue;

            BulkImportInfo draftInfo = new BulkImportInfo();
            draftInfo.setCreated(draft.getCreationTime());
            draftInfo.setLastUpdate(draft.getLastUpdateTime());
            draftInfo.setId(draft.getId());

            draftInfo.setName(draft.getName());
            draftInfo.setType(EntryAddType.stringToType(draft.getImportType()));

            try {
                int count = dao.retrieveSavedDraftCount(draft.getId());
                draftInfo.setCount(count);
            } catch (DAOException e) {
                draftInfo.setCount(-1);
                Logger.error(e);
            }

            // set the account info
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setEmail(draftAccount.getEmail());
            accountInfo.setFirstName(draftAccount.getFirstName());
            accountInfo.setLastName(draftAccount.getLastName());
            draftInfo.setAccount(accountInfo);
            infoArrayList.add(draftInfo);
        }

        return infoArrayList;
    }

    public BulkImportInfo deleteDraftById(Account requesting, long draftId)
            throws ControllerException, PermissionException {
        BulkImportDraft draft;
        try {
            draft = dao.retrieveById(draftId);
            if (draft == null)
                throw new ControllerException("Could not retrieve draft with id \"" + draftId
                        + "\"");

            Account draftAccount = draft.getAccount();
            if (!requesting.equals(draftAccount) && !accountController.isAdministrator(requesting))
                throw new PermissionException("No permissions to delete draft " + draftId);

            dao.delete(draft);

        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        BulkImportInfo draftInfo = new BulkImportInfo();
        try {
            int count = dao.retrieveSavedDraftCount(draft.getId());
            draftInfo.setCount(count);
        } catch (DAOException e) {
            draftInfo.setCount(-1);
            Logger.error(e);
        }
        draftInfo.setCreated(draft.getCreationTime());
        draftInfo.setId(draft.getId());
        Account draftAccount = draft.getAccount();
        draftInfo.setName(draftAccount.getFullName());
        draftInfo.setType(EntryAddType.stringToType(draft.getImportType()));

        // set the account info
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setEmail(draftAccount.getEmail());
        accountInfo.setFirstName(draftAccount.getFirstName());
        accountInfo.setLastName(draftAccount.getLastName());
        draftInfo.setAccount(accountInfo);
        return draftInfo;
    }

    public BulkImportInfo createBulkImportDraft(Account draftOwner, Account entryAccount,
            EntryAddType type, String name, ArrayList<EntryInfo> entryList)
            throws ControllerException {

        BulkImportDraft draft = new BulkImportDraft();
        draft.setName(name);
        draft.setAccount(draftOwner);
        draft.setImportType(type.toString());

        ArrayList<Long> contents = new ArrayList<Long>();

        // convert info contents to Entry
        for (EntryInfo info : entryList) {

            switch (type) {
            // special treatment for strain with plasmid
            case STRAIN_WITH_PLASMID:
                StrainInfo strainInfo;
                PlasmidInfo plasmidInfo;

                if (info.getType() == EntryType.STRAIN) { // this is the typically case but cannot be too careful
                    strainInfo = (StrainInfo) info;
                    plasmidInfo = (PlasmidInfo) info.getInfo();
                } else {
                    plasmidInfo = (PlasmidInfo) info;
                    strainInfo = (StrainInfo) info.getInfo();
                }

                Strain strain = (Strain) InfoToModelFactory.infoToEntry(strainInfo);
                Plasmid plasmid = (Plasmid) InfoToModelFactory.infoToEntry(plasmidInfo);
                strain.setVisibility(Visibility.DRAFT.getValue());
                plasmid.setVisibility(Visibility.DRAFT.getValue());
                strain.setOwner(entryAccount.getFullName());
                strain.setOwnerEmail(entryAccount.getEmail());
                plasmid.setOwner(entryAccount.getFullName());
                plasmid.setOwnerEmail(entryAccount.getEmail());

                // save entries
                HashSet<Entry> results = entryController.createStrainWithPlasmid(entryAccount,
                    strain, plasmid);
                for (Entry entry : results) {
                    contents.add(entry.getId());
                }
                break;

            // all others
            default:
                Entry entry = InfoToModelFactory.infoToEntry(info);
                entry.setVisibility(Visibility.DRAFT.getValue());
                entry.setOwner(entryAccount.getFullName());
                entry.setOwnerEmail(entryAccount.getEmail());

                // save entry
                entry = entryController.createEntry(entryAccount, entry);
                contents.add(entry.getId());
                break;
            }
        }

        ArrayList<Entry> entries = entryController.getEntriesByIdSet(entryAccount, contents);
        draft.setContents(entries);
        draft.setCreationTime(new Date(System.currentTimeMillis()));
        draft.setLastUpdateTime(draft.getCreationTime());

        try {
            return BulkImportUtil.modelToInfo(dao.save(draft));
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public BulkImportInfo updateBulkImportDraft(Account account, long draftId,
            ArrayList<EntryInfo> entryList) throws ControllerException, PermissionException {

        BulkImportDraft draft;
        try {
            draft = dao.retrieveByIdWithContents(draftId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        // check permissions
        if (!draft.getAccount().equals(account) && !accountController.isAdministrator(account))
            throw new PermissionException("User " + account.getEmail()
                    + " does not have permission to update draft " + draftId);

        List<Entry> draftContents = new ArrayList<Entry>(draft.getContents());
        List<Entry> newContents = new ArrayList<Entry>();

        EntryAddType type = EntryAddType.stringToType(draft.getImportType());
        if (type == null)
            throw new ControllerException("Could not determine type for draft " + draftId);

        // convert info contents to Entry
        for (EntryInfo info : entryList) {

            switch (type) {
            // special treatment for strain with plasmid
            case STRAIN_WITH_PLASMID:
                StrainInfo strainInfo = (StrainInfo) info;
                PlasmidInfo plasmidInfo = (PlasmidInfo) info.getInfo();

                // save entries
                boolean updated = updateIfExists(account, draftContents, newContents, info);
                if (!updated) {
                    Strain strain = (Strain) InfoToModelFactory.infoToEntry(strainInfo);
                    Plasmid plasmid = (Plasmid) InfoToModelFactory.infoToEntry(plasmidInfo);
                    strain.setVisibility(Visibility.DRAFT.getValue());
                    plasmid.setVisibility(Visibility.DRAFT.getValue());
                    HashSet<Entry> results = entryController.createStrainWithPlasmid(account,
                        strain, plasmid);
                    newContents.addAll(results);
                    break;
                }

                // if strain exists, then plasmid has to also
                updateIfExists(account, draftContents, newContents, plasmidInfo);
                break;

            // all others
            default:
                // save entry
                updated = updateIfExists(account, draftContents, newContents, info);
                if (!updated) {
                    Entry entry = InfoToModelFactory.infoToEntry(info);
                    entry.setVisibility(Visibility.DRAFT.getValue());
                    entry = entryController.createEntry(account, entry);
                    newContents.add(entry);
                }
                break;
            }
        }

        // update the draft
        try {
            draft.setContents(newContents);
            draft.setLastUpdateTime(new Date(System.currentTimeMillis()));
            dao.update(draft);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        // convert draft to info
        BulkImportInfo draftInfo = new BulkImportInfo();
        draftInfo.setCount(newContents.size());
        draftInfo.setCreated(draft.getCreationTime());
        draftInfo.setLastUpdate(draft.getLastUpdateTime());
        draftInfo.setId(draft.getId());
        Account draftAccount = draft.getAccount();
        draftInfo.setName(draftAccount.getFullName());
        draftInfo.setType(EntryAddType.stringToType(draft.getImportType()));

        // set the account info
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setEmail(draftAccount.getEmail());
        accountInfo.setFirstName(draftAccount.getFirstName());
        accountInfo.setLastName(draftAccount.getLastName());
        draftInfo.setAccount(accountInfo);
        return draftInfo;
    }

    private boolean updateIfExists(Account account, List<Entry> entryList, List<Entry> newContents,
            EntryInfo info) throws ControllerException, PermissionException {
        if (entryList == null || entryList.isEmpty())
            return false;

        Iterator<Entry> iterator = entryList.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (info.getId() == entry.getId()) {
                // perform update
                InfoToModelFactory.infoToEntry(info, entry);
                entry.setVisibility(Visibility.DRAFT.getValue());
                entryController.update(account, entry);
                newContents.add(entry);
                return true;
            }
        }

        return false;
    }

    /**
     * Submits bulk import. If user performing the submission is not an administrator
     * then it is submitted for verification.
     * 
     * @param entryAccount account of user performing the submission
     * @param type type of bulk import
     * @param entryList list of entries contained in this bulk import
     * @return true is import was submitted successfully, false otherwise
     * @throws ControllerException
     */
    public boolean submitBulkImport(Account entryAccount, EntryAddType type,
            ArrayList<EntryInfo> entryList) throws ControllerException {

        boolean isAdmin = accountController.isAdministrator(entryAccount);
        Visibility visibility;
        if (!isAdmin)
            visibility = Visibility.PENDING;
        else
            visibility = Visibility.OK;

        ArrayList<Long> contents = new ArrayList<Long>();

        // convert info contents to Entry
        for (EntryInfo info : entryList) {

            switch (type) {
            // special treatment for strain with plasmid
            case STRAIN_WITH_PLASMID:
                StrainInfo strainInfo;
                PlasmidInfo plasmidInfo;

                if (info.getType() == EntryType.STRAIN) { // this is the typically case but cannot be too careful
                    strainInfo = (StrainInfo) info;
                    plasmidInfo = (PlasmidInfo) info.getInfo();
                } else {
                    plasmidInfo = (PlasmidInfo) info;
                    strainInfo = (StrainInfo) info.getInfo();
                }

                Strain strain = (Strain) InfoToModelFactory.infoToEntry(strainInfo);
                Plasmid plasmid = (Plasmid) InfoToModelFactory.infoToEntry(plasmidInfo);

                strain.setVisibility(visibility.getValue());
                plasmid.setVisibility(visibility.getValue());

                strain.setOwner(entryAccount.getFullName());
                strain.setOwnerEmail(entryAccount.getEmail());
                plasmid.setOwner(entryAccount.getFullName());
                plasmid.setOwnerEmail(entryAccount.getEmail());

                // save entries
                HashSet<Entry> results = entryController.createStrainWithPlasmid(entryAccount,
                    strain, plasmid);
                for (Entry entry : results) {
                    contents.add(entry.getId());
                }

                // TODO : sequence and attachments 
                break;

            // all others
            default:
                Entry entry = InfoToModelFactory.infoToEntry(info);
                entry.setVisibility(visibility.getValue());
                entry.setOwner(entryAccount.getFullName());
                entry.setOwnerEmail(entryAccount.getEmail());

                // save entry
                entry = entryController.createEntry(entryAccount, entry);

                // check sequence
                if (info.isHasSequence()) {
                    // support for only one in bulk import upload
                    saveSequence(entryAccount, info.getSequenceAnalysis().get(0), entry);
                }

                // check attachment 
                if (info.isHasAttachment()) {
                    try {
                        saveAttachment(entryAccount, info.getAttachments().get(0), entry);
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                }

                contents.add(entry.getId());
                break;
            }
        }

        if (isAdmin)
            return true;

        Account draftOwner = accountController.getSystemAccount();

        BulkImportDraft draft = new BulkImportDraft();
        draft.setName(entryAccount.getEmail());
        draft.setAccount(draftOwner);
        draft.setImportType(type.toString());

        ArrayList<Entry> entries = entryController.getEntriesByIdSet(entryAccount, contents);
        draft.setContents(entries);
        draft.setCreationTime(new Date(System.currentTimeMillis()));
        draft.setLastUpdateTime(draft.getCreationTime());

        try {
            return BulkImportUtil.modelToInfo(dao.save(draft)) != null;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    protected void saveSequence(Account account, SequenceAnalysisInfo sequenceInfo, Entry entry)
            throws ControllerException {
        if (sequenceInfo == null)
            return;

        String fileId = sequenceInfo.getFileId();
        File file = new File(JbeirSettings.getSetting("TEMPORARY_DIRECTORY") + File.separatorChar
                + fileId);

        if (!file.exists()) {
            Logger.error("Could not find sequence file \"" + file.getAbsolutePath() + "\"");
            return;
        }

        try {
            String sequenceString = FileUtils.readFileToString(file);
            SequenceController controller = new SequenceController();
            controller.parseAndSaveSequence(account, entry, sequenceString);
        } catch (IOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    protected void saveAttachment(Account entryAccount, AttachmentInfo info, Entry entry)
            throws FileNotFoundException, PermissionException, ControllerException {
        if (info == null)
            return;

        String fileId = info.getFileId();

        File file = new File(JbeirSettings.getSetting("TEMPORARY_DIRECTORY") + File.separatorChar
                + fileId);

        if (!file.exists()) {
            Logger.error("Could not find attachment file \"" + file.getAbsolutePath() + "\"");
            return;
        }

        Attachment attachment = new Attachment();
        attachment.setEntry(entry);
        attachment.setFileName(info.getFilename());

        FileInputStream inputStream = new FileInputStream(file);

        AttachmentController controller = new AttachmentController();
        controller.save(entryAccount, attachment, inputStream);
    }
}

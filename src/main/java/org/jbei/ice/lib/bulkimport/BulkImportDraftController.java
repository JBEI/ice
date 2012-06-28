package org.jbei.ice.lib.bulkimport;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.server.EntryToInfoFactory;
import org.jbei.ice.server.InfoToModelFactory;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.StrainInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Controller for dealing with bulk import drafts
 *
 * @author Hector Plahar
 */
public class BulkImportDraftController {

    private final BulkImportDraftDAO dao;
    private final AccountController accountController;
    private final EntryController entryController;

    public BulkImportDraftController() {
        dao = new BulkImportDraftDAO();
        accountController = new AccountController();
        entryController = new EntryController();
    }

    public BulkImportDraftInfo retrieveById(Account account, long id)
            throws ControllerException, PermissionException {

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

        BulkImportDraftInfo draftInfo = new BulkImportDraftInfo();
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
     * @param account     account of requesting user
     * @param userAccount account whose saved drafts are being requested
     * @return list of draft infos representing saved drafts.
     * @throws ControllerException
     */
    public ArrayList<BulkImportDraftInfo> retrieveByUser(Account account, Account userAccount)
            throws ControllerException {

        ArrayList<BulkImportDraft> results;

        try {
            results = dao.retrieveByAccount(userAccount);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        ArrayList<BulkImportDraftInfo> infoArrayList = new ArrayList<BulkImportDraftInfo>();

        for (BulkImportDraft draft : results) {
            Account draftAccount = draft.getAccount();

            boolean isOwner = account.equals(draftAccount);
            boolean isAdmin = accountController.isAdministrator(account);
            if (!isOwner && !isAdmin)
                continue;

            BulkImportDraftInfo draftInfo = new BulkImportDraftInfo();
            draftInfo.setCreated(draft.getCreationTime());
            draftInfo.setId(draft.getId());

            draftInfo.setName(draft.getName());
            draftInfo.setType(EntryAddType.stringToType(draft.getImportType()));

            try {
                int count = dao.retrieveSavedDraftCount(draft.getId());
                draftInfo.setCount(count);
            } catch (DAOException e) {
                draftInfo.setCount(-1);
                Logger.error(e);
                continue;
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

    public BulkImportDraftInfo deleteDraftById(Account requesting, long draftId)
            throws ControllerException, PermissionException {
        BulkImportDraft draft;
        try {
            draft = dao.retrieveById(draftId);
            if (draft == null)
                throw new ControllerException("Could not retrieve draft with id \"" + draftId + "\"");

            Account draftAccount = draft.getAccount();
            if (!requesting.equals(draftAccount) || !accountController.isAdministrator(requesting))
                throw new PermissionException("No permissions to delete draft " + draftId);

            dao.delete(draft);

        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        BulkImportDraftInfo draftInfo = new BulkImportDraftInfo();
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

    public BulkImportDraftInfo createBulkImportDraft(Account account, EntryAddType type,
            String name, ArrayList<EntryInfo> entryList) throws ControllerException {

        BulkImportDraft draft = new BulkImportDraft();
        draft.setName(name);
        draft.setAccount(account);
        draft.setImportType(type.toString());

        ArrayList<Entry> contents = new ArrayList<Entry>();

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
                    strain.setVisibility(new Integer(0));
                    plasmid.setVisibility(new Integer(0));
                    strain.setOwner(account.getFullName());
                    strain.setOwnerEmail(account.getEmail());
                    plasmid.setOwner(account.getFullName());
                    plasmid.setOwnerEmail(account.getEmail());

                    // save entries
                    HashSet<Entry> results = entryController.createStrainWithPlasmid(strain, plasmid);
                    contents.addAll(results);
                    break;

                // all others
                default:
                    Entry entry = InfoToModelFactory.infoToEntry(info);
                    entry.setVisibility(new Integer(0));
                    entry.setOwner(account.getFullName());
                    entry.setOwnerEmail(account.getEmail());

                    // save entry
                    entry = entryController.createEntry(entry);
                    contents.add(entry);
                    break;
            }
        }

        draft.setContents(contents);
        draft.setCreationTime(new Date(System.currentTimeMillis()));
        draft.setLastUpdateTime(draft.getCreationTime());

        try {
            return BulkImportUtil.modelToInfo(dao.save(draft));
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public BulkImportDraftInfo updateBulkImportDraft(Account account, long draftId, ArrayList<EntryInfo> entryList)
            throws ControllerException, PermissionException {

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
        List<Entry> contents = new ArrayList<Entry>(draft.getContents());
        EntryAddType type = EntryAddType.stringToType(draft.getImportType());
        if (type == null)
            throw new ControllerException("Could not determine type for draft " + draftId);

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

                    // save entries
                    boolean updated = updateIfExists(account, contents, info);
                    if (!updated) {
                        Strain strain = (Strain) InfoToModelFactory.infoToEntry(strainInfo);
                        Plasmid plasmid = (Plasmid) InfoToModelFactory.infoToEntry(plasmidInfo);
                        strain.setVisibility(new Integer(0));
                        plasmid.setVisibility(new Integer(0));
                        HashSet<Entry> results = entryController.createStrainWithPlasmid(strain, plasmid);
                        contents.addAll(results);
                        break;
                    }

                    // if strain exists, then plasmid has to also
                    updateIfExists(account, contents, plasmidInfo);
                    break;

                // all others
                default:

                    // save entry
                    updated = updateIfExists(account, contents, info);
                    if (!updated) {
                        Entry entry = InfoToModelFactory.infoToEntry(info);
                        entry.setVisibility(new Integer(0));
                        entry = entryController.createEntry(entry);
                        contents.add(entry);
                    }
                    break;
            }
        }

        // update the draft
        try {
            draft.setContents(contents);
            draft.setLastUpdateTime(new Date(System.currentTimeMillis()));
            dao.save(draft);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        BulkImportDraftInfo draftInfo = new BulkImportDraftInfo();
        draftInfo.setCount(contents.size());
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

    private boolean updateIfExists(Account account, List<Entry> entryList, EntryInfo info)
            throws ControllerException, PermissionException {
        if (entryList == null || entryList.isEmpty())
            return false;

        Iterator<Entry> iterator = entryList.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (info.getId() == entry.getId()) {
                // perform update
                InfoToModelFactory.infoToEntry(info, entry);
                entryController.save(account, entry);
                return true;
            }
        }

        return false;
    }

    public boolean submitBulkImportForVerification(Account account, EntryAddType type, ArrayList<EntryInfo> entryList)
            throws ControllerException {
        Account systemAccount = accountController.getSystemAccount();
        return createBulkImportDraft(systemAccount, type, "Pending", entryList) != null;
    }
}

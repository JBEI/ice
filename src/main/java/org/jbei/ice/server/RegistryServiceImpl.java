package org.jbei.ice.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbei.ice.client.RegistryService;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.SearchController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.BulkImportManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.FolderManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.NewsManager;
import org.jbei.ice.lib.managers.QueryManager;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.managers.TraceSequenceManager;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.BulkImport;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Folder;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.models.News;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.AuthenticatedPermissionManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BlastProgram;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;
import org.jbei.ice.shared.dto.NewsItem;
import org.jbei.ice.shared.dto.ProfileInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.SearchFilterInfo;
import org.jbei.ice.shared.dto.StorageInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

// TODO : this whole class needs to be redone. The logic needs to be moved to controllers/managers
public class RegistryServiceImpl extends RemoteServiceServlet implements RegistryService {

    private static final long serialVersionUID = 1L;

    @Override
    public AccountInfo login(String name, String pass) {

        try {
            SessionData sessionData = AccountController.authenticate(name, pass);
            log("User by login '" + name + "' successfully logged in");

            AccountInfo info = this.accountToInfo(sessionData.getAccount());
            if (info == null)
                return null;

            info.setSessionId(sessionData.getSessionKey());
            long visibleEntryCount = EntryManager.getNumberOfVisibleEntries();
            info.setVisibleEntryCount(visibleEntryCount);
            return info;
        } catch (InvalidCredentialsException e) {
            Logger.warn("Invalid credentials provided by user: " + name);
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    @Override
    public AccountInfo sessionValid(String sid) {
        try {
            if (AccountController.isAuthenticated(sid)) {
                Account account = AccountController.getAccountBySessionKey(sid);
                AccountInfo info = this.accountToInfo(account);
                long visibleEntryCount = EntryManager.getNumberOfVisibleEntries();
                info.setVisibleEntryCount(visibleEntryCount);
                return info;
            }
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (ManagerException e) {
            Logger.error(e);
        }
        return null;
    }

    @Override
    public boolean logout(String sessionId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ArrayList<Long> retrieveSearchResults(ArrayList<SearchFilterInfo> filters) {
        ArrayList<QueryFilter> queryFilters = new ArrayList<QueryFilter>();
        for (SearchFilterInfo filter : filters) {
            QueryFilter queryFilter = new QueryFilter(filter);
            queryFilters.add(queryFilter);
        }

        try {
            Set<Long> filterResults = QueryManager.runFilters(queryFilters);
            ArrayList<Long> results = new ArrayList<Long>(filterResults);
            return results;
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<EntryInfo> retrieveEntryData(String sid, ArrayList<Long> entryIds,
            ColumnField type, boolean asc) {

        // TODO: Use Controller and put all of the logic in there
        if (type == null)
            type = ColumnField.CREATED;

        try {
            Account account = this.retrieveAccountForSid(sid);
            if (account == null)
                return null;

            ArrayList<EntryInfo> results = new ArrayList<EntryInfo>();
            List<Entry> entries = null;

            //            EntryController controller = new EntryController(account);
            switch (type) {
            case TYPE:

                entries = EntryManager.getEntriesByIdSetSortByType(entryIds, asc);
                //                entries = controller.
                break;

            case CREATED:
                entries = EntryManager.getEntriesByIdSetSortByCreated(entryIds, asc);
                break;

            default:
                entries = EntryManager.getEntriesByIdSet(entryIds);
            }

            if (entries == null)
                return results;

            for (Entry entry : entries) {

                EntryInfo view = EntryViewFactory.createTipView(entry);
                if (view == null)
                    continue;

                results.add(view);
            }

            return results;
        } catch (ManagerException e) {
            Logger.error("Error retrieving entry id set", e);
            return null;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public EntryInfo retrieveEntryView(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ArrayList<FolderDetails> retrieveUserCollections(String sessionId, String userId) {
        ArrayList<FolderDetails> results = new ArrayList<FolderDetails>();

        try {
            Account account = retrieveAccountForSid(sessionId);
            if (account == null)
                return null;

            Account userAccount = AccountController.getByEmail(userId);

            // get user folder
            List<Folder> userFolders = FolderManager.getFoldersByOwner(userAccount);
            if (userFolders != null) {
                for (Folder folder : userFolders) {
                    long id = folder.getId();
                    FolderDetails details = new FolderDetails(id, folder.getName(), false);
                    int folderSize = FolderManager.getFolderSize(id);
                    details.setCount(folderSize);
                    details.setDescription(folder.getDescription());
                    results.add(details);
                }
            }

            return results;
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        } catch (ManagerException me) {
            Logger.error(me);
            return null;
        }
    }

    @Override
    public ArrayList<FolderDetails> retrieveCollections(String sessionId) {

        ArrayList<FolderDetails> results = new ArrayList<FolderDetails>();
        try {
            Account account = retrieveAccountForSid(sessionId);
            if (account == null)
                return null;

            Account system = AccountController.getSystemAccount();
            List<Folder> folders = FolderManager.getFoldersByOwner(system);

            for (Folder folder : folders) {
                long id = folder.getId();
                FolderDetails details = new FolderDetails(id, folder.getName(), true);
                int folderSize = FolderManager.getFolderSize(id);
                details.setCount(folderSize);
                details.setDescription(folder.getDescription());
                results.add(details);
            }

            // get user folder
            List<Folder> userFolders = FolderManager.getFoldersByOwner(account);
            if (userFolders != null) {
                for (Folder folder : userFolders) {
                    long id = folder.getId();
                    FolderDetails details = new FolderDetails(id, folder.getName(), false);
                    int folderSize = FolderManager.getFolderSize(id);
                    details.setCount(folderSize);
                    details.setDescription(folder.getDescription());
                    results.add(details);
                }
            }

            return results;

        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        } catch (ManagerException me) {
            Logger.error(me);
            return null;
        }
    }

    @Override
    public ArrayList<Long> retrieveEntriesForFolder(String sessionId, long folderId) {
        // TODO :

        //        Account account = this.retrieveAccountForSid(sessionId);
        //        if( account == null )
        //            return null;

        try {
            return FolderManager.getFolderContents(folderId, false);
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<Long> retrieveUserEntries(String sid, String userId) {

        Account account = null;

        try {
            if (userId == null)
                account = retrieveAccountForSid(sid);
            else
                account = AccountManager.getByEmail(userId);

            if (account == null)
                return null;

            EntryController entryController = new EntryController(account);
            return entryController.getEntryIdsByOwner(account.getEmail());

        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<Long> retrieveAllEntryIDs(String sid) {
        Account account = null;

        try {
            account = retrieveAccountForSid(sid);
            if (account == null)
                return null;

            EntryController entryController = new EntryController(account);
            return entryController.getAllEntryIDs();

        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public long retrieveAvailableEntryCount(String sessionId) {
        // TODO Auto-generated method stub
        return -1;
    }

    protected Account retrieveAccountForSid(String sid) throws ControllerException {
        if (!AccountController.isAuthenticated(sid))
            return null;

        return AccountController.getAccountBySessionKey(sid);
    }

    @Override
    public ArrayList<Long> retrieveRecentlyViewed(String sid) {
        try {
            Account account = this.retrieveAccountForSid(sid);
            if (account == null)
                return null;

            return WorkspaceManager.getRecentlyViewedByAccount(account);
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<Long> retrieveWorkspaceEntries(String sid) {
        return new ArrayList<Long>();
    }

    @Override
    public HashMap<AutoCompleteField, ArrayList<String>> retrieveAutoCompleteData(String sid) {
        HashMap<AutoCompleteField, ArrayList<String>> data = new HashMap<AutoCompleteField, ArrayList<String>>();

        // origin of replication
        ArrayList<String> origin = new ArrayList<String>();
        origin.addAll(UtilsManager.getUniqueOriginOfReplications());
        data.put(AutoCompleteField.ORIGIN_OF_REPLICATION, origin);

        // selection markers
        try {
            ArrayList<String> markers = new ArrayList<String>();
            markers.addAll(UtilsManager.getUniqueSelectionMarkers());
            data.put(AutoCompleteField.SELECTION_MARKER, markers);
        } catch (ManagerException e) {
            Logger.error(e);
        }

        // promoters
        ArrayList<String> promoters = new ArrayList<String>();
        promoters.addAll(UtilsManager.getUniquePromoters());
        data.put(AutoCompleteField.PROMOTER, promoters);

        // plasmid names
        ArrayList<String> plasmidNames = new ArrayList<String>();
        plasmidNames.addAll(UtilsManager.getUniquePublicPlasmidNames());
        data.put(AutoCompleteField.PLASMID_NAME, plasmidNames);

        return data;
    }

    @Override
    public EntryInfo retrieveEntryDetails(String sid, long id) {
        try {
            Entry entry = EntryManager.get(id);
            if (entry == null)
                return null;

            ArrayList<Attachment> attachments = AttachmentManager.getByEntry(entry);
            ArrayList<Sample> samples = SampleManager.getSamplesByEntry(entry);
            List<TraceSequence> sequences = TraceSequenceManager.getByEntry(entry);

            Map<Sample, LinkedList<Storage>> sampleMap = new HashMap<Sample, LinkedList<Storage>>();
            for (Sample sample : samples) {
                Storage storage = sample.getStorage();

                LinkedList<Storage> storageList = new LinkedList<Storage>();

                List<Storage> storages = StorageManager.getStoragesUptoScheme(storage);
                if (storages != null)
                    storageList.addAll(storages);
                Storage scheme = StorageManager.getSchemeContainingParentStorage(storage);
                if (scheme != null)
                    storageList.add(scheme);

                sampleMap.put(sample, storageList);
            }

            boolean hasSequence = (SequenceManager.getByEntry(entry) != null);

            return EntryToInfoFactory
                    .getInfo(entry, attachments, sampleMap, sequences, hasSequence);
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public AccountInfo retrieveAccountInfo(String sid, String userId) {
        try {
            this.retrieveAccountForSid(sid);
            Account account = retrieveAccountForSid(sid);
            if (account == null)
                return null;

            account = AccountManager.getByEmail(userId);
            if (account == null)
                return null;

            AccountInfo info = accountToInfo(account);

            // get the count for samples
            int sampleCount = SampleManager.getSampleCountBy(info.getEmail());
            info.setUserSampleCount(sampleCount);
            long visibleEntryCount = EntryManager.getNumberOfVisibleEntries();
            info.setVisibleEntryCount(visibleEntryCount);
            int entryCount = EntryManager.getEntryCountBy(info.getEmail());
            info.setUserEntryCount(entryCount);

            return info;
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public AccountInfo retrieveAccountInfoForSession(String sid) {
        Account account;
        try {
            account = retrieveAccountForSid(sid);
            return accountToInfo(account);
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return null;
    }

    private AccountInfo accountToInfo(Account account) {
        if (account == null)
            return null;

        AccountInfo info = new AccountInfo();
        info.setEmail(account.getEmail());
        info.setFirstName(account.getFirstName());
        info.setLastName(account.getLastName());
        info.setInstitution(account.getInstitution());
        info.setDescription(account.getDescription());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d yyyy");
        Date memberSinceDate = account.getCreationTime();
        info.setSince(dateFormat.format(memberSinceDate));

        return info;
    }

    @Override
    public ArrayList<BlastResultInfo> blastSearch(String sid, String query, BlastProgram program) {
        try {
            Account account = this.retrieveAccountForSid(sid);
            if (account == null)
                return null;

            ArrayList<BlastResultInfo> results = new ArrayList<BlastResultInfo>();
            ArrayList<BlastResult> blastResults = new ArrayList<BlastResult>();

            SearchController searchController = new SearchController(account);
            if (program == BlastProgram.BLAST_N) {
                blastResults.addAll(searchController.blastn(query));
            } else if (program == BlastProgram.TBLAST_X) {
                blastResults.addAll(searchController.tblastx(query));
            }

            //            if (blastResults != null && blastResults.size() > 0) {
            //                Panel resultPanel;
            //                if (program.equals("tblastx")) {
            //                    String proteinQuery;
            //                    try {
            //                        proteinQuery = SequenceUtils.translateToProtein(query);
            //                    } catch (Exception e) {
            //                        proteinQuery = "";
            //
            //                        Logger.error("Failed to translate dna to protein!", e);
            //                    }
            //
            //                    resultPanel = new BlastResultPanel(BLAST_RESULT_PANEL_NAME,
            //                            proteinQuery, blastResults, NUMBER_OF_ENTRIES_PER_PAGE, false);
            //                } else {
            //                    resultPanel = new BlastResultPanel(BLAST_RESULT_PANEL_NAME, query,
            //                            blastResults, NUMBER_OF_ENTRIES_PER_PAGE, true);
            //                }

            for (BlastResult blastResult : blastResults) {
                BlastResultInfo info = new BlastResultInfo();
                info.setBitScore(blastResult.getBitScore());
                EntryInfo view = EntryViewFactory.createTipView(blastResult.getEntry());
                info.setDataView(view);
                info.seteValue(blastResult.geteValue());
                info.setAlignmentLength(blastResult.getAlignmentLength());
                info.setPercentId(blastResult.getPercentId());
                info.setQueryLength(query.length());
                results.add(info);
            }

            return results;

        } catch (ControllerException ce) {
            Logger.error(ce);
        } catch (ProgramTookTooLongException e) {
            Logger.error(e);
        }

        return null;
    }

    //
    // STORAGE
    //

    @Override
    public ArrayList<StorageInfo> retrieveChildren(String sid, long id) {
        ArrayList<StorageInfo> result = new ArrayList<StorageInfo>();
        List<Storage> children = null;

        try {
            Storage currentStorage = StorageManager.get(id, true);
            children = new LinkedList<Storage>(currentStorage.getChildren());

            for (Storage storage : children) {

                StorageInfo info = new StorageInfo();
                String index = storage.getIndex();
                if (index == null || index.isEmpty())
                    info.setDisplay(storage.getName());
                else
                    info.setDisplay(storage.getName() + " " + index);
                info.setId(storage.getId());
                result.add(info);

                ArrayList<Sample> samples = SampleManager.getSamplesByStorage(storage);
                if (samples == null || samples.isEmpty())
                    continue;

                ArrayList<SampleInfo> sampleInfos = new ArrayList<SampleInfo>();
                for (Sample sample : samples) {
                    SampleInfo sampleInfo = new SampleInfo();
                    sampleInfo.setLabel(sample.getLabel());
                    sampleInfo.setId("" + sample.getId());
                    sampleInfos.add(sampleInfo);
                }
                info.setSamples(sampleInfos);
            }

            return result;
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<StorageInfo> retrieveStorageRoot(String sid) {
        try {

            ArrayList<StorageInfo> result = new ArrayList<StorageInfo>();

            for (Storage storage : StorageManager.getAllStorageRoot()) {
                StorageInfo info = new StorageInfo();
                info.setDisplay(storage.getName());
                info.setId(storage.getId());
                result.add(info);
            }

            return result;

        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    //
    // SAMPLES
    //

    // Uses email identifier from session if parameter instance is null
    @Override
    public LinkedList<Long> retrieveSamplesByDepositor(String sid, String email, ColumnField field,
            boolean asc) {

        Account account = null;
        try {
            account = this.retrieveAccountForSid(sid);
        } catch (ControllerException e) {
            e.printStackTrace();
        }
        if (account == null)
            return null;

        if (field == null)
            field = ColumnField.CREATED;

        String depositor = (email == null) ? account.getEmail() : email;
        SampleController sampleController = new SampleController(account);

        // sort param
        try {
            return sampleController.retrieveSamplesByDepositor(depositor, field, asc);
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public LinkedList<SampleInfo> retrieveSampleInfo(String sid, LinkedList<Long> sampleIds,
            boolean asc) {
        LinkedList<SampleInfo> data = null;
        Account account = null;
        try {
            account = this.retrieveAccountForSid(sid);
        } catch (ControllerException e) {
            Logger.error(e);
        }

        if (account == null)
            return null;

        SampleController sampleController = new SampleController(account);
        try {
            LinkedList<Sample> results = sampleController.retrieveSamplesByIdSet(sampleIds, asc);
            if (results != null) {
                data = new LinkedList<SampleInfo>();

                for (Sample sample : results) { // TODO
                    SampleInfo info = new SampleInfo();
                    info.setId(String.valueOf(sample.getId()));
                    info.setCreationTime(sample.getCreationTime());
                    EntryInfo view = EntryViewFactory.createTipView(sample.getEntry());
                    info.setDataView(view);
                    info.setLabel(sample.getLabel());
                    info.setNotes(sample.getNotes());
                    Storage storage = sample.getStorage();
                    if (storage != null) {
                        info.setLocationId(String.valueOf(storage.getId()));
                        info.setLocation(storage.getIndex());
                    }
                    data.add(info);
                }
            }

            return data;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public FolderDetails retrieveFolderDetails(String sid, long folderId) {
        try {
            Folder folder = FolderManager.get(folderId);
            long id = folder.getId();
            boolean isSystemFolder = folder.getOwnerEmail().equals(
                AccountManager.getSystemAccount().getEmail());
            FolderDetails details = new FolderDetails(id, folder.getName(), isSystemFolder);
            int folderSize = FolderManager.getFolderSize(id);
            details.setCount(folderSize);
            details.setDescription(folder.getDescription());
            return details;

        } catch (ManagerException e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public FolderDetails createUserCollection(String sid, String name, String description) {
        try {
            Account account = this.retrieveAccountForSid(sid);
            Folder folder = new Folder(name);
            folder.setOwnerEmail(account.getEmail());
            folder.setDescription(description);
            folder = FolderManager.save(folder);
            FolderDetails details = new FolderDetails(folder.getId(), folder.getName(), false);
            details.setDescription(folder.getDescription());
            return details;
        } catch (ControllerException e) {
            Logger.error(e.getMessage());
            return null;
        } catch (ManagerException e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    // TODO : this is currently working just as "Add to"
    @Override
    public boolean moveToUserCollection(String sid, ArrayList<Long> source,
            ArrayList<Long> destination, ArrayList<Long> entryIds) {

        // TODO:  this needs to be done in a single transaction, set list of folders in manager instead of iterating here
        try {
            for (long folderId : destination) {
                FolderManager.addFolderContents(folderId, entryIds);
            }
            return true;
        } catch (ManagerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public ArrayList<FolderDetails> addEntriesToCollection(String sid, ArrayList<Long> destination,
            ArrayList<Long> entryIds) {

        ArrayList<FolderDetails> results = new ArrayList<FolderDetails>();

        // TODO : see todo in moveToUserCollection
        try {
            for (long folderId : destination) {
                Folder folder = FolderManager.addFolderContents(folderId, entryIds);
                FolderDetails details = new FolderDetails(folder.getId(), folder.getName(), false);
                int folderSize = FolderManager.getFolderSize(folder.getId()); // TODO : this call may not be needed
                details.setCount(folderSize);
                details.setDescription(folder.getDescription());
                results.add(details);
            }
            return results;
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ProfileInfo retrieveProfileInfo(String sid, String userId) {

        try {
            Account account = retrieveAccountForSid(sid);
            if (account == null)
                return null;

            account = AccountManager.getByEmail(userId);
            if (account == null)
                return null;

            AccountInfo accountInfo = accountToInfo(account);

            // get the count for samples
            int sampleCount = SampleManager.getSampleCountBy(accountInfo.getEmail());
            accountInfo.setUserSampleCount(sampleCount);
            long visibleEntryCount = EntryManager.getNumberOfVisibleEntries();
            accountInfo.setVisibleEntryCount(visibleEntryCount);
            int entryCount = EntryManager.getEntryCountBy(accountInfo.getEmail());
            accountInfo.setUserEntryCount(entryCount);

            ProfileInfo profile = new ProfileInfo();
            profile.setAccountInfo(accountInfo);

            if (entryCount > 0) {
                // get user entries
                ArrayList<Long> entries = EntryManager.getEntriesByOwner(userId);
                profile.setUserEntries(entries);
            }

            if (sampleCount > 0) {
                // get user samples
                ArrayList<Long> samples = SampleManager.getSampleIdsByOwner(userId);
                profile.setUserSamples(samples);
            }

            return profile;

        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<BulkImportDraftInfo> retrieveImportDraftData(String sid, String email) {
        try {
            Account account = retrieveAccountForSid(sid);
            if (account == null)
                return null;

            ArrayList<BulkImport> results = BulkImportManager.retrieveByUser(account);
            ArrayList<BulkImportDraftInfo> info = new ArrayList<BulkImportDraftInfo>();

            if (results != null) {
                for (BulkImport draft : results) {
                    BulkImportDraftInfo draftInfo = new BulkImportDraftInfo();
                    //                    draftInfo.setCount(draft.getPrimaryData().size()); // TODO
                    draftInfo.setCreated(draft.getCreationTime());
                    draftInfo.setName(draft.getName());
                    //                    draftInfo.setType(EntryAddType.valueOf(draft.getType()));
                    info.add(draftInfo);
                }
            }

            return info;

        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        } catch (ManagerException me) {
            Logger.error(me);
            return null;
        }
    }

    @Override
    public ArrayList<Long> createEntry(String sid, HashSet<EntryInfo> infoSet) {
        ArrayList<Long> result = new ArrayList<Long>();

        try {
            Account account = retrieveAccountForSid(sid);
            if (account == null)
                return result;

            EntryController controller = new EntryController(account);

            for (EntryInfo info : infoSet) {
                Entry entry = InfoToModelFactory.infoToEntry(info);
                entry = controller.createEntry(entry);

                // TODO : save sample (if any)
                /* 
                 * if (sample != null) {
                sample.setEntry(newEntry);
                if (sample.getStorage() != null) {
                    Storage storage = StorageManager.update(sample.getStorage());
                    sample.setStorage(storage);
                }
                sampleController.saveSample(sample);
                }

                 */
                result.add(entry.getId());
            }
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return result;
    }

    @Override
    public HashMap<String, ArrayList<String>> retrieveStorageSchemes(String sessionId,
            EntryType type) {

        HashMap<String, ArrayList<String>> schemeMap = new HashMap<String, ArrayList<String>>();

        List<Storage> schemes = StorageManager.getStorageSchemesForEntryType(type.getName());
        for (Storage scheme : schemes) {

            String schemeName = scheme.getName();
            ArrayList<String> schemeOptions = new ArrayList<String>();

            Storage storage;
            try {
                storage = StorageManager.get(scheme.getId(), false);

                if (storage != null && storage.getSchemes() != null) {
                    for (Storage storageScheme : storage.getSchemes()) {
                        schemeOptions.add(storageScheme.getName());
                    }
                }
            } catch (ManagerException e) {
                Logger.error(e);
                continue;
            }

            if (PopulateInitialDatabase.DEFAULT_PLASMID_STORAGE_SCHEME_NAME.equals(schemeName))
                schemeMap.put(schemeName, schemeOptions);
            else if (PopulateInitialDatabase.DEFAULT_STRAIN_STORAGE_SCHEME_NAME.equals(schemeName))
                schemeMap.put(schemeName, schemeOptions);
            else if (PopulateInitialDatabase.DEFAULT_PART_STORAGE_SCHEME_NAME.equals(schemeName))
                schemeMap.put(schemeName, schemeOptions);
            else if (PopulateInitialDatabase.DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME
                    .equals(schemeName))
                schemeMap.put(schemeName, schemeOptions);
        }

        return schemeMap;
    }

    @Override
    public LinkedHashMap<Long, String> retrieveAllAccounts(String sessionId) {

        LinkedHashMap<Long, String> results = null;

        try {
            Set<Account> accounts = AccountController.getAllByFirstName();
            results = new LinkedHashMap<Long, String>();
            for (Account account : accounts) {
                results.put(account.getId(), account.getFullName());
            }
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return results;
    }

    @Override
    public LinkedHashMap<Long, String> retrieveAllGroups(String sessionId) {
        LinkedHashMap<Long, String> results = null;

        try {
            Set<Group> groups = GroupManager.getAll();
            results = new LinkedHashMap<Long, String>();
            for (Group group : groups) {
                results.put(group.getId(), group.getLabel());
            }
        } catch (ManagerException e) {
            Logger.error(e);
        }
        return results;
    }

    @Override
    public ArrayList<PermissionInfo> retrievePermissionData(String sessionId, Long entryId) {

        ArrayList<PermissionInfo> results = null;
        Entry entry = null;

        try {
            entry = EntryManager.get(entryId);
            if (entry == null)
                return null;
        } catch (ManagerException me) {
            Logger.error(me);
        }

        results = new ArrayList<PermissionInfo>();

        try {
            Set<Account> readAccounts = AuthenticatedPermissionManager.getReadUser(entry);
            for (Account account : readAccounts) {
                results.add(new PermissionInfo(PermissionType.READ_ACCOUNT, account.getId(),
                        account.getFullName()));
            }
        } catch (ManagerException me) {
            Logger.error(me);
        } catch (PermissionException pe) {
            Logger.error(pe);
        }

        try {
            Set<Account> writeAccounts = AuthenticatedPermissionManager.getWriteUser(entry);
            for (Account account : writeAccounts) {
                results.add(new PermissionInfo(PermissionType.WRITE_ACCOUNT, account.getId(),
                        account.getFullName()));
            }
        } catch (ManagerException me) {
            Logger.error(me);
        } catch (PermissionException pe) {
            Logger.error(pe);
        }

        try {
            Set<Group> readGroups = AuthenticatedPermissionManager.getReadGroup(entry);
            for (Group group : readGroups) {
                results.add(new PermissionInfo(PermissionType.READ_GROUP, group.getId(), group
                        .getLabel()));
            }
        } catch (ManagerException me) {
            Logger.error(me);
        } catch (PermissionException pe) {
            Logger.error(pe);
        }

        try {
            Set<Group> writeGroups = AuthenticatedPermissionManager.getWriteGroup(entry);
            for (Group group : writeGroups) {
                results.add(new PermissionInfo(PermissionType.WRITE_GROUP, group.getId(), group
                        .getLabel()));
            }
        } catch (ManagerException me) {
            Logger.error(me);
        } catch (PermissionException pe) {
            Logger.error(pe);
        }

        return results;
    }

    @Override
    public ArrayList<NewsItem> retrieveNewsItems(String sessionId) {

        Account account;
        try {
            account = retrieveAccountForSid(sessionId);
            if (account == null)
                return null;
        } catch (ControllerException e) {
            Logger.error(e);
        }

        ArrayList<NewsItem> items = new ArrayList<NewsItem>();

        ArrayList<News> results;
        try {
            results = NewsManager.retrieveAll();
            for (News news : results) {
                NewsItem item = new NewsItem(String.valueOf(news.getId()), news.getCreationTime(),
                        news.getTitle(), news.getBody());
                items.add(item);
            }
        } catch (ManagerException e) {
            Logger.error(e);
        }

        return items;
    }

    @Override
    public NewsItem createNewsItem(String sessionId, NewsItem item) {

        Account account;
        try {
            account = retrieveAccountForSid(sessionId);
            if (account == null)
                return null;
        } catch (ControllerException e) {
            Logger.error(e);
        }

        News news = new News();
        news.setTitle(item.getHeader());
        news.setBody(item.getBody());

        try {
            News saved = NewsManager.save(news);
            item.setCreationDate(saved.getCreationTime());
            item.setId(String.valueOf(saved.getId()));
            return item;
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }

    }

    @Override
    public FolderDetails updateFolder(String sid, long folderId, FolderDetails update) {
        Account account;
        try {
            account = retrieveAccountForSid(sid);
            if (account == null)
                return null;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }

        try {
            Folder folder = FolderManager.get(folderId);
            if (folder == null)
                return null;

            folder.setName(update.getName());
            folder.setDescription(update.getDescription());
            Folder updated = FolderManager.update(folder);
            update.setId(updated.getId());
            return update;

        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public BulkImportDraftInfo saveBulkImportDraft(String sid, String email, String name,
            ArrayList<EntryInfo> info) {

        Account account;
        try {
            account = retrieveAccountForSid(sid);
            if (account == null)
                return null;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }

        BulkImport draft = new BulkImport();

        try {
            Account emailAccount = AccountManager.getByEmail(email);
            draft.setAccount(emailAccount);
            draft.setName(name);
            // TODO : entry infos

            BulkImport result = BulkImportManager.createBulkImportRecord(draft);
            BulkImportDraftInfo draftInfo = new BulkImportDraftInfo();

            draftInfo.setCreated(result.getCreationTime());
            draftInfo.setName(result.getName());
            return draftInfo;

        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }

    }
}

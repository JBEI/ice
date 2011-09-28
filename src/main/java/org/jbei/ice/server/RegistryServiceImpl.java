package org.jbei.ice.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.FolderManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.QueryManager;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Folder;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BlastProgram;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.FilterTrans;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.PlasmidInfo;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BlastResultInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

// TODO : this whole class needs to be redone. The logic needs to be moved to controllers/managers
public class RegistryServiceImpl extends RemoteServiceServlet implements RegistryService {

    private static final long serialVersionUID = 1L;

    @Override
    public AccountInfo login(String name, String pass) {

        String sessionId = null;

        try {
            SessionData sessionData = AccountController.authenticate(name, pass);
            sessionId = sessionData.getSessionKey();
            log("User by login '" + name + "' successfully logged in");

            AccountInfo info = new AccountInfo();
            info.setSessionId(sessionId);
            info.setFirstName(sessionData.getAccount().getFirstName());
            info.setLastName(sessionData.getAccount().getLastName());
            info.setEmail(sessionData.getAccount().getEmail());
            return info;
        } catch (InvalidCredentialsException e) {
            Logger.warn("Invalid credentials provided by user: " + name);
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (Exception e) {
            Logger.error(e);
        }
        return "THIS_IS_A_FAKE_SESSION_dfsafsfas2345435mkldnmg";
    }

    @Override
    public boolean sessionValid(String sid) {
        try {
            return AccountController.isAuthenticated(sid);
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public boolean logout(String sessionId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ArrayList<Long> retrieveSearchResults(ArrayList<FilterTrans> filters) {
        ArrayList<QueryFilter> queryFilters = new ArrayList<QueryFilter>();
        for (FilterTrans filter : filters) {
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
    public ArrayList<EntryData> retrieveEntryData(String sid, ArrayList<Long> entryIds,
            ColumnField type, boolean asc) {

        // TODO Use Controller and put all of this logic in there
        if (type == null)
            type = ColumnField.CREATED;

        try {
            ArrayList<EntryData> results = new ArrayList<EntryData>();
            List<Entry> entries = null;

            Account account = this.retrieveAccountForSid(sid);
            if (account == null)
                return null;

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

                EntryData view = EntryViewFactory.createTipView(entry);
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
    public EntryData retrieveEntryView(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ArrayList<FolderDetails> retrieveCollections(String sessionId) {

        ArrayList<FolderDetails> results = new ArrayList<FolderDetails>();
        try {
            //            Account account = retrieveAccountForSid(sessionId);
            //            if (account == null)
            //                return null;

            // TODO : this needs to be in a manager and another data transfer object 
            // TODO : FolderDetails, used
            Account system = AccountController.getSystemAccount();
            List<Folder> folders = FolderManager.getFoldersByOwner(system);

            for (Folder folder : folders) {
                long id = folder.getId();
                String name = folder.getName();
                FolderDetails details = new FolderDetails(id, name);
                results.add(details);
            }

            // get user accounts also
            //            List<Folder> userFolders = FolderManager.getFoldersByOwner(account);
            //            if (userFolders != null) {
            //                for (Folder folder : userFolders) {
            //                    long id = folder.getId();
            //                    String name = folder.getName();
            //                    FolderDetails details = new FolderDetails(id, name);
            //                    results.add(details);
            //                }
            //            }

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
    public ArrayList<Long> retrieveEntriesForFolder(String sessionId, FolderDetails folder) {
        // TODO :

        //        Account account = this.retrieveAccountForSid(sessionId);
        //        if( account == null )
        //            return null;

        try {
            return FolderManager.getFolderContents(folder.getId(), false);
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
            return entryToInfo(entry);
        } catch (ManagerException e) {
            Logger.error(e);
        }
        return null;
    }

    @Override
    public AccountInfo retrieveAccountInfo(String sid, String userId) {
        try {
            Account account = retrieveAccountForSid(sid);
            if (account == null)
                return null;

            account = AccountManager.getByEmail(userId);
            if (account == null)
                return null;

            return accountToInfo(account);
        } catch (ManagerException e) {
            Logger.error(e);
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return null;
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

    private EntryInfo entryToInfo(Entry entry) {
        if (entry instanceof Plasmid) {
            PlasmidInfo info = new PlasmidInfo();
            Plasmid plasmid = (Plasmid) entry;

            info.setAlias(plasmid.getAlias());
            info.setBackbone(plasmid.getBackbone());
            info.setCreator(plasmid.getCreator());
            info.setCreatorEmail(plasmid.getCreatorEmail());
            info.setPartId(plasmid.getOnePartNumber().getPartNumber());
            info.setName(plasmid.getNamesAsString());
            info.setAlias(plasmid.getAlias());
            info.setBackbone(plasmid.getBackbone());
            info.setBioSafetyLevel(plasmid.getBioSafetyLevel());
            info.setOwner(plasmid.getOwner());
            info.setOwnerEmail(plasmid.getOwnerEmail());
            info.setCircular(plasmid.getCircular());
            info.setShortDescription(plasmid.getShortDescription());

            return info;
        }
        return null;
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
                EntryData view = EntryViewFactory.createTipView(blastResult.getEntry());
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

    @Override
    public ArrayList<StorageInfo> retrieveChildren(String sid, long id) {
        ArrayList<StorageInfo> result = new ArrayList<StorageInfo>();
        List<Storage> list = null;

        // if id == 0 returns root
        try {
            if (id == 0) {
                list = StorageManager.getAllStorageSchemes();
            } else {
                Storage storage = StorageManager.get(id, true);
                list = new LinkedList<Storage>(storage.getChildren());
            }
        } catch (ManagerException e) {
            Logger.error(e);
        }
        if (list == null)
            return null;

        for (Storage storage : list) {
            StorageInfo info = new StorageInfo();
            info.setDisplay(storage.getName());
            info.setId(storage.getId());
            result.add(info);
        }

        return result;
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
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d yyyy");
                    Date memberSinceDate = sample.getCreationTime();
                    info.setCreationTime(dateFormat.format(memberSinceDate));
                    EntryData view = EntryViewFactory.createTipView(sample.getEntry());
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
}

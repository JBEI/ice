package org.jbei.ice.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.RegistryService;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.bulkupload.BulkUploadController;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.StorageController;
import org.jbei.ice.lib.entry.sample.StorageDAO;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.TraceSequenceDAO;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.message.MessageController;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.news.News;
import org.jbei.ice.lib.news.NewsController;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;
import org.jbei.ice.services.webservices.ServiceException;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AccountResults;
import org.jbei.ice.shared.dto.AccountType;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.MessageInfo;
import org.jbei.ice.shared.dto.NewsItem;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;
import org.jbei.ice.shared.dto.autocomplete.AutoCompleteSuggestion;
import org.jbei.ice.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.shared.dto.comment.UserComment;
import org.jbei.ice.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.EntryType;
import org.jbei.ice.shared.dto.entry.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.folder.FolderDetails;
import org.jbei.ice.shared.dto.group.GroupInfo;
import org.jbei.ice.shared.dto.group.GroupType;
import org.jbei.ice.shared.dto.message.MessageList;
import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.permission.PermissionSuggestion;
import org.jbei.ice.shared.dto.search.SearchQuery;
import org.jbei.ice.shared.dto.search.SearchResults;
import org.jbei.ice.shared.dto.user.PreferenceKey;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class RegistryServiceImpl extends RemoteServiceServlet implements RegistryService {

    private static final long serialVersionUID = 1L;

    @Override
    public String getConfigurationSetting(String name) {
        ConfigurationController controller = ControllerFactory.getConfigurationController();
        String value = null;
        try {
            value = controller.getPropertyValue(name);
        } catch (ControllerException e) {
        }
        return value;
    }

    @Override
    public Boolean setConfigurationSetting(String sid, ConfigurationKey key, String value) {
        try {
            Account account = retrieveAccountForSid(sid);
            if (account.getType() != AccountType.ADMIN)
                return false;
            ConfigurationController controller = ControllerFactory.getConfigurationController();
            controller.setPropertyValue(key, value);
            return true;
        } catch (ControllerException | AuthenticationException e) {
            return false;
        }
    }

    @Override
    public boolean setPreferenceSetting(String sid, PreferenceKey key, String value) throws AuthenticationException {
        PreferencesController controller = ControllerFactory.getPreferencesController();
        Account account = retrieveAccountForSid(sid);
        try {
            return controller.saveSetting(account, key, value);
        } catch (ControllerException e) {
            return false;
        }
    }

    @Override
    public HashMap<PreferenceKey, String> retrieveUserPreferences(String sid, ArrayList<PreferenceKey> keys)
            throws AuthenticationException {
        PreferencesController controller = ControllerFactory.getPreferencesController();
        Account account = retrieveAccountForSid(sid);
        try {
            return controller.retrieveAccountPreferences(account, keys);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public BulkUploadAutoUpdate autoUpdateBulkUpload(String sid, BulkUploadAutoUpdate wrapper, EntryAddType addType)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        Logger.info(account.getEmail() + ": " + wrapper.toString());
        BulkUploadController controller = ControllerFactory.getBulkUploadController();
        try {
            return controller.autoUpdateBulkUpload(account, wrapper, addType);
        } catch (ControllerException de) {
            return null;
        }
    }

    @Override
    public Long updateBulkUploadPreference(String sid, long bulkUploadId, EntryAddType addType, PreferenceInfo info)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        Logger.info(account.getEmail() + ": Preference " + info.toString() + " for bulk upload " + bulkUploadId);
        BulkUploadController controller = ControllerFactory.getBulkUploadController();
        try {
            return controller.updatePreference(account, bulkUploadId, addType, info);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public Long updateBulkUploadPermissions(String sid, long id, EntryAddType type,
            ArrayList<PermissionInfo> permissions) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        Logger.info(account.getEmail() + ": updating permissions for bulk upload " + id);
        try {
            return ControllerFactory.getBulkUploadController().updatePermissions(account, id, type, permissions);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public void requestEntryTransfer(String sid, ArrayList<Long> ids, ArrayList<String> sites) {
        Account account;
        try {
            account = retrieveAccountForSid(sid);
            if (!ControllerFactory.getAccountController().isAdministrator(account))
                return;

            Logger.info("Requesting transfer of " + ids.size() + " entries");
        } catch (AuthenticationException | ControllerException e) {
            Logger.error(e);
            return;
        }

        // retrieve entries
        EntryController entryController = ControllerFactory.getEntryController();
        SequenceController sequenceController = ControllerFactory.getSequenceController();

        HashMap<Entry, String> entrySeq = new HashMap<>();
        for (long id : ids) {
            try {
                Entry entry = entryController.get(account, id);
                Sequence sequence = sequenceController.getByEntry(entry);
                String sequenceString = null;
                if (sequence != null) {
                    sequenceString = sequence.getSequenceUser();
                    if (sequenceString == null || sequenceString.isEmpty())
                        sequenceString = sequence.getSequence();
                }
                entrySeq.put(entry, sequenceString);
            } catch (ControllerException e) {
                Logger.error(e);
                continue;
            }
        }

        for (String url : sites) {
            IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(url);
            if (api == null) {
                Logger.error("Could not retrieve api for " + url + ". Transfer aborted");
                continue;
            }

            try {
                api.transmitEntries(entrySeq);
            } catch (ServiceException e) {
                Logger.error(e);
            }
        }
    }

    @Override
    public GroupInfo createNewGroup(String sessionId, GroupInfo info) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            return ControllerFactory.getGroupController().createGroup(account, info);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public GroupInfo updateGroup(String sessionId, GroupInfo info) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": updating group " + info.getId());
        if (info.getType() == null)
            info.setType(GroupType.PRIVATE);

        try {
            return ControllerFactory.getGroupController().updateGroup(account, info);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public GroupInfo deleteGroup(String sessionId, GroupInfo info) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": deleting group " + info.getId());
        GroupController controller = ControllerFactory.getGroupController();
        if (info.getType() == null)
            info.setType(GroupType.PRIVATE);

        try {
            return controller.deleteGroup(account, info);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public boolean removeAccountFromGroup(String sessionId, GroupInfo info, AccountInfo accountInfo)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": removing \"" + accountInfo.getEmail() + "\" from group " + info.getId());
        if (info.getType() == null)
            info.setType(GroupType.PRIVATE);

        try {
            ControllerFactory.getAccountController().removeMemberFromGroup(info.getId(), accountInfo.getEmail());
            return true;
        } catch (ControllerException ce) {
            return false;
        }
    }

    @Override
    public AccountInfo login(String name, String pass) {
        try {
            AccountController controller = ControllerFactory.getAccountController();
            AccountInfo info = controller.authenticate(name, pass);
            if (info == null) {
                return null;
            }

            Logger.info("User by login '" + name + "' successfully logged in");
            Account account = controller.getByEmail(info.getEmail());
            EntryController entryController = ControllerFactory.getEntryController();
            long visibleEntryCount = entryController.getNumberOfVisibleEntries(account);
            info.setVisibleEntryCount(visibleEntryCount);

            // get the count of the user's entries
            long ownerEntryCount = entryController.getNumberOfOwnerEntries(account, account.getEmail());
            info.setUserEntryCount(ownerEntryCount);

            // get new message count
            MessageController messageController = ControllerFactory.getMessageController();
            int count = messageController.getNewMessageCount(account);
            info.setNewMessageCount(count);
            return info;
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (InvalidCredentialsException e) {
            Logger.warn("Invalid credentials provided by " + name);
        }
        return null;
    }

    @Override
    public AccountResults retrieveAllUserAccounts(String sid, int start, int limit) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": retrieving all accounts [" + start + " - " + limit + "]");
            return ControllerFactory.getAccountController().retrieveAccounts(account, start, limit);
        } catch (ControllerException e) {
            Logger.error(e);
        }
        return null;
    }

    @Override
    public boolean handleForgotPassword(String email, String url) throws AuthenticationException {
        AccountController controller = ControllerFactory.getAccountController();
        try {
            Logger.info("Resetting password for user " + email);
            controller.resetPassword(email, true, url);
            return true;
        } catch (ControllerException e) {
            Logger.error("Error resetting password for user " + email, e);
            return false;
        }
    }

    @Override
    public boolean updateAccountPassword(String sid, String email, String password)
            throws AuthenticationException {

        try {
            Account account = retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": updating password for account " + email);
            AccountController controller = ControllerFactory.getAccountController();
            controller.updatePassword(email, password);
            return true;

        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public String createNewAccount(AccountInfo info, boolean sendEmail) {
//        String serverName = getThreadLocalRequest().getServerName();
        try {
            return ControllerFactory.getAccountController().createNewAccount(info, sendEmail);
        } catch (ControllerException e) {
            Logger.error("Error creating new account", e);
            return null;
        }
    }

    @Override
    public AccountInfo retrieveAccount(String email) {
        Account account = null;
        AccountController controller = ControllerFactory.getAccountController();

        try {
            account = controller.getByEmail(email);
        } catch (ControllerException e) {
            Logger.error("Error retrieving account", e);
        }

        return Account.toDTO(account);
    }

    @Override
    public AccountInfo updateAccount(String sid, String email, AccountInfo info) throws AuthenticationException {
        AccountController controller = ControllerFactory.getAccountController();
        try {
            Account account = retrieveAccountForSid(sid);

            // require a user is a moderator or updating self account
            if (!controller.isAdministrator(account) && !email.equals(account.getEmail()))
                return null;

            account = controller.getByEmail(email);
            if (account == null)
                return null;

            account.setIsSubscribed(1);
            account.setModificationTime(Calendar.getInstance().getTime());
            account.setSalt(Utils.generateSaltForUserAccount());
            account.setFirstName(info.getFirstName());
            account.setLastName(info.getLastName());
            account.setInitials(info.getInitials());
            account.setInstitution(info.getInstitution());
            account.setDescription(info.getDescription());
            controller.save(account);
            return info;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public AccountInfo sessionValid(String sid) {
        AccountController controller = ControllerFactory.getAccountController();
        EntryController entryController = ControllerFactory.getEntryController();

        try {
            if (AccountController.isAuthenticated(sid)) {
                Account account = controller.getAccountBySessionKey(sid);
                AccountInfo info = Account.toDTO(account);
                long entryCount = entryController.getNumberOfOwnerEntries(account, account.getEmail());
                info.setUserEntryCount(entryCount);

                boolean isModerator = controller.isAdministrator(account);
                info.setAdmin(isModerator);
                long visibleEntryCount = entryController.getNumberOfVisibleEntries(account);
                info.setVisibleEntryCount(visibleEntryCount);

                // get new message count
                MessageController messageController = ControllerFactory.getMessageController();
                int count = messageController.getNewMessageCount(account);
                info.setNewMessageCount(count);

                return info;
            }
        } catch (ControllerException e) {
            Logger.error(e);
        }
        return null;
    }

    @Override
    public boolean logout(String sessionId) {
        Logger.info("De-authenticating session \"" + sessionId + "\"");
        try {
            AccountController.deauthenticate(sessionId);
            return true;
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public ArrayList<FolderDetails> retrieveCollections(String sessionId) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": retrieving collections");
        try {
            return ControllerFactory.getFolderController().retrieveFoldersForUser(account);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    @Override
    public boolean promoteCollection(String sessionId, long id) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": promoting collection with id " + id);
        try {
            return ControllerFactory.getFolderController().promoteFolder(account, id);
        } catch (ControllerException ce) {
            return false;
        }
    }

    @Override
    public boolean demoteCollection(String sessionId, long id) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": demoting collection with id " + id);
        try {
            return ControllerFactory.getFolderController().demoteFolder(account, id);
        } catch (ControllerException ce) {
            return false;
        }
    }

    @Override
    public FolderDetails retrieveEntriesForFolder(String sessionId, long folderId, ColumnField sort, boolean asc,
            int start, int limit) throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": retrieving entries for folder " + folderId + " from " + start
                                + " with size " + limit);
            FolderController folderController = ControllerFactory.getFolderController();
            return folderController.retrieveFolderContents(folderId, sort, asc, start, limit);
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public FolderDetails deleteFolder(String sessionId, long folderId) throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": deleting folder " + folderId);
            FolderController folderController = ControllerFactory.getFolderController();
            Folder folder = folderController.getFolderById(folderId);
            if (folder == null)
                return null;

            AccountController controller = ControllerFactory.getAccountController();
            Account system = controller.getSystemAccount();
            boolean isSystem = system.getEmail().equals(folder.getOwnerEmail());
            if (isSystem) {
                Logger.info("Cannot delete system folder");
                return null;
            }

            FolderDetails details = new FolderDetails(folder.getId(), folder.getName(), isSystem);
            long folderSize = folderController.getFolderSize(folderId);
            details.setCount(folderSize);
            details.setDescription(folder.getDescription());
            folderController.delete(account, folder);
            return details;
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (PermissionException ce) {
            Logger.warn(ce.getMessage());
        }
        return null;
    }

    @Override
    public FolderDetails retrieveUserEntries(String sid, String userId, ColumnField sort, boolean asc, int start,
            int limit) throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sid);
            EntryController entryController = ControllerFactory.getEntryController();
            FolderDetails details = new FolderDetails(0, "My Entries", true);
            AccountController controller = ControllerFactory.getAccountController();
            Account user = controller.get(Long.decode(userId));
            Logger.info(account.getEmail() + ": retrieving user entries for " + user.getEmail());
            List<Entry> entries = entryController.retrieveOwnerEntries(account, user.getEmail(), sort,
                                                                       asc, start, limit);
            long count = entryController.getNumberOfOwnerEntries(account, user.getEmail());
            details.setCount(count);
            for (Entry entry : entries) {
                EntryInfo info = ModelToInfoFactory.createTableViewData(entry, false);
                details.getEntries().add(info);
            }
            return details;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public FolderDetails retrieveAllVisibleEntrys(String sid, FolderDetails details, ColumnField field, boolean asc,
            int start, int limit) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": retrieving all visible entries from " + start + " with size " + limit);
            EntryController entryController = ControllerFactory.getEntryController();
            FolderDetails retrieved = entryController.retrieveVisibleEntries(account, field, asc, start, limit);
            details.setEntries(retrieved.getEntries());
            if (details.getCount() < 0) {
                long count = entryController.getNumberOfVisibleEntries(account);
                details.setCount(count);
            }
            return details;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<AccountInfo> retrieveAvailableAccounts(String sessionId) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": retrieving available accounts for group creation");
        GroupController controller = ControllerFactory.getGroupController();
        try {
            return controller.retrieveAccountsForGroupCreation(account);
        } catch (ControllerException e) {
            return null;
        }
    }

    protected Account retrieveAccountForSid(String sid) throws AuthenticationException {
        try {
            boolean isAuthenticated = AccountController.isAuthenticated(sid);
            AccountController controller = ControllerFactory.getAccountController();

            if (!isAuthenticated)
                throw new AuthenticationException("Session failed authentication: " + sid);

            return controller.getAccountBySessionKey(sid);
        } catch (ControllerException ce) {
            throw new AuthenticationException();
        }
    }

    @Override
    public SuggestOracle.Response getAutoCompleteSuggestion(AutoCompleteField field, Request request) {
        SuggestOracle.Response response = new SuggestOracle.Response();
        List<Suggestion> suggestions = new ArrayList<>(request.getLimit());

        try {
            EntryController eC = ControllerFactory.getEntryController();
            Set<String> results = eC.getMatchingAutoCompleteField(field, request.getQuery(), request.getLimit());
            for (String result : results) {
                AutoCompleteSuggestion suggestion = new AutoCompleteSuggestion(result);
                suggestions.add(suggestion);
            }
        } catch (ControllerException e) {
            Logger.error(e);
        }

        response.setSuggestions(suggestions);
        return response;
    }

    @Override
    public ArrayList<SequenceAnalysisInfo> retrieveEntryTraceSequences(String sid, long entryId)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Entry entry = ControllerFactory.getEntryController().get(account, entryId);
            if (entry == null)
                return null;

            List<TraceSequence> sequences = TraceSequenceDAO.getByEntry(entry);
            return ModelToInfoFactory.getSequenceAnalysis(sequences);

        } catch (ControllerException | DAOException e) {
            Logger.error(e);
        }

        return null;
    }

    @Override
    public ArrayList<SequenceAnalysisInfo> deleteEntryTraceSequences(String sid, long entryId, ArrayList<String> fileId)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Entry entry = ControllerFactory.getEntryController().get(account, entryId);
            if (entry == null)
                return null;

            SequenceAnalysisController controller = ControllerFactory.getSequenceAnalysisController();
            for (String id : fileId) {
                TraceSequence sequence = controller.getTraceSequenceByFileId(id);
                if (sequence == null) {
                    Logger.warn("Could not retrieve trace sequence by file Id " + id);
                    continue;
                }
                controller.removeTraceSequence(account, sequence);
            }
            List<TraceSequence> sequences = TraceSequenceDAO.getByEntry(entry);
            return ModelToInfoFactory.getSequenceAnalysis(sequences);

        } catch (ControllerException | DAOException e) {
            Logger.error(e);
        } catch (PermissionException ce) {
            Logger.warn(ce.getMessage());
        }

        return null;
    }

    @Override
    public EntryInfo retrieveEntryDetails(String sid, long id, String recordId, String url)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);

        try {
            Entry entry;
            if (recordId == null || recordId.isEmpty()) {
                entry = ControllerFactory.getEntryController().get(account, id);
                return ControllerFactory.getEntryController().retrieveEntryDetails(account, entry);
            }

            if (url != null) {
                IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(url);
                entry = api.getPublicEntryByRecordId(recordId);
                boolean hasSequence = api.hasSequence(entry.getRecordId());
                boolean hasOriginalSequence = api.hasOriginalSequence(entry.getRecordId());
                return ModelToInfoFactory.getInfo(null, entry, null, null, null, hasSequence, hasOriginalSequence);
            }

            entry = ControllerFactory.getEntryController().getByRecordId(account, recordId);
            return ControllerFactory.getEntryController().retrieveEntryDetails(account, entry);
        } catch (ControllerException | ServiceException ce) {
            Logger.error(ce);
        } catch (PermissionException ce) {
            Logger.warn(ce.getMessage());
        }
        return null;
    }

    @Override
    public EntryInfo retrieveEntryTipDetails(String sid, String recordId, String url) throws AuthenticationException {
        Account account = null;
        if (url == null) {
            try {
                account = retrieveAccountForSid(sid);
                Entry entry;
                try {
                    entry = ControllerFactory.getEntryController().getByRecordId(account, recordId);
                } catch (PermissionException e) {
                    Logger.warn(account.getEmail() + ": No read permission for " + recordId);
                    return null;
                }

                Logger.info(account.getEmail() + ": retrieving entry tip details for " + entry.getId());
                return ModelToInfoFactory.createTipView(account, entry);
            } catch (ControllerException e) {
                Logger.error(e);
                return null;
            }
        }

        IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(url);
        try {
            Entry entry = api.getPublicEntryByRecordId(recordId);
            return ModelToInfoFactory.createTipView(account, entry);
        } catch (ServiceException e) {
            Logger.error(e);
        }
        return null;
    }

    @Override
    public SearchResults performSearch(String sid, SearchQuery query, boolean isWeb) throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sid);
            SearchController search = ControllerFactory.getSearchController();
            SearchResults searchResults = search.runSearch(account, query, isWeb);
            if (searchResults == null)
                return null;
            searchResults.setQuery(query);
            return searchResults;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public FolderDetails createUserCollection(String sid, String name, String description,
            ArrayList<Long> contents) throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sid);
            EntryController entryController = ControllerFactory.getEntryController();
            FolderController folderController = ControllerFactory.getFolderController();
            Logger.info(account.getEmail() + ": creating new folder with name " + name);

            Folder folder = folderController.createNewFolder(account.getEmail(), name, description);

            FolderDetails details = new FolderDetails(folder.getId(), folder.getName(), false);
            details.setDescription(folder.getDescription());

            if (contents != null && !contents.isEmpty()) {

                ArrayList<Entry> entrys = new ArrayList<>(entryController.getEntriesByIdSet(account, contents));
                folderController.addFolderContents(folder.getId(), entrys);
//                details.setAccountInfo(contents);
                details.setCount(contents.size());
            } else {
                details.setCount(0l);
            }

            return details;
        } catch (ControllerException e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public ArrayList<FolderDetails> moveToUserCollection(String sid, long source,
            ArrayList<Long> destination, ArrayList<Long> entryIds) throws AuthenticationException {
        Account account = this.retrieveAccountForSid(sid);
        Logger.info(account.getEmail() + ": moving entries to user collection.");
        EntryController entryController = ControllerFactory.getEntryController();
        FolderController folderController = ControllerFactory.getFolderController();
        ArrayList<Entry> entrys;

        try {
            entrys = new ArrayList<>(entryController.getEntriesByIdSet(account, entryIds));
            if (folderController.removeFolderContents(account, source, entryIds) == null) {
                return null;
            }
        } catch (ControllerException ce) {
            return null;
        }

        ArrayList<FolderDetails> results = new ArrayList<>();

        for (long folderId : destination) {
            try {
                Folder folder = folderController.addFolderContents(folderId, entrys);
                FolderDetails details = new FolderDetails(folder.getId(), folder.getName(), false);
                long folderSize = folderController.getFolderSize(folder.getId());
                details.setCount(folderSize + entryIds.size());
                details.setDescription(folder.getDescription());
                results.add(details);
            } catch (ControllerException ce) {
                continue;
            }
        }

        try {
            Folder sourceFolder = folderController.getFolderById(source);
            long folderSize = folderController.getFolderSize(source);
            FolderDetails sourceDetails = new FolderDetails(sourceFolder.getId(), sourceFolder.getName(), false);
            sourceDetails.setCount(folderSize - entryIds.size());
            results.add(sourceDetails);
            return results;
        } catch (ControllerException ce) {
            return null;
        }
    }

    @Override
    public FolderDetails removeFromUserCollection(String sid, long source, ArrayList<Long> entryIds)
            throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": removing from user collection.");
            FolderController folderController = ControllerFactory.getFolderController();

            Folder folder = folderController.removeFolderContents(account, source, entryIds);
            if (folder == null)
                return null;

            FolderDetails details = new FolderDetails(folder.getId(), folder.getName(), false);
            long folderSize = folderController.getFolderSize(source);
            details.setCount(folderSize - entryIds.size());
            details.setDescription(folder.getDescription());
            return details;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<FolderDetails> addEntriesToCollection(String sid, ArrayList<Long> destination,
            ArrayList<Long> entryIds) throws AuthenticationException {
        ArrayList<FolderDetails> results = new ArrayList<>();

        try {
            Account account = this.retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": adding entries to collection");
            EntryController entryController = ControllerFactory.getEntryController();
            FolderController folderController = ControllerFactory.getFolderController();

            ArrayList<Entry> entrys = new ArrayList<>(entryController.getEntriesByIdSet(account, entryIds));
            for (long folderId : destination) {
                long size = folderController.getFolderSize(folderId);
                Folder folder = folderController.addFolderContents(folderId, entrys);
                FolderDetails details = new FolderDetails(folder.getId(), folder.getName(), false);
                details.setCount(size + entryIds.size());
                details.setDescription(folder.getDescription());
                results.add(details);
            }
            return results;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public AccountInfo retrieveProfileInfo(String sid, String userId) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": retrieving profile info for " + userId);
            AccountController controller = ControllerFactory.getAccountController();
            EntryController entryController = ControllerFactory.getEntryController();
            account = controller.get(Long.decode(userId));
            if (account == null)
                return null;

            AccountInfo accountInfo = Account.toDTO(account);
            long visibleEntryCount = entryController.getNumberOfVisibleEntries(account);
            accountInfo.setVisibleEntryCount(visibleEntryCount);
            long entryCount = entryController.getNumberOfOwnerEntries(account, account.getEmail());
            accountInfo.setUserEntryCount(entryCount);
            return accountInfo;
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public boolean removeSequence(String sid, long entryId) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Entry entry;
            EntryController entryController = ControllerFactory.getEntryController();

            entry = entryController.get(account, entryId);
            if (entry == null) {
                Logger.info("Could not retrieve entry with id " + entryId);
                return false;
            }

            SequenceController sequenceController = ControllerFactory.getSequenceController();
            Sequence sequence = sequenceController.getByEntry(entry);

            if (sequence != null) {
                try {
                    sequenceController.delete(account, sequence);
                    Logger.info("User '" + account.getEmail() + "' removed sequence: '" + entryId
                                        + "'");
                    return true;
                } catch (PermissionException e) {
                    Logger.warn(account.getEmail() + " attempting to delete sequence for entry "
                                        + entryId + " but does not have permissions");
                }
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
        return false;
    }

    @Override
    public ArrayList<BulkUploadInfo> retrieveUserSavedDrafts(String sid) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            BulkUploadController controller = ControllerFactory.getBulkUploadController();
            Logger.info(account.getEmail() + ": retrieve user saved drafts");
            ArrayList<BulkUploadInfo> result = controller.retrieveByUser(account, account);
            return result;
        } catch (Exception ce) {
            return null;
        }
    }

    @Override
    public ArrayList<BulkUploadInfo> retrieveDraftsPendingVerification(String sid) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            AccountController accountController = ControllerFactory.getAccountController();
            if (!accountController.isAdministrator(account)) {
                Logger.warn(account.getEmail() + ": attempting to retrieve pending drafts. Admin only function.");
                return null;
            }

            BulkUploadController controller = ControllerFactory.getBulkUploadController();
            Logger.info(account.getEmail() + ": retrieving drafts pending verification");
            ArrayList<BulkUploadInfo> result = controller.retrievePendingImports(account);
            return result;
        } catch (Exception ce) {
            return null;
        }
    }

    @Override
    public boolean revertedSubmittedBulkUpload(String sid, long uploadId) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            AccountController accountController = ControllerFactory.getAccountController();
            if (!accountController.isAdministrator(account)) {
                Logger.warn(account.getEmail() + ": attempting revert bulk upload. Admin only function.");
                return false;
            }

            BulkUploadController controller = ControllerFactory.getBulkUploadController();
            Logger.info(account.getEmail() + ": reverting submitted bulk upload " + uploadId);
            return controller.revertSubmitted(account, uploadId);

        } catch (ControllerException ce) {
            Logger.error(ce);
            return false;
        }
    }

    @Override
    public BulkUploadInfo deleteSavedDraft(String sid, long draftId) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            BulkUploadController draftController = ControllerFactory.getBulkUploadController();
            Logger.info(account.getEmail() + ": deleting bulk import draft with id " + draftId);
            BulkUploadInfo result = draftController.deleteDraftById(account, draftId);
            return result;
        } catch (Exception ce) {
            return null;
        }
    }

    @Override
    public BulkUploadInfo retrieveBulkImport(String sid, long id, int start, int limit) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        BulkUploadController controller = ControllerFactory.getBulkUploadController();

        try {
            Logger.info(account.getEmail() + ": retrieving bulk import with id \"" + id + "\"");
            BulkUploadInfo result = controller.retrieveById(account, id, start, limit);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean submitBulkUploadDraft(String sid, long draftId) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": submitting bulk import draft \"" + draftId);
            BulkUploadController controller = ControllerFactory.getBulkUploadController();
            try {
                return controller.submitBulkImportDraft(account, draftId);
            } catch (PermissionException e) {
                Logger.warn(e.getMessage());
                return false;
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
            return false;
        }
    }

    @Override
    public HashMap<String, String> retrieveSystemSettings(String sid) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            AccountController controller = ControllerFactory.getAccountController();
            if (!controller.isAdministrator(account))
                return null;

            Logger.info(account.getEmail() + " retrieving system settings");
            ConfigurationController configurationController = ControllerFactory.getConfigurationController();
            HashMap<String, String> settings = configurationController.retrieveSystemSettings();
            return settings;
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public HashMap<String, String> retrieveWebOfRegistrySettings(String sid) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            AccountController controller = ControllerFactory.getAccountController();
            if (!controller.isAdministrator(account))
                return null;

            Logger.info(account.getEmail() + " retrieving web of registry system settings");
            ConfigurationController configurationController = ControllerFactory.getConfigurationController();
            HashMap<String, String> settings = new HashMap<>();

            String v = configurationController.getPropertyValue(ConfigurationKey.WEB_PARTNERS);
            settings.put(ConfigurationKey.WEB_PARTNERS.name(), v);
            v = configurationController.getPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
            settings.put(ConfigurationKey.JOIN_WEB_OF_REGISTRIES.name(), v);
            return settings;
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public boolean isWebOfRegistriesEnabled() {
        ConfigurationController configurationController = ControllerFactory.getConfigurationController();
        try {
            String value = configurationController.getPropertyValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
            return value.equalsIgnoreCase("yes");
        } catch (ControllerException e) {
            return false;
        }
    }

    @Override
    public MessageList retrieveMessages(String sessionId, int start, int count) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        MessageController controller = ControllerFactory.getMessageController();
        try {
            return controller.retrieveMessages(account, account, start, count);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public int markMessageRead(String sessionId, long id) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        MessageController controller = ControllerFactory.getMessageController();
        try {
            return controller.markMessageAsRead(account, id);
        } catch (ControllerException ce) {
            return -1;
        }
    }

    @Override
    public boolean setBulkUploadDraftName(String sessionId, long id, String draftName) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        try {
            Logger.info(account.getEmail() + ": renaming bulk import \"" + id + "\" to " + draftName);
            return ControllerFactory.getBulkUploadController().renameDraft(account, id, draftName);
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public boolean addWebPartner(String sessionId, String webPartner) {
        try {
            Account account = retrieveAccountForSid(sessionId);
            AccountController accountController = ControllerFactory.getAccountController();
            if (!accountController.isAdministrator(account))
                return false;

            Logger.info(account.getEmail() + ": adding web partner " + webPartner);
            ConfigurationController configurationController = ControllerFactory.getConfigurationController();
            String value = configurationController.getPropertyValue(ConfigurationKey.WEB_PARTNERS);
            if (value == null || value.isEmpty())
                value = webPartner;
            else
                value += (";" + webPartner);
            configurationController.setPropertyValue(ConfigurationKey.WEB_PARTNERS, value);
            return true;
        } catch (ControllerException | AuthenticationException e) {
            return false;
        }
    }

    @Override
    public ArrayList<AccountInfo> retrieveGroupMembers(String sessionId, GroupInfo info)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + " retrieving members for group " + info.getLabel());
            GroupController controller = ControllerFactory.getGroupController();
            ArrayList<AccountInfo> result = controller.retrieveGroupMembers(info.getUuid());
            return result;
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public ArrayList<GroupInfo> retrieveUserGroups(String sessionId) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": retrieving user groups");
            return ControllerFactory.getGroupController().retrieveUserGroups(account);
        } catch (ControllerException ce) {
            return null;
        }
    }

    @Override
    public ArrayList<AccountInfo> setGroupMembers(String sessionId, GroupInfo info, ArrayList<AccountInfo> members)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": adding " + members.size() + " members to group " + info.getId());
            GroupController groupController = ControllerFactory.getGroupController();
            return groupController.setGroupMembers(account, info, members);
        } catch (ControllerException ce) {
            return null;
        }
    }

    @Override
    public boolean approvePendingBulkImport(String sessionId, long id)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            AccountController accountController = ControllerFactory.getAccountController();
            if (!accountController.isAdministrator(account)) {
                Logger.error(account.getEmail() + ": non-admin attempt to approve bulk upload");
                return false;
            }

            Logger.info(account.getEmail() + ": approving bulk import with id \"" + id + "\"");
            BulkUploadController controller = ControllerFactory.getBulkUploadController();
            return controller.approveBulkImport(account, id);
        } catch (ControllerException ce) {
            Logger.error(ce);
        } catch (PermissionException ce) {
            Logger.warn(ce.getMessage());
        }
        return false;
    }

    @Override
    public Long createEntry(String sid, EntryInfo info) throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": creating new entry");
            EntryController controller = ControllerFactory.getEntryController();
            Entry entry = InfoToModelFactory.infoToEntry(info);

            SampleController sampleController = ControllerFactory.getSampleController();
            StorageController storageController = ControllerFactory.getStorageController();
            ArrayList<SampleStorage> sampleMap = info.getSampleStorage();

            if (info.getInfo() != null) {
                Entry enclosed = InfoToModelFactory.infoToEntry(info.getInfo());
                controller.createStrainWithPlasmid(account, entry, enclosed, info.getPermissions());
            } else
                entry = controller.createEntry(account, entry, info.getPermissions());

            if (sampleMap != null) {
                for (SampleStorage sampleStorage : sampleMap) {
                    SampleInfo sampleInfo = sampleStorage.getSample();
                    LinkedList<StorageInfo> locations = sampleStorage.getStorageList();

                    Sample sample = sampleController.createSample(sampleInfo.getLabel(),
                                                                  account.getEmail(), sampleInfo.getNotes());
                    sample.setEntry(entry);

                    if (locations == null || locations.isEmpty()) {
                        // create sample, but not location
                        try {
                            Logger.info("Creating sample without location");
                            sampleController.saveSample(account, sample);
                        } catch (PermissionException e) {
                            Logger.warn(e.getMessage());
                            sample = null;
                        } catch (ControllerException e) {
                            Logger.error(e);
                            sample = null;
                        }
                    } else {
                        // create sample and location
                        String[] labels = new String[locations.size()];
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < labels.length; i++) {
                            labels[i] = locations.get(i).getDisplay();
                            sb.append(labels[i]);
                            if (i - 1 < labels.length)
                                sb.append("/");
                        }

                        Logger.info("Creating sample with locations " + sb.toString());
                        Storage storage;
                        try {
                            Storage scheme = storageController.get(Long.parseLong(sampleInfo.getLocationId()), false);
                            storage = storageController.getLocation(scheme, labels);
                            storage = storageController.update(storage);
                            sample.setStorage(storage);
                        } catch (NumberFormatException | ControllerException e) {
                            Logger.error(e);
                            continue;
                        }
                    }

                    if (sample != null) {
                        try {
                            sampleController.saveSample(account, sample);
                        } catch (ControllerException e) {
                            Logger.error(e);
                        } catch (PermissionException ce) {
                            Logger.warn(ce.getMessage());
                        }
                    }
                }
            }

            // save attachments
            if (info.getAttachments() != null) {
                AttachmentController attachmentController = ControllerFactory.getAttachmentController();
                String attDir = Utils.getConfigValue(ConfigurationKey.ATTACHMENTS_DIRECTORY);
                for (AttachmentInfo attachmentInfo : info.getAttachments()) {
                    Attachment attachment = new Attachment();
                    attachment.setEntry(entry);
                    attachment.setDescription(attachmentInfo.getDescription());
                    attachment.setFileName(attachmentInfo.getFilename());
                    File file = new File(attDir + File.separator + attachmentInfo.getFileId());
                    if (!file.exists())
                        continue;
                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        attachmentController.save(account, attachment, inputStream);
                    } catch (FileNotFoundException e) {
                        Logger.warn(e.getMessage());
                        continue;
                    }
                }
            }
            return entry.getId();
        } catch (ControllerException e) {
            Logger.error(e);
            return 0l;
        }
    }

    @Override
    public boolean deleteSample(String sessionId, SampleInfo info) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": deleting sample " + info.getSampleId());
        SampleController sampleController = ControllerFactory.getSampleController();
        try {
            long id = Long.decode(info.getSampleId());
            Sample sample = sampleController.getSampleById(id);
            sampleController.deleteSample(account, sample);
            return true;
        } catch (ControllerException | NumberFormatException he) {
            Logger.error(he);
            return false;
        } catch (PermissionException pe) {
            Logger.warn(pe.getMessage());
            return false;
        }
    }

    @Override
    public SampleStorage createSample(String sessionId, SampleStorage sampleStorage, long entryId)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": creating sample for entry with id " + entryId);

        EntryController controller = ControllerFactory.getEntryController();
        SampleController sampleController = ControllerFactory.getSampleController();
        StorageController storageController = ControllerFactory.getStorageController();

        Entry entry;
        try {
            entry = controller.get(account, entryId);
            if (entry == null) {
                Logger.error("Could not retrieve entry with id " + entryId + ". Skipping sample creation");
                return null;
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }

        SampleInfo sampleInfo = sampleStorage.getSample();
        LinkedList<StorageInfo> locations = sampleStorage.getStorageList();

        Sample sample = sampleController.createSample(sampleInfo.getLabel(), account.getEmail(), sampleInfo.getNotes());
        sample.setEntry(entry);

        if (locations == null || locations.isEmpty()) {
            Logger.info("Creating sample without location");

            // create sample, but not location
            try {
                sample = sampleController.saveSample(account, sample);
                sampleStorage.getSample().setSampleId(sample.getId() + "");
                sampleStorage.getSample().setDepositor(account.getEmail());
                return sampleStorage;
            } catch (ControllerException e) {
                Logger.error(e);
            } catch (PermissionException ce) {
                Logger.warn(ce.getMessage());
            }
            return null;
        }

        // create sample and location
        String[] labels = new String[locations.size()];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labels.length; i++) {
            labels[i] = locations.get(i).getDisplay();
            sb.append(labels[i]);
            if (i - 1 < labels.length)
                sb.append("/");
        }

        Logger.info("Creating sample with locations " + sb.toString());
        try {
            Storage scheme = storageController.get(Long.parseLong(sampleInfo.getLocationId()), false);
            Storage storage = storageController.getLocation(scheme, labels);
            storage = storageController.update(storage);
            sample.setStorage(storage);
            sample = sampleController.saveSample(account, sample);
            sampleStorage.getStorageList().clear();

            List<Storage> storages = StorageDAO.getStoragesUptoScheme(storage);
            if (storages != null) {
                for (Storage storage1 : storages) {
                    StorageInfo info = new StorageInfo();
                    info.setDisplay(storage1.getIndex());
                    info.setId(storage1.getId());
                    info.setType(storage1.getStorageType().name());
                    sampleStorage.getStorageList().add(info);
                }
            }

            sampleStorage.getSample().setSampleId(sample.getId() + "");
            sampleStorage.getSample().setDepositor(account.getEmail());
            return sampleStorage;
        } catch (NumberFormatException | ControllerException e) {
            Logger.error(e);
        } catch (PermissionException ce) {
            Logger.warn(ce.getMessage());
        }

        return null;
    }

    @Override
    public boolean updateEntry(String sid, EntryInfo info) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);

            Logger.info(account.getEmail() + ": updating entry " + info.getId());
            EntryController controller = ControllerFactory.getEntryController();
            Entry existing = controller.getByRecordId(account, info.getRecordId());

            Entry entry = InfoToModelFactory.infoToEntry(info, existing);
            controller.update(account, entry);
            return true;
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (PermissionException ce) {
            Logger.warn(ce.getMessage());
        }
        return false;
    }

    @Override
    public HashMap<SampleInfo, ArrayList<String>> retrieveStorageSchemes(String sessionId,
            EntryType type) throws AuthenticationException {
        retrieveAccountForSid(sessionId);
        HashMap<SampleInfo, ArrayList<String>> schemeMap = new HashMap<>();
        StorageController storageController = ControllerFactory.getStorageController();
        List<Storage> schemes;
        try {
            schemes = storageController.getStorageSchemesForEntryType(type.getName());
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }

        for (Storage scheme : schemes) {
            SampleInfo sampleInfo = new SampleInfo();
            sampleInfo.setLocation(scheme.getName());
            sampleInfo.setLocationId(String.valueOf(scheme.getId()));

            ArrayList<String> schemeOptions = new ArrayList<>();
            try {
                Storage storage = storageController.get(scheme.getId(), false);

                if (storage != null && storage.getSchemes() != null) {
                    for (Storage storageScheme : storage.getSchemes()) {
                        schemeOptions.add(storageScheme.getName());
                    }
                }
            } catch (ControllerException e) {
                Logger.error(e);
                continue;
            }

            sampleInfo.setLabel(scheme.getName());
            schemeMap.put(sampleInfo, schemeOptions);
        }

        return schemeMap;
    }

    @Override
    public SuggestOracle.Response getPermissionSuggestions(Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        List<Suggestion> suggestions = new ArrayList<>(req.getLimit());

        try {
            // TODO : split tokens if there are spaces. this is for a manager
            AccountController controller = ControllerFactory.getAccountController();
            Set<Account> accounts = controller.getMatchingAccounts(req.getQuery(), req.getLimit());
            for (Account account : accounts) {
                PermissionInfo info = new PermissionInfo();
                info.setDisplay(account.getFullName());
                info.setArticle(PermissionInfo.Article.ACCOUNT);
                info.setArticleId(account.getId());
                PermissionSuggestion object = new PermissionSuggestion(info);
                suggestions.add(object);
            }
            GroupController groupController = ControllerFactory.getGroupController();
            Set<Group> groups = groupController.getMatchingGroups(req.getQuery(), req.getLimit());
            for (Group group : groups) {
                PermissionInfo info = new PermissionInfo();
                info.setDisplay(group.getLabel());
                info.setArticle(PermissionInfo.Article.GROUP);
                info.setArticleId(group.getId());
                PermissionSuggestion object = new PermissionSuggestion(info);
                suggestions.add(object);
            }
        } catch (ControllerException e) {
            Logger.error(e);
        }

        resp.setSuggestions(suggestions);
        return resp;
    }

    @Override
    public ArrayList<NewsItem> retrieveNewsItems(String sessionId) throws AuthenticationException {
        retrieveAccountForSid(sessionId);
        ArrayList<NewsItem> items = new ArrayList<>();
        try {
            ArrayList<News> results = new NewsController().retrieveAll();
            for (News news : results) {
                NewsItem item = new NewsItem(String.valueOf(news.getId()), news.getCreationTime(),
                                             news.getTitle(), news.getBody());
                items.add(item);
            }
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return items;
    }

    @Override
    public NewsItem createNewsItem(String sessionId, NewsItem item) throws AuthenticationException {
        try {
            retrieveAccountForSid(sessionId);
            News news = new News();
            news.setTitle(item.getHeader());
            news.setBody(item.getBody());

            NewsController controller = new NewsController();
            News saved = controller.save(news);
            item.setCreationDate(saved.getCreationTime());
            item.setId(String.valueOf(saved.getId()));
            return item;
        } catch (ControllerException e) {
            Logger.error(e);
        }

        return null;
    }

    @Override
    public FolderDetails updateFolder(String sid, long folderId, FolderDetails update) throws AuthenticationException {
        Account account;
        try {
            FolderController folderController = ControllerFactory.getFolderController();
            account = retrieveAccountForSid(sid);
            Folder folder = folderController.getFolderById(folderId);
            if (folder == null)
                return null;

            Logger.info(account.getEmail() + ": updating folder " + folder.getName() + " with id " + folder.getId());
            folder.setName(update.getName());
            folder.setDescription(update.getDescription());
            Folder updated = folderController.updateFolder(folder);
            update.setId(updated.getId());
            return update;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public boolean addPermission(String sessionId, PermissionInfo permissionInfo) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": adding permissions " + permissionInfo.toString());
            PermissionsController permissionController = ControllerFactory.getPermissionController();
            permissionController.addPermission(account, permissionInfo);
            return true;
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public boolean removePermission(String sessionId, PermissionInfo permissionInfo) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": removing permissions " + permissionInfo.toString());
            PermissionsController permissionController = ControllerFactory.getPermissionController();
            permissionController.removePermission(account, permissionInfo);
            return true;
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public boolean saveSequence(String sessionId, long entryId, String sequenceUser) throws AuthenticationException {
        Account account;
        Entry entry;
        try {
            account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": saving sequence for entry " + entryId);
            EntryController entryController = ControllerFactory.getEntryController();
            entry = entryController.get(account, entryId);
            if (entry == null) {
                Logger.error("Could not retrieve entry with id " + entryId);
                return false;
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }

        SequenceController sequenceController = ControllerFactory.getSequenceController();
        IDNASequence dnaSequence = SequenceController.parse(sequenceUser);

        if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
            String errorMsg = "Couldn't parse sequence file! Supported formats: "
                    + GeneralParser.getInstance().availableParsersToString() + ". "
                    + "If you believe this is an error, please contact the administrator with your file";

            Logger.error(errorMsg);
            return false;
        }

        try {
            Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
            sequence.setSequenceUser(sequenceUser);
            sequence.setEntry(entry);
            return sequenceController.save(account, sequence) != null;
        } catch (ControllerException e) {
            Logger.error(e);
        } catch (PermissionException e) {
            Logger.warn(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean sendFeedback(String email, String message) {
        Emailer.send(email, Utils.getConfigValue(ConfigurationKey.PROJECT_NAME),
                     "Thank you for sending your feedback.\n\nBest regards,\nRegistry Team");
        Emailer.send(Utils.getConfigValue(ConfigurationKey.ADMIN_EMAIL), "Registry site feedback", message);
        return true;
    }

    @Override
    public ArrayList<GroupInfo> retrieveGroups(String sid, GroupType type) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        Logger.info(account.getEmail() + ": retrieving " + type.toString() + " groups");
        GroupController controller = ControllerFactory.getGroupController();
        try {
            return controller.retrieveGroups(account, type);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public boolean deleteEntryAttachment(String sid, String fileId) throws AuthenticationException {
        Account account;
        try {
            account = retrieveAccountForSid(sid);
            AttachmentController controller = ControllerFactory.getAttachmentController();
            Attachment attachment = controller.getAttachmentByFileId(fileId);
            if (attachment == null)
                return false;

            controller.delete(account, attachment);
            return true;
        } catch (Exception ce) {
            Logger.error(ce);
        }
        return false;
    }

    @Override
    public ArrayList<FolderDetails> deleteEntry(String sessionId, EntryInfo info)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": deleting entry " + info.getId());
            EntryController controller = ControllerFactory.getEntryController();
            AccountController accountController = ControllerFactory.getAccountController();
            FolderController folderController = ControllerFactory.getFolderController();

            Entry entry = controller.get(account, info.getId());
            if (entry == null)
                return null;

            controller.delete(account, entry);

            ArrayList<FolderDetails> folderList = new ArrayList<>();
            List<Folder> folders = folderController.getFoldersByEntry(entry);
            String systemEmail = accountController.getSystemAccount().getEmail();
            ArrayList<Long> entryIds = new ArrayList<>();
            entryIds.add(entry.getId());
            if (folders != null) {
                for (Folder folder : folders) {
                    try {
                        Folder returned = folderController.removeFolderContents(account, folder.getId(), entryIds);
                        boolean isSystem = systemEmail.equals(returned.getOwnerEmail());
                        FolderDetails details = new FolderDetails(returned.getId(), returned.getName(), isSystem);
                        long size = folderController.getFolderSize(folder.getId());
                        details.setCount(size);
                        folderList.add(details);
                    } catch (ControllerException me) {
                        continue;
                    }
                }
            }

            return folderList;

        } catch (ControllerException ce) {
            Logger.error(ce);
        } catch (PermissionException e) {
            Logger.warn(e.getMessage());
        }
        return null;
    }

    @Override
    public Boolean sendMessage(String sid, MessageInfo info) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": sending message");
            ControllerFactory.getMessageController().sendMessage(account, info);
            return true;
        } catch (ControllerException ce) {
            return false;
        }
    }

    @Override
    public Boolean rebuildSearchIndex(String sid) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        return ControllerFactory.getSearchController().rebuildIndexes(account);
    }

    @Override
    public UserComment sendComment(String sid, UserComment comment) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        Logger.info(account.getEmail() + ": adding comment to entry " + comment.getEntryId());
        try {
            return ControllerFactory.getEntryController().addCommentToEntry(account, comment);
        } catch (ControllerException e) {
            return null;
        }
    }
}

package org.jbei.ice.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.service.RegistryService;
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
import org.jbei.ice.lib.entry.EntryTransfers;
import org.jbei.ice.lib.entry.EntryUtil;
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
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.ExportAsOption;
import org.jbei.ice.lib.shared.dto.AccountResults;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.NewsItem;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.StorageInfo;
import org.jbei.ice.lib.shared.dto.autocomplete.AutoCompleteSuggestion;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.shared.dto.comment.UserComment;
import org.jbei.ice.lib.shared.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;
import org.jbei.ice.lib.shared.dto.entry.Visibility;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;
import org.jbei.ice.lib.shared.dto.folder.FolderType;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.message.MessageInfo;
import org.jbei.ice.lib.shared.dto.message.MessageList;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
import org.jbei.ice.lib.shared.dto.permission.PermissionSuggestion;
import org.jbei.ice.lib.shared.dto.search.IndexType;
import org.jbei.ice.lib.shared.dto.search.SearchBoostField;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.search.SearchResults;
import org.jbei.ice.lib.shared.dto.user.AccountType;
import org.jbei.ice.lib.shared.dto.user.PreferenceKey;
import org.jbei.ice.lib.shared.dto.user.User;
import org.jbei.ice.lib.shared.dto.web.RegistryPartner;
import org.jbei.ice.lib.shared.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;
import org.jbei.ice.lib.utils.IceXlsSerializer;
import org.jbei.ice.lib.utils.IceXmlSerializer;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.io.FileUtils;

// TODO : use command pattern to split this up
public class RegistryServiceImpl extends RemoteServiceServlet implements RegistryService {

    private static final long serialVersionUID = 1L;

    @Override
    public String getConfigurationSetting(String name) {
        ConfigurationController controller = ControllerFactory.getConfigurationController();
        String value = null;
        try {
            value = controller.getPropertyValue(name);
        } catch (ControllerException e) {
            Logger.error(e);
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
    public boolean setPreferenceSetting(String sid, String key, String value) throws AuthenticationException {
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
    public ArrayList<AccessPermission> retrieveDefaultPermissions(String sid) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        try {
            return ControllerFactory.getPermissionController().getDefaultPermissions(account);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public HashMap<String, String> retrieveUserSearchPreferences(String sid) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        try {
            List<SearchBoostField> searchBoostFields = Arrays.asList(SearchBoostField.values());
            return ControllerFactory.getPreferencesController().retrieveUserPreferenceList(account, searchBoostFields);
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
            return controller.autoUpdateBulkUpload(account.getEmail(), wrapper, addType);
        } catch (ControllerException de) {
            return null;
        }
    }

    @Override
    public Long updateBulkUploadPreference(String sid, long bulkUploadId, EntryAddType addType, PreferenceInfo info)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        Logger.info(account.getEmail() + ": preference " + info.toString() + " for bulk upload " + bulkUploadId);
        BulkUploadController controller = ControllerFactory.getBulkUploadController();
        try {
            return controller.updatePreference(account, bulkUploadId, addType, info);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public void requestEntryTransfer(String sid, ArrayList<Long> ids, ArrayList<String> sites)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        try {
            new EntryTransfers().transferEntries(account, ids, sites);
        } catch (ControllerException e) {
            Logger.error(e);
        }
    }

    @Override
    public UserGroup createNewGroup(String sessionId, UserGroup user) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            return ControllerFactory.getGroupController().createGroup(account, user);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public UserGroup updateGroup(String sessionId, UserGroup user) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": updating group " + user.getId());
        if (user.getType() == null)
            user.setType(GroupType.PRIVATE);

        try {
            return ControllerFactory.getGroupController().updateGroup(account, user);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public UserGroup deleteGroup(String sessionId, UserGroup user) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": deleting group " + user.getId());
        GroupController controller = ControllerFactory.getGroupController();
        if (user.getType() == null)
            user.setType(GroupType.PRIVATE);

        try {
            return controller.deleteGroup(account, user);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public boolean removeAccountFromGroup(String sessionId, UserGroup info, User user)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": removing \"" + user.getEmail() + "\" from group " + info.getId());
        if (info.getType() == null)
            info.setType(GroupType.PRIVATE);

        try {
            ControllerFactory.getAccountController().removeMemberFromGroup(info.getId(), user.getEmail());
            return true;
        } catch (ControllerException ce) {
            return false;
        }
    }

    @Override
    public User login(String name, String pass) {
        try {
            AccountController controller = ControllerFactory.getAccountController();
            User info = controller.authenticate(name, pass);
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
    public String createNewAccount(User info, boolean sendEmail) {
        try {
            return ControllerFactory.getAccountController().createNewAccount(info, sendEmail);
        } catch (ControllerException e) {
            Logger.error("Error creating new account", e);
            return null;
        }
    }

    @Override
    public User retrieveAccount(String email) {
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
    public User updateAccount(String sid, String email, User info) throws AuthenticationException {
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
    public User sessionValid(String sid) {
        AccountController controller = ControllerFactory.getAccountController();
        EntryController entryController = ControllerFactory.getEntryController();

        try {
            if (AccountController.isAuthenticated(sid)) {
                Account account = controller.getAccountBySessionKey(sid);
                User info = Account.toDTO(account);
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
            return folderController.retrieveFolderContents(account, folderId, sort, asc, start, limit);
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
            return ControllerFactory.getFolderController().delete(account, folderId);
        } catch (ControllerException e) {
            Logger.error(e);
        }
        return null;
    }

    @Override
    public FolderDetails retrieveUserEntries(String sid, String userId, ColumnField sort, boolean asc, int start,
            int limit) throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sid);
            EntryController entryController = ControllerFactory.getEntryController();
            FolderDetails details = new FolderDetails(0, "My Entries");
            details.setType(FolderType.SHARED);
            AccountController controller = ControllerFactory.getAccountController();
            Account user = controller.get(Long.decode(userId));
            Logger.info(account.getEmail() + ": retrieving user entries for " + user.getEmail());
            List<Entry> entries = entryController.retrieveOwnerEntries(account, user.getEmail(), sort,
                                                                       asc, start, limit);
            long count = entryController.getNumberOfOwnerEntries(account, user.getEmail());
            details.setCount(count);
            for (Entry entry : entries) {
                PartData info = ModelToInfoFactory.createTableViewData(entry, false);
                try {
                    info.setCanEdit(ControllerFactory.getPermissionController().hasWritePermission(account, entry));
                } catch (ControllerException ce) {
                    continue;
                }
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
    public ArrayList<User> retrieveAvailableAccounts(String sessionId) throws AuthenticationException {
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
    public PartData retrieveEntryDetails(String sid, long id, String url) throws AuthenticationException {
        EntryController controller = ControllerFactory.getEntryController();
        Account account = retrieveAccountForSid(sid);

        try {
            if (url != null && !url.isEmpty()) {
                Logger.info(account.getEmail() + ": retrieving entry details for " + id + " from " + url);
                IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(url);
                if (api == null)
                    return null;
                return controller.retrieveEntryDetailsFromURL(id, api);
            }

            Logger.info(account.getEmail() + ": retrieving entry details for " + id);
            return controller.retrieveEntryDetails(account, id);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    @Override
    public boolean enablePublicReadAccess(String sid, long entryId) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        try {
            return ControllerFactory.getPermissionController().enablePublicReadAccess(account, entryId);
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public boolean disablePublicReadAccess(String sid, long entryId) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        try {
            return ControllerFactory.getPermissionController().disablePublicReadAccess(account, entryId);
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public boolean enableOrDisableFolderPublicAccess(String sid, long folderId, boolean isEnable)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        try {
            PermissionsController controller = ControllerFactory.getPermissionController();
            return controller.enableOrDisableFolderPublicAccess(account, folderId, isEnable);
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public PartData retrieveEntryTipDetails(String sid, long entryId, String url) throws AuthenticationException {
        EntryController controller = ControllerFactory.getEntryController();
        Account account = retrieveAccountForSid(sid);

        try {
            if (url != null && !url.isEmpty()) {
                Logger.info(account.getEmail() + ": retrieving entry tip details for " + entryId + " from " + url);
                IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(url);
                if (api == null)
                    return null;

                return controller.retrieveEntryTipDetailsFromURL(entryId, api);
            }

            Logger.info(account.getEmail() + ": retrieving entry tip details for " + entryId);
            return controller.retrieveEntryTipDetails(account, entryId);
        } catch (ControllerException ce) {
            Logger.error(ce);
            return null;
        }
    }

    @Override
    public SearchResults performSearch(String sid, SearchQuery query, boolean isWeb) throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sid);
            SearchResults searchResults = ControllerFactory.getSearchController().runSearch(account, query, isWeb);
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
    public FolderDetails createUserCollection(String sid, String name, String description, ArrayList<Long> contents)
            throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": creating new folder with name " + name);
            return ControllerFactory.getFolderController().createNewFolder(account, name, description, contents);
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
                Folder folder = folderController.addFolderContents(account, folderId, entrys);
                FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
                long folderSize = folderController.getFolderSize(folder.getId());
                details.setCount(folderSize + entryIds.size());
                details.setDescription(folder.getDescription());
                details.setType(folder.getType());
                results.add(details);
            } catch (ControllerException ce) {
                return null;
            }
        }

        try {
            Folder sourceFolder = folderController.getFolderById(source);
            long folderSize = folderController.getFolderSize(source);
            FolderDetails sourceDetails = new FolderDetails(sourceFolder.getId(), sourceFolder.getName());
            sourceDetails.setType(sourceFolder.getType());
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

            FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
            details.setType(folder.getType());
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
                Folder folder = folderController.addFolderContents(account, folderId, entrys);
                FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
                details.setType(folder.getType());
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
    public User retrieveProfileInfo(String sid, String userId) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": retrieving profile info for " + userId);
            AccountController controller = ControllerFactory.getAccountController();
            EntryController entryController = ControllerFactory.getEntryController();
            try {
                account = controller.get(Long.decode(userId));
            } catch (NumberFormatException nfe) {
                return null;
            }

            if (account == null)
                return null;

            User user = Account.toDTO(account);
            long visibleEntryCount = entryController.getNumberOfVisibleEntries(account);
            user.setVisibleEntryCount(visibleEntryCount);
            long entryCount = entryController.getNumberOfOwnerEntries(account, account.getEmail());
            user.setUserEntryCount(entryCount);
            return user;
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
            Logger.info(account.getEmail() + ": retrieve user saved drafts");
            return ControllerFactory.getBulkUploadController().retrieveByUser(account, account);
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

            Logger.info(account.getEmail() + ": retrieving drafts pending verification");
            return ControllerFactory.getBulkUploadController().retrievePendingImports(account);
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
            return draftController.deleteDraftById(account, draftId);
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
            return controller.retrieveById(account, id, start, limit);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public BulkUploadInfo getBulkEditData(String sid, ArrayList<Long> partIds) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        BulkUploadController controller = ControllerFactory.getBulkUploadController();
        try {
            Logger.info(account.getEmail() + ": retrieving \"" + partIds.size() + "\" for bulk edit");
            return controller.getPartsForBulkEdit(account, partIds);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean submitBulkUploadDraft(String sid, long draftId, ArrayList<UserGroup> readGroups)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": submitting bulk import draft \"" + draftId);
            BulkUploadController controller = ControllerFactory.getBulkUploadController();
            try {
                return controller.submitBulkImportDraft(account, draftId, readGroups);
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

            Logger.info(account.getEmail() + ": retrieving system settings");
            return ControllerFactory.getConfigurationController().retrieveSystemSettings();
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public WebOfRegistries retrieveWebOfRegistryPartners(String sid) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            if (!ControllerFactory.getAccountController().isAdministrator(account))
                return null;

            return ControllerFactory.getWebController().getRegistryPartners();
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public ArrayList<RegistryPartner> setEnableWebOfRegistries(String sessionId, boolean value)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        if (account.getType() != AccountType.ADMIN)
            return null;

        String uri = getThreadLocalRequest().getRequestURL().substring(
                getThreadLocalRequest().getScheme().length() + 3);
        uri = uri.substring(0, uri.indexOf("/"));

        if (value)
            Logger.info(account.getEmail() + ": adding " + uri + " to web of registries");
        else
            Logger.info(account.getEmail() + ": dropping " + uri + " from web of registries");
        try {
            return ControllerFactory.getWebController().setEnable(uri, value);
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public boolean isWebOfRegistriesEnabled() {
        return ControllerFactory.getWebController().isWebEnabled();
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
    public boolean addWebPartner(String sessionId, String partnerUrl, String partnerName) {
        try {
            Account account = retrieveAccountForSid(sessionId);
            AccountController accountController = ControllerFactory.getAccountController();
            if (!accountController.isAdministrator(account))
                return false;

            Logger.info(account.getEmail() + ": adding web partner " + partnerName + "(" + partnerUrl + ")");
            ControllerFactory.getWebController().addWebPartner(partnerUrl, partnerName);
            return true;
        } catch (ControllerException | AuthenticationException e) {
            return false;
        }
    }

    @Override
    public ArrayList<User> retrieveGroupMembers(String sessionId, UserGroup user)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + " retrieving members for group " + user.getLabel());
            GroupController controller = ControllerFactory.getGroupController();
            return controller.retrieveGroupMembers(user.getUuid());
        } catch (ControllerException e) {
            return null;
        }
    }

    @Override
    public ArrayList<UserGroup> retrieveUserGroups(String sessionId, boolean includePublicGroup)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": retrieving user groups");
            return ControllerFactory.getGroupController().retrieveUserGroups(account, includePublicGroup);
        } catch (ControllerException ce) {
            return null;
        }
    }

    @Override
    public ArrayList<User> setGroupMembers(String sessionId, UserGroup user, ArrayList<User> members)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": adding " + members.size() + " members to group " + user.getId());
            GroupController groupController = ControllerFactory.getGroupController();
            return groupController.setGroupMembers(account, user, members);
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
    public Long createEntry(String sid, PartData info) throws AuthenticationException {
        try {
            Account account = this.retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": creating new entry");
            return ControllerFactory.getEntryController().createPart(account, info).getId();
        } catch (ControllerException ce) {
            return null;
        }
    }

    @Override
    public boolean deleteSample(String sessionId, PartSample part) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        Logger.info(account.getEmail() + ": deleting sample " + part.getSampleId());
        SampleController sampleController = ControllerFactory.getSampleController();
        try {
            long id = Long.decode(part.getSampleId());
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

        PartSample partSample = sampleStorage.getPartSample();
        LinkedList<StorageInfo> locations = sampleStorage.getStorageList();

        Sample sample = sampleController.createSample(partSample.getLabel(), account.getEmail(), partSample.getNotes());
        sample.setEntry(entry);

        if (locations == null || locations.isEmpty()) {
            Logger.info("Creating sample without location");

            // create sample, but not location
            try {
                sample = sampleController.saveSample(account, sample);
                sampleStorage.getPartSample().setSampleId(sample.getId() + "");
                sampleStorage.getPartSample().setDepositor(account.getEmail());
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
            Storage scheme = storageController.get(Long.parseLong(partSample.getLocationId()), false);
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

            sampleStorage.getPartSample().setSampleId(sample.getId() + "");
            sampleStorage.getPartSample().setDepositor(account.getEmail());
            return sampleStorage;
        } catch (NumberFormatException | ControllerException e) {
            Logger.error(e);
        } catch (PermissionException ce) {
            Logger.warn(ce.getMessage());
        }

        return null;
    }

    @Override
    public Long updateEntry(String sid, PartData info) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sid);
            Logger.info(account.getEmail() + ": updating entry " + info.getId());
            return ControllerFactory.getEntryController().updatePart(account, info);
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public HashMap<PartSample, ArrayList<String>> retrieveStorageSchemes(String sessionId,
            EntryType type) throws AuthenticationException {
        retrieveAccountForSid(sessionId);
        HashMap<PartSample, ArrayList<String>> schemeMap = new HashMap<>();
        StorageController storageController = ControllerFactory.getStorageController();
        List<Storage> schemes;
        try {
            schemes = storageController.getStorageSchemesForEntryType(type.getName());
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }

        for (Storage scheme : schemes) {
            PartSample partSample = new PartSample();
            partSample.setLocation(scheme.getName());
            partSample.setLocationId(String.valueOf(scheme.getId()));

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

            partSample.setLabel(scheme.getName());
            schemeMap.put(partSample, schemeOptions);
        }

        return schemeMap;
    }

    @Override
    public SuggestOracle.Response getPermissionSuggestions(String sid, Request req) throws AuthenticationException {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        List<Suggestion> suggestions = new ArrayList<>(req.getLimit());
        Account account = retrieveAccountForSid(sid);

        try {
            GroupController groupController = ControllerFactory.getGroupController();
            Set<Group> groups = groupController.getMatchingGroups(account, req.getQuery(), req.getLimit());
            for (Group group : groups) {
                AccessPermission access = new AccessPermission();
                access.setDisplay(group.getLabel());
                access.setArticle(AccessPermission.Article.GROUP);
                access.setArticleId(group.getId());
                PermissionSuggestion object = new PermissionSuggestion(access);
                suggestions.add(object);
            }

            int balance = req.getLimit() - suggestions.size();
            if (balance == 0) {
                resp.setSuggestions(suggestions);
                return resp;
            }

            AccountController controller = ControllerFactory.getAccountController();
            Set<Account> accounts = controller.getMatchingAccounts(account, req.getQuery(), balance);
            for (Account matching : accounts) {
                AccessPermission access = new AccessPermission();
                access.setDisplay(matching.getFullName());
                access.setArticle(AccessPermission.Article.ACCOUNT);
                access.setArticleId(matching.getId());
                PermissionSuggestion object = new PermissionSuggestion(access);
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
            return null;
        }
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
    public boolean addPermission(String sessionId, AccessPermission accessPermission) throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": adding permissions " + accessPermission.toString());
            PermissionsController permissionController = ControllerFactory.getPermissionController();
            permissionController.addPermission(account, accessPermission);
            return true;
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public boolean removePermission(String sessionId, AccessPermission accessPermission)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": removing permissions " + accessPermission.toString());
            PermissionsController permissionController = ControllerFactory.getPermissionController();
            permissionController.removePermission(account, accessPermission);
            return true;
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public PartData saveSequence(String sessionId, PartData part, String sequenceUser, boolean isFile)
            throws AuthenticationException {
        Account account;
        Entry entry;
        try {
            account = retrieveAccountForSid(sessionId);
            EntryController entryController = ControllerFactory.getEntryController();
            entry = entryController.get(account, part.getId());
            if (entry == null) {
                entry = EntryUtil.createEntryFromType(part.getType(), account.getFullName(), account.getEmail());
                entry.setVisibility(Visibility.DRAFT.getValue());
                entry = entryController.createEntry(account, entry, null);
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }

        Logger.info(account.getEmail() + ": saving sequence for entry " + entry.getId());
        if (isFile) {
            String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
            File file = Paths.get(tmpDir, sequenceUser).toFile();
            if (!file.exists())
                return null;

            try {
                sequenceUser = FileUtils.readFileToString(file);
            } catch (IOException e) {
                Logger.error(e);
                return null;
            }
        }

        SequenceController sequenceController = ControllerFactory.getSequenceController();
        try {
            sequenceController.parseAndSaveSequence(account, entry, sequenceUser);
            part.setId(entry.getId());
            part.setRecordId(entry.getRecordId());
            return part;
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public ArrayList<UserGroup> retrieveGroups(String sid, GroupType type) throws AuthenticationException {
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
    public ArrayList<FolderDetails> deleteEntry(String sessionId, PartData info)
            throws AuthenticationException {
        try {
            Account account = retrieveAccountForSid(sessionId);
            Logger.info(account.getEmail() + ": deleting entry " + info.getId());
            EntryController controller = ControllerFactory.getEntryController();

            Entry entry = controller.get(account, info.getId());
            if (entry == null)
                return null;

            return controller.delete(account, entry.getId());
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
            return ControllerFactory.getMessageController().sendMessage(account, info);
        } catch (ControllerException ce) {
            return false;
        }
    }

    @Override
    public Boolean rebuildSearchIndex(String sid, IndexType type) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        return ControllerFactory.getSearchController().rebuildIndexes(account, type);
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

    @Override
    public boolean requestSample(String sid, long entryID, String details) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        Logger.info(account.getEmail() + ": requesting sample for entry " + entryID + " with options " + details);
        return ControllerFactory.getEntryController().requestSample(account, entryID, details);
    }

    @Override
    public UserComment alertToEntryProblem(String sid, long entryID, String details) throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        Logger.info(account.getEmail() + ": sending alert for entry " + entryID + " with details " + details);
        return ControllerFactory.getEntryController().sendProblemNotification(account, entryID, details);
    }

    @Override
    public ArrayList<PartData> retrieveTransferredParts(String sessionId) throws AuthenticationException {
        Account account = retrieveAccountForSid(sessionId);
        try {
            return ControllerFactory.getEntryTransfers().getTransferredParts(account);
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public boolean processTransferredParts(String sid, ArrayList<Long> partIds, boolean accept)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        if (partIds == null || partIds.isEmpty())
            return false;

        try {
            return ControllerFactory.getEntryTransfers().processTransferredParts(account, partIds, accept);
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public boolean setPropagatePermissionForFolder(String sid, long folderId, boolean prop)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        try {
            return ControllerFactory.getFolderController().setPropagatePermissionForFolder(account, folderId, prop);
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    @Override
    public String exportParts(String sid, ArrayList<Long> partIds, ExportAsOption option)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        EntryController entryController = ControllerFactory.getEntryController();
        Set<String> typeSet = new HashSet<>();
        LinkedList<Entry> entries = new LinkedList<>();

        try {
            for (long id : partIds) {
                Entry entry = entryController.get(account, id);
                if (entry == null)
                    continue;

                typeSet.add(entry.getRecordType().toUpperCase());
                entries.add(entry);
            }

            String tempDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
            String fileName = UUID.randomUUID().toString();
            String data;

            // actual export
            switch (option) {
                case XML:
                    fileName = fileName + ".xml";
                    data = IceXmlSerializer.serializeToJbeiXml(account, entries);
                    break;
                case CSV:
                default:
                    fileName = fileName + ".csv";
                    data = IceXlsSerializer.serialize(entries, new TreeSet<>(typeSet));
                    break;
            }
            File file = Files.createFile(Paths.get(tempDir, fileName)).toFile();
            FileUtils.writeStringToFile(file, data);
            return fileName;
        } catch (ControllerException | IOException | UtilityException ce) {
            Logger.error(ce);
            return null;
        }
    }

    @Override
    public RegistryPartner setRegistryPartnerStatus(String sid, RegistryPartner partner)
            throws AuthenticationException {
        Account account = retrieveAccountForSid(sid);
        RemotePartnerStatus status = RemotePartnerStatus.valueOf(partner.getStatus());
        try {
            return ControllerFactory.getWebController().setPartnerStatus(account, partner.getUrl(), status);
        } catch (ControllerException e) {
            return null;
        }
    }
}

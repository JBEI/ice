package org.jbei.ice.client.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.FolderEntryDataProvider;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.profile.group.UserGroupPresenter;
import org.jbei.ice.client.profile.message.UserMessagesPresenter;
import org.jbei.ice.client.profile.preferences.UserPreferencesPresenter;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.MessageInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.folder.FolderDetails;
import org.jbei.ice.shared.dto.group.GroupInfo;
import org.jbei.ice.shared.dto.group.GroupType;
import org.jbei.ice.shared.dto.user.PreferenceKey;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Presenter for the profile page
 *
 * @author Hector Plahar
 */
public class ProfilePresenter extends AbstractPresenter {

    private final IProfileView display;
    private final FolderEntryDataProvider folderDataProvider;
    private final CollectionDataTable collectionsDataTable;
    private final String userId;
    private UserProfilePresenter profilePresenter;
    private UserGroupPresenter groupPresenter;
    private UserPreferencesPresenter preferencesPresenter;
    private UserMessagesPresenter messagesPresenter;
    private UserOption currentOption;
    private AccountInfo currentAccount;
    private final UserOption[] availableOptions;

    public ProfilePresenter(final RegistryServiceAsync service, final HandlerManager eventBus, IProfileView display,
            final String userId, String selection) {
        super(service, eventBus);
        this.userId = userId;
        this.display = display;
        profilePresenter = new UserProfilePresenter(service, eventBus, userId);
        profilePresenter.setNameChangeCallback(getCallback());

        // check if it is the owner viewing the profile
        if ((ClientController.account.getId() + "").equals(this.userId))
            availableOptions = UserOption.values();
        else
            availableOptions = new UserOption[]{UserOption.PROFILE, UserOption.ENTRIES};

        display.setMenuOptions(availableOptions);
        UserOption option = UserOption.urlToOption(selection);
        display.setMenuSelection(option);
        currentOption = option;

        this.collectionsDataTable = new CollectionDataTable(new EntryTablePager()) {

            @Override
            protected EntryViewEventHandler getHandler() {
                return new EntryViewEventHandler() {
                    @Override
                    public void onEntryView(EntryViewEvent event) {
                        event.setNavigable(folderDataProvider);
                        eventBus.fireEvent(event);
                    }
                };
            }

            @Override
            protected ArrayList<DataTableColumn<EntryInfo, ?>> createColumns() {
                ArrayList<DataTableColumn<EntryInfo, ?>> columns = new ArrayList<DataTableColumn<EntryInfo, ?>>();
                columns.add(super.addTypeColumn(true, 60, com.google.gwt.dom.client.Style.Unit.PX));
                DataTableColumn<EntryInfo, EntryInfo> partIdCol = addPartIdColumn(false, 120,
                                                                                  com.google.gwt.dom.client.Style
                                                                                          .Unit.PX);
                columns.add(partIdCol);
                columns.add(super.addNameColumn(120, com.google.gwt.dom.client.Style.Unit.PX));
                columns.add(super.addSummaryColumn());
                columns.add(super.addStatusColumn());
                super.addHasAttachmentColumn();
                super.addHasSampleColumn();
                super.addHasSequenceColumn();
                columns.add(super.addCreatedColumn());
                return columns;
            }
        };

        this.folderDataProvider = new FolderEntryDataProvider(collectionsDataTable, service);
        retrieveProfileInfo();
        handlerUserMenuSelection();
    }

    private Callback<AccountInfo> getCallback() {
        return new Callback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo accountInfo) {
                display.setAccountInfo(accountInfo);
            }

            @Override
            public void onFailure() {}
        };
    }

    private void handlerUserMenuSelection() {
        this.display.getUserSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                currentOption = display.getUserSelectionModel().getSelectedObject();
                if (currentAccount == null)
                    return;
                setViewForOption();
            }
        });
    }

    public void setViewForOption() {
        if (currentOption == null)
            return;

        if (!Arrays.asList(availableOptions).contains(currentOption)) {
            currentOption = availableOptions[0];
            display.setMenuSelection(currentOption);
        }

        switch (currentOption) {
            case PROFILE:
            default:
                profilePresenter.setAccountInfo(currentAccount);
                display.show(currentOption, profilePresenter.getView().asWidget());
                break;

            case PREFERENCES:
                if (preferencesPresenter == null)
                    preferencesPresenter = new UserPreferencesPresenter(service, eventBus);
                ArrayList<PreferenceKey> keys = new ArrayList<PreferenceKey>();
                keys.add(PreferenceKey.FUNDING_SOURCE);
                keys.add(PreferenceKey.PRINCIPAL_INVESTIGATOR);
                retrievePreferences(keys);
                break;

            case GROUPS:
                if (groupPresenter == null) {
                    groupPresenter = new UserGroupPresenter(service, eventBus);
                }
                retrieveGroups();
                break;

            case MESSAGES:
                if (messagesPresenter == null)
                    messagesPresenter = new UserMessagesPresenter(service, eventBus);
                retrieveMessages();
                break;

            case ENTRIES:
                retrieveUserEntries();
                break;
        }
    }

    private void retrieveMessages() {
        new IceAsyncCallback<ArrayList<MessageInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<MessageInfo>> callback) throws AuthenticationException {
                service.retrieveMessages(ClientController.sessionId, 0, 20, callback);
            }

            @Override
            public void onSuccess(ArrayList<MessageInfo> result) {
                if (result == null || currentOption != UserOption.MESSAGES)
                    return;

                messagesPresenter.setMessages(result);
                display.show(currentOption, messagesPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    private void retrieveProfileInfo() {
        new IceAsyncCallback<AccountInfo>() {

            @Override
            protected void callService(AsyncCallback<AccountInfo> callback) throws AuthenticationException {
                service.retrieveProfileInfo(ClientController.sessionId, userId, callback);
            }

            @Override
            public void onSuccess(AccountInfo profileInfo) {
                currentAccount = profileInfo;
                display.setAccountInfo(profileInfo);
                setViewForOption();
            }
        }.go(eventBus);
    }

    private void retrieveGroups() {
        new IceAsyncCallback<ArrayList<GroupInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<GroupInfo>> callback) throws AuthenticationException {
                service.retrieveGroups(ClientController.sessionId, GroupType.PRIVATE, callback);
            }

            @Override
            public void onSuccess(ArrayList<GroupInfo> result) {
                if (result == null || currentOption != UserOption.GROUPS)
                    return;

                groupPresenter.setGroups(result);
                display.show(currentOption, groupPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    private void retrievePreferences(final ArrayList<PreferenceKey> keys) {
        new IceAsyncCallback<HashMap<PreferenceKey, String>>() {

            @Override
            protected void callService(AsyncCallback<HashMap<PreferenceKey, String>> callback)
                    throws AuthenticationException {
                service.retrieveUserPreferences(ClientController.sessionId, keys, callback);
            }

            @Override
            public void onSuccess(HashMap<PreferenceKey, String> result) {
                HashMap<String, String> data = new HashMap<String, String>();
                for (PreferenceKey key : PreferenceKey.values()) {
                    String value = "";
                    if (result.containsKey(key))
                        value = result.get(key);

                    data.put(key.name(), value);
                }
                preferencesPresenter.setData(data);
                display.show(currentOption, preferencesPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    private void retrieveUserEntries() {
        service.retrieveUserEntries(ClientController.sessionId, this.userId, ColumnField.CREATED, false, 0,
                                    collectionsDataTable.getVisibleRange().getLength(),
                                    new AsyncCallback<FolderDetails>() {

                                        @Override
                                        public void onSuccess(FolderDetails folder) {
                                            folderDataProvider.setFolderData(folder, true);
                                            if (folder == null) {
                                                return;
                                            }

                                            collectionsDataTable.clearSelection();
                                            VerticalPanel panel = new VerticalPanel();
                                            panel.add(collectionsDataTable);
                                            panel.add(collectionsDataTable.getPager());
                                            panel.setStyleName("margin-top-20");
                                            display.show(currentOption, panel);
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            folderDataProvider.setFolderData(null, true);
                                        }
                                    });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}

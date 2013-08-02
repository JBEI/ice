package org.jbei.ice.client.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.profile.group.UserGroupPresenter;
import org.jbei.ice.client.profile.message.UserMessagesPresenter;
import org.jbei.ice.client.profile.preferences.UserPreferencesPresenter;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.message.MessageList;
import org.jbei.ice.lib.shared.dto.user.PreferenceKey;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Presenter for the profile page
 *
 * @author Hector Plahar
 */
public class ProfilePresenter extends AbstractPresenter {

    private final IProfileView display;
    //    private final CollectionDataTable collectionsDataTable;
    private final String userId;
    private UserProfilePresenter profilePresenter;
    private UserGroupPresenter groupPresenter;
    private UserPreferencesPresenter preferencesPresenter;
    private UserMessagesPresenter messagesPresenter;
    private UserOption currentOption;
    private User currentAccount;
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
            availableOptions = new UserOption[]{UserOption.PROFILE};

        display.setMenuOptions(availableOptions);
        UserOption option = UserOption.urlToOption(selection);
        display.setMenuSelection(option);
        currentOption = option;

//        this.collectionsDataTable = new CollectionDataTable(new EntryTablePager(), null);

//        {
//
//            @Override
//            protected ArrayList<DataTableColumn<PartData, ?>> createColumns() {
//                ArrayList<DataTableColumn<PartData, ?>> columns = new ArrayList<DataTableColumn<PartData, ?>>();
//                columns.add(super.addTypeColumn(true, 60, com.google.gwt.dom.client.Style.Unit.PX));
//                DataTableColumn<PartData, PartData> partIdCol = addPartIdColumn(false, 120,
//                        com.google.gwt.dom.client.Style.Unit.PX);
//                columns.add(partIdCol);
//                columns.add(super.addNameColumn(120, com.google.gwt.dom.client.Style.Unit.PX));
//                columns.add(super.addSummaryColumn());
//                columns.add(super.addStatusColumn());
//                super.addHasAttachmentColumn();
//                super.addHasSampleColumn();
//                super.addHasSequenceColumn();
//                columns.add(super.addCreatedColumn());
//                return columns;
//            }
//        };

//        this.folderDataProvider = new FolderEntryDataProvider(collectionsDataTable, service);
        retrieveProfileInfo();
        handlerUserMenuSelection();
    }

    private Callback<User> getCallback() {
        return new Callback<User>() {

            @Override
            public void onSuccess(User user) {
                display.setAccountInfo(user);
            }

            @Override
            public void onFailure() {
            }
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
                profilePresenter.setUser(currentAccount);
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

//            case ENTRIES:
//                retrieveUserEntries();
//                break;
        }
    }

    private void retrieveMessages() {
        new IceAsyncCallback<MessageList>() {

            @Override
            protected void callService(AsyncCallback<MessageList> callback) throws AuthenticationException {
                service.retrieveMessages(ClientController.sessionId, 0, 50, callback);
            }

            @Override
            public void onSuccess(MessageList result) {
                if (result == null || currentOption != UserOption.MESSAGES)
                    return;

                messagesPresenter.setMessages(result);
                display.show(currentOption, messagesPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    private void retrieveProfileInfo() {
        new IceAsyncCallback<User>() {

            @Override
            protected void callService(AsyncCallback<User> callback) throws AuthenticationException {
                service.retrieveProfileInfo(ClientController.sessionId, userId, callback);
            }

            @Override
            public void onSuccess(User profileInfo) {
                currentAccount = profileInfo;
                display.setAccountInfo(profileInfo);
                setViewForOption();
            }
        }.go(eventBus);
    }

    private void retrieveGroups() {
        new IceAsyncCallback<ArrayList<UserGroup>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<UserGroup>> callback) throws AuthenticationException {
                service.retrieveGroups(ClientController.sessionId, GroupType.PRIVATE, callback);
            }

            @Override
            public void onSuccess(ArrayList<UserGroup> result) {
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

        // retrieve search preferences
        new IceAsyncCallback<HashMap<String, String>>() {

            @Override
            protected void callService(AsyncCallback<HashMap<String, String>> callback)
                    throws AuthenticationException {
                service.retrieveUserSearchPreferences(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(HashMap<String, String> result) {
                preferencesPresenter.setSearchPreferences(result);
            }
        }.go(eventBus);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}

package org.jbei.ice.client.profile;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.SamplesDataProvider;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.login.RegistrationDetails;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.AccountInfo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Presenter for the profile page
 *
 * @author Hector Plahar
 */
public class ProfilePresenter extends AbstractPresenter {

    private final String sid = AppController.sessionId;
    private final SamplesDataProvider samplesDataProvider;

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IProfileView display;

    private AccountInfo currentInfo;
    private EntryDataViewDataProvider entryDataProvider;
    private final CollectionDataTable collectionsDataTable;
    private final String userId;

    public ProfilePresenter(final RegistryServiceAsync service, final HandlerManager eventBus,
                            final IProfileView display, final String userId) {

        this.userId = userId;
        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        this.display.getMenu().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                CellEntry selected = display.getMenu().getSelection();
                switch (selected.getType()) {
                    default:
                    case ABOUT:
                        display.setContents(currentInfo);
                        break;

                    case ENTRIES:
                        retrieveUserEntries();
                        break;

                    case SAMPLES:
                        retrieveUserSamples();
                        break;
                }
            }
        });

        new IceAsyncCallback<AccountInfo>() {

            @Override
            protected void callService(AsyncCallback<AccountInfo> callback) {
                try {
                    service.retrieveProfileInfo(AppController.sessionId, userId, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(AccountInfo profileInfo) {
                currentInfo = profileInfo;
                display.setContents(currentInfo);

                // set menu
                ArrayList<CellEntry> menu = new ArrayList<CellEntry>();
                CellEntry about = new CellEntry(MenuType.ABOUT, -1);
                menu.add(about);
                menu.add(new CellEntry(MenuType.ENTRIES, currentInfo.getUserEntryCount()));
                menu.add(new CellEntry(MenuType.SAMPLES, currentInfo.getUserSampleCount()));
                display.getMenu().setRowData(menu);
            }
        }.go(eventBus);


        samplesDataProvider = new SamplesDataProvider(display.getSamplesTable(), service);

        this.collectionsDataTable = new CollectionDataTable(new EntryTablePager()) {

            @Override
            protected EntryViewEventHandler getHandler() {
                return new EntryViewEventHandler() {
                    @Override
                    public void onEntryView(EntryViewEvent event) {
                        event.setNavigable(entryDataProvider);
                        eventBus.fireEvent(event);
                    }
                };
            }
        };
        this.entryDataProvider = new EntryDataViewDataProvider(collectionsDataTable, service);

        // check
        checkCanEditProfile();
        checkCanChangePassword();
    }

    private void retrieveUserEntries() {
        service.retrieveUserEntries(AppController.sessionId, this.userId,
                                    new AsyncCallback<FolderDetails>() {

                                        @Override
                                        public void onSuccess(FolderDetails folder) {
                                            if (folder == null) {
                                                entryDataProvider.setValues(null);
                                                return;
                                            }

                                            collectionsDataTable.clearSelection();
                                            ArrayList<Long> entries = folder.getContents();
                                            entryDataProvider.setValues(entries);
                                            display.setEntryContent(collectionsDataTable);
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                        }
                                    });
    }

    private void retrieveUserSamples() {
        service.retrieveSamplesByDepositor(AppController.sessionId, currentInfo.getEmail(),
                                           ColumnField.CREATED, false, new AsyncCallback<LinkedList<Long>>() {

            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(LinkedList<Long> result) {
                samplesDataProvider.setValues(new ArrayList<Long>(result));
                display.setSampleView();
            }
        });
    }

    private void checkCanEditProfile() {
        service.getSetting("PROFILE_EDIT_ALLOWED", new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if ("yes".equalsIgnoreCase(result) || "true".equalsIgnoreCase(result)) {
                    // must be admin or current logged in user
                    if (AppController.accountInfo.isModerator()
                            || AppController.accountInfo.getEmail().equals(userId))
                        display.addEditProfileLinkHandler(new EditProfileHandler());
                }
            }

            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    private void checkCanChangePassword() {
        service.getSetting("PASSWORD_CHANGE_ALLOWED", new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if ("yes".equalsIgnoreCase(result) || "true".equalsIgnoreCase(result)) {
                    // must be currently logged in user to change password
                    if (AppController.accountInfo.getEmail().equals(userId))
                        display.addChangePasswordLinkHandler(new ChangePasswordHandler());
                }
            }

            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    private class EditProfileHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            display.editProfile(currentInfo, new SaveProfileHandler(), new ShowCurrentInfoHandler());
        }
    }

    private class SaveProfileHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            RegistrationDetails details = display.getUpdatedDetails();
            if (details == null)
                return;

            if (!details.getEmail().equals(currentInfo.getEmail())) {
                return;
            }

            currentInfo.setDescription(details.getAbout());
            currentInfo.setFirstName(details.getFirstName());
            currentInfo.setLastName(details.getLastName());
            currentInfo.setInitials(details.getInitials());
            currentInfo.setInstitution(details.getInstitution());

            try {
                service.updateAccount(AppController.sessionId, currentInfo.getEmail(), currentInfo,
                                      new AsyncCallback<AccountInfo>() {

                                          @Override
                                          public void onSuccess(AccountInfo result) {
                                              currentInfo = result;
                                              display.setContents(currentInfo);
                                          }

                                          @Override
                                          public void onFailure(Throwable caught) {
                                              Window.alert("Could not connect to the server for update");
                                          }
                                      });
            } catch (org.jbei.ice.client.exception.AuthenticationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private class ShowCurrentInfoHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            display.setContents(currentInfo);
        }
    }

    private class ChangePasswordHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            display.changePasswordPanel(currentInfo, new UpdatePasswordClickHandler(),
                                        new ShowCurrentInfoHandler());
        }
    }

    private class UpdatePasswordClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            String password = display.getUpdatedPassword();
            if (password.isEmpty())
                return;

            try {
                service.updateAccountPassword(AppController.sessionId, currentInfo.getEmail(),
                                              password, new AsyncCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean result) {
                        if (result)
                            display.setContents(currentInfo);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                    }
                });
            } catch (org.jbei.ice.client.exception.AuthenticationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}

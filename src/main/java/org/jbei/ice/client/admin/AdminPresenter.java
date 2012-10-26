package org.jbei.ice.client.admin;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.group.GroupPresenter;
import org.jbei.ice.client.admin.usermanagement.UserPresenter;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Presenter for the admin page
 *
 * @author Hector Plahar
 */
public class AdminPresenter extends AbstractPresenter {

    private final AdminView view;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    private UserPresenter userPresenter;
    private GroupPresenter groupPresenter;

    public AdminPresenter(RegistryServiceAsync service, HandlerManager eventBus, AdminView view) {
        this.service = service;
        this.view = view;
        this.eventBus = eventBus;

        // presenters
        userPresenter = new UserPresenter();
        groupPresenter = new GroupPresenter();

        setSelectionHandler();
    }

    protected void setSelectionHandler() {
        this.view.getUserSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                setViewForOption(view.getUserSelectionModel().getSelectedObject());
            }
        });
    }

    private void setViewForOption(AdminOption option) {
        if (option == null)
            return;

        switch (option) {
            case SETTINGS:
//                retrieveProfileInfo();
                break;

            case GROUPS:
                break;

            case USERS:
//                retrieveUserEntries();
                break;
        }
    }

//    private void retrieveProfileInfo() {
//        new IceAsyncCallback<AccountInfo>() {
//
//            @Override
//            protected void callService(AsyncCallback<AccountInfo> callback) throws AuthenticationException {
//                service.retrieveProfileInfo(AppController.sessionId, userId, callback);
//            }
//
//            @Override
//            public void onSuccess(AccountInfo profileInfo) {
//                currentInfo = profileInfo;
//                display.setContents(currentInfo);
//            }
//        }.go(eventBus);
//    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }
}

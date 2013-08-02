package org.jbei.ice.client.admin.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupPresenter extends AdminPanelPresenter {

    private final AdminGroupPanel view;
    private UserGroup currentUserGroupSelection;

    public GroupPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        this.view = new AdminGroupPanel();

        // handlers
        addGroupSelectionHandler();
        retrieveAvailableAccountsToUser();
        addGroupSaveHandler();
        addCreateGroupHandler();
        addGroupMemberDeleteDelegate();
        addGroupDeleteDelegate();
        addGroupUpdateDelegate();
        addVerifyMemberDelegate();
    }

    public void setGroups(ArrayList<UserGroup> result) {
        view.displayGroups(result);
    }

    public void addGroupDeleteDelegate() {
        view.setDeleteGroupDelegate(new ServiceDelegate<UserGroup>() {
            @Override
            public void execute(final UserGroup userGroup) {
                new IceAsyncCallback<UserGroup>() {

                    @Override
                    protected void callService(AsyncCallback<UserGroup> callback) throws AuthenticationException {
                        service.deleteGroup(ClientController.sessionId, userGroup, callback);
                    }

                    @Override
                    public void onSuccess(UserGroup result) {
                        view.removeGroup(result);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addGroupUpdateDelegate() {
        view.setUpdateGroupDelegate(new ServiceDelegate<UserGroup>() {
            @Override
            public void execute(final UserGroup userGroup) {
                new IceAsyncCallback<UserGroup>() {

                    @Override
                    protected void callService(AsyncCallback<UserGroup> callback) throws AuthenticationException {
                        service.updateGroup(ClientController.sessionId, userGroup, callback);
                    }

                    @Override
                    public void onSuccess(UserGroup result) {
                    }
                }.go(eventBus);
            }
        });
    }

    private void addVerifyMemberDelegate() {
        view.setVerifyRegisteredUserDelegate(new ServiceDelegate<String>() {
            @Override
            public void execute(final String email) {
                new IceAsyncCallback<User>() {

                    @Override
                    protected void callService(AsyncCallback<User> callback) throws AuthenticationException {
                        service.retrieveAccount(email, callback);
                    }

                    @Override
                    public void onSuccess(User result) {
                        view.addVerifiedAccount(result);
                    }

                    @Override
                    public void onNullResult() {
                        view.addVerifiedAccount(null);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addCreateGroupHandler() {
        view.setNewGroupHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final UserGroup user = view.getNewGroup();
                if (user == null)
                    return;

                // save new group
                new IceAsyncCallback<UserGroup>() {

                    @Override
                    protected void callService(AsyncCallback<UserGroup> callback) throws AuthenticationException {
                        service.createNewGroup(ClientController.sessionId, user, callback);
                    }

                    @Override
                    public void onSuccess(UserGroup result) {
                        view.addGroupDisplay(result);
                        view.setCreateGroupVisibility(false);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addGroupMemberDeleteDelegate() {
        view.setDeleteMemberDelegate(new ServiceDelegate<User>() {
            @Override
            public void execute(final User info) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.removeAccountFromGroup(ClientController.sessionId, currentUserGroupSelection, info,
                                                       callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (result.booleanValue()) {
                            currentUserGroupSelection.setMemberCount(currentUserGroupSelection.getMemberCount() - 1);
                            view.removeGroupMember(currentUserGroupSelection, info);
                        }
                        // else show error msg
                    }
                }.go(eventBus);
            }
        });
    }

    private void addGroupSaveHandler() {
        view.setGroupMemberSaveHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final ArrayList<User> selectedMembers = view.getSelectedMembers();
                GWT.log(selectedMembers.size() + " members selected for group " + currentUserGroupSelection.getLabel());
                new IceAsyncCallback<ArrayList<User>>() {

                    @Override
                    protected void callService(AsyncCallback<ArrayList<User>> callback)
                            throws AuthenticationException {
                        service.setGroupMembers(ClientController.sessionId, currentUserGroupSelection, selectedMembers,
                                                callback);
                    }

                    @Override
                    public void onSuccess(ArrayList<User> result) {
                        Collections.sort(result, new Comparator<User>() {
                            @Override
                            public int compare(User o1, User o2) {
                                return o1.getFullName().compareTo(o2.getFullName());
                            }
                        });
                        currentUserGroupSelection.setMembers(result);
                        view.setGroupMembers(currentUserGroupSelection, result);
                    }
                }.go(eventBus);
            }
        });
    }


    protected void retrieveAvailableAccountsToUser() {
        new IceAsyncCallback<ArrayList<User>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<User>> callback) throws AuthenticationException {
                service.retrieveAvailableAccounts(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<User> result) {
                view.setAvailableAccounts(result);
            }
        }.go(eventBus);
    }

    private void addGroupSelectionHandler() {
        this.view.setGroupSelectionHandler(new ServiceDelegate<UserGroup>() {

            @Override
            public void execute(UserGroup userGroup) {
                if (userGroup == null)
                    return;

                currentUserGroupSelection = userGroup;
                retrieveGroupMembers(userGroup);
            }
        });
    }

    @Override
    public IAdminPanel getView() {
        return this.view;
    }

    protected void retrieveGroupMembers(final UserGroup user) {
        new IceAsyncCallback<ArrayList<User>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<User>> callback) throws AuthenticationException {
                service.retrieveGroupMembers(ClientController.sessionId, user, callback);
            }

            @Override
            public void onSuccess(ArrayList<User> result) {
                Collections.sort(result, new Comparator<User>() {
                    @Override
                    public int compare(User o1, User o2) {
                        return o1.getFullName().compareTo(o2.getFullName());
                    }
                });

                if (!currentUserGroupSelection.getUuid().equalsIgnoreCase(user.getUuid()))
                    return;

                currentUserGroupSelection.setMembers(result);
                view.setGroupMembers(currentUserGroupSelection, result);
            }
        }.go(eventBus);
    }
}

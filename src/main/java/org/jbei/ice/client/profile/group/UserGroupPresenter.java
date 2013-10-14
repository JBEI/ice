package org.jbei.ice.client.profile.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.profile.PanelPresenter;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class UserGroupPresenter extends PanelPresenter {

    private final UserGroupPanel groupPanel;
    private UserGroup currentUserGroup;

    public UserGroupPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        groupPanel = new UserGroupPanel();

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

    @Override
    public IUserProfilePanel getView() {
        return groupPanel;
    }

    public void setGroups(ArrayList<UserGroup> result) {
        groupPanel.displayGroups(result);
    }

    public void setMemberGroups(ArrayList<UserGroup> result) {
        groupPanel.displayMemberGroups(result);
    }

    public void addGroupDeleteDelegate() {
        groupPanel.setDeleteGroupDelegate(new ServiceDelegate<UserGroup>() {

            @Override
            public void execute(final UserGroup userGroup) {
                new IceAsyncCallback<UserGroup>() {

                    @Override
                    protected void callService(AsyncCallback<UserGroup> callback) throws AuthenticationException {
                        service.deleteGroup(ClientController.sessionId, userGroup, callback);
                    }

                    @Override
                    public void onSuccess(UserGroup result) {
                        groupPanel.removeGroup(result);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addGroupMemberDeleteDelegate() {
        groupPanel.setDeleteMemberDelegate(new ServiceDelegate<User>() {

            @Override
            public void execute(final User info) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.removeAccountFromGroup(ClientController.sessionId, currentUserGroup, info, callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (result.booleanValue()) {
                            currentUserGroup.setMemberCount(currentUserGroup.getMemberCount() - 1);
                            groupPanel.removeGroupMember(currentUserGroup, info);
                        }
                        // else show error msg
                    }
                }.go(eventBus);
            }
        });
    }

    private void addVerifyMemberDelegate() {
        groupPanel.setVerifyRegisteredUserDelegate(new ServiceDelegate<String>() {

            @Override
            public void execute(final String email) {
                new IceAsyncCallback<User>() {

                    @Override
                    protected void callService(AsyncCallback<User> callback) throws AuthenticationException {
                        service.retrieveAccount(email, callback);
                    }

                    @Override
                    public void onSuccess(User result) {
                        groupPanel.addVerifiedAccount(result);
                    }

                    @Override
                    public void onNullResult() {
                        groupPanel.addVerifiedAccount(null);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addGroupUpdateDelegate() {
        groupPanel.setUpdateGroupDelegate(new ServiceDelegate<UserGroup>() {

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

    private void addGroupSaveHandler() {
        groupPanel.setGroupMemberSaveHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                final ArrayList<User> selectedMembers = groupPanel.getSelectedMembers();
                if (selectedMembers.isEmpty())
                    return;

                GWT.log(selectedMembers.size() + " members selected for group " + currentUserGroup.getLabel());
                new IceAsyncCallback<ArrayList<User>>() {

                    @Override
                    protected void callService(AsyncCallback<ArrayList<User>> callback)
                            throws AuthenticationException {
                        service.setGroupMembers(ClientController.sessionId, currentUserGroup, selectedMembers,
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
                        currentUserGroup.setMembers(result);
                        groupPanel.setGroupMembers(currentUserGroup, result);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addCreateGroupHandler() {
        groupPanel.setNewGroupHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                final UserGroup user = groupPanel.getNewGroup();
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
                        groupPanel.addGroupDisplay(result);
                        groupPanel.setCreateGroupVisibility(false);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addGroupSelectionHandler() {
        this.groupPanel.setGroupSelectionHandler(new ServiceDelegate<UserGroup>() {

            @Override
            public void execute(UserGroup userGroup) {
                if (userGroup == null)
                    return;

                currentUserGroup = userGroup;
                retrieveGroupMembers(userGroup);
            }
        });
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

                if (!currentUserGroup.getUuid().equalsIgnoreCase(user.getUuid()))
                    return;

                currentUserGroup.setMembers(result);
                groupPanel.setGroupMembers(currentUserGroup, result);
            }
        }.go(eventBus);
    }

    protected void retrieveAvailableAccountsToUser() {
        new IceAsyncCallback<ArrayList<User>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<User>> callback) throws AuthenticationException {
                service.retrieveAvailableAccounts(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<User> result) {
                groupPanel.setAvailableAccounts(result);
            }
        }.go(eventBus);
    }
}

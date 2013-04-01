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
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.group.GroupInfo;

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
    private GroupInfo currentGroup;

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

    public void setGroups(ArrayList<GroupInfo> result) {
        groupPanel.displayGroups(result);
    }

    public void addGroupDeleteDelegate() {
        groupPanel.setDeleteGroupDelegate(new ServiceDelegate<GroupInfo>() {

            @Override
            public void execute(final GroupInfo groupInfo) {
                new IceAsyncCallback<GroupInfo>() {

                    @Override
                    protected void callService(AsyncCallback<GroupInfo> callback) throws AuthenticationException {
                        service.deleteGroup(ClientController.sessionId, groupInfo, callback);
                    }

                    @Override
                    public void onSuccess(GroupInfo result) {
                        groupPanel.removeGroup(result);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addGroupMemberDeleteDelegate() {
        groupPanel.setDeleteMemberDelegate(new ServiceDelegate<AccountInfo>() {

            @Override
            public void execute(final AccountInfo info) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.removeAccountFromGroup(ClientController.sessionId, currentGroup, info, callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (result.booleanValue()) {
                            currentGroup.setMemberCount(currentGroup.getMemberCount() - 1);
                            groupPanel.removeGroupMember(currentGroup, info);
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
                new IceAsyncCallback<AccountInfo>() {

                    @Override
                    protected void callService(AsyncCallback<AccountInfo> callback) throws AuthenticationException {
                        service.retrieveAccount(email, callback);
                    }

                    @Override
                    public void onSuccess(AccountInfo result) {
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
        groupPanel.setUpdateGroupDelegate(new ServiceDelegate<GroupInfo>() {

            @Override
            public void execute(final GroupInfo groupInfo) {
                new IceAsyncCallback<GroupInfo>() {

                    @Override
                    protected void callService(AsyncCallback<GroupInfo> callback) throws AuthenticationException {
                        service.updateGroup(ClientController.sessionId, groupInfo, callback);
                    }

                    @Override
                    public void onSuccess(GroupInfo result) {}
                }.go(eventBus);
            }
        });
    }

    private void addGroupSaveHandler() {
        groupPanel.setGroupMemberSaveHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                final ArrayList<AccountInfo> selectedMembers = groupPanel.getSelectedMembers();
                if (selectedMembers.isEmpty())
                    return;

                GWT.log(selectedMembers.size() + " members selected for group " + currentGroup.getLabel());
                new IceAsyncCallback<ArrayList<AccountInfo>>() {

                    @Override
                    protected void callService(AsyncCallback<ArrayList<AccountInfo>> callback)
                            throws AuthenticationException {
                        service.setGroupMembers(ClientController.sessionId, currentGroup, selectedMembers, callback);
                    }

                    @Override
                    public void onSuccess(ArrayList<AccountInfo> result) {
                        Collections.sort(result, new Comparator<AccountInfo>() {

                            @Override
                            public int compare(AccountInfo o1, AccountInfo o2) {
                                return o1.getFullName().compareTo(o2.getFullName());
                            }
                        });
                        currentGroup.setMembers(result);
                        groupPanel.setGroupMembers(currentGroup, result);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addCreateGroupHandler() {
        groupPanel.setNewGroupHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                final GroupInfo info = groupPanel.getNewGroup();
                if (info == null)
                    return;

                // save new group
                new IceAsyncCallback<GroupInfo>() {

                    @Override
                    protected void callService(AsyncCallback<GroupInfo> callback) throws AuthenticationException {
                        service.createNewGroup(ClientController.sessionId, info, callback);
                    }

                    @Override
                    public void onSuccess(GroupInfo result) {
                        groupPanel.addGroupDisplay(result);
                        groupPanel.setCreateGroupVisibility(false);
                    }
                }.go(eventBus);
            }
        });
    }

    private void addGroupSelectionHandler() {
        this.groupPanel.setGroupSelectionHandler(new ServiceDelegate<GroupInfo>() {

            @Override
            public void execute(GroupInfo groupInfo) {
                if (groupInfo == null)
                    return;

                currentGroup = groupInfo;
                retrieveGroupMembers(groupInfo);
            }
        });
    }

    protected void retrieveGroupMembers(final GroupInfo info) {
        new IceAsyncCallback<ArrayList<AccountInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<AccountInfo>> callback) throws AuthenticationException {
                service.retrieveGroupMembers(ClientController.sessionId, info, callback);
            }

            @Override
            public void onSuccess(ArrayList<AccountInfo> result) {
                Collections.sort(result, new Comparator<AccountInfo>() {
                    @Override
                    public int compare(AccountInfo o1, AccountInfo o2) {
                        return o1.getFullName().compareTo(o2.getFullName());
                    }
                });

                if (!currentGroup.getUuid().equalsIgnoreCase(info.getUuid()))
                    return;

                currentGroup.setMembers(result);
                groupPanel.setGroupMembers(currentGroup, result);
            }
        }.go(eventBus);
    }

    protected void retrieveAvailableAccountsToUser() {
        new IceAsyncCallback<ArrayList<AccountInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<AccountInfo>> callback) throws AuthenticationException {
                service.retrieveAvailableAccounts(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<AccountInfo> result) {
                groupPanel.setAvailableAccounts(result);
            }
        }.go(eventBus);
    }
}

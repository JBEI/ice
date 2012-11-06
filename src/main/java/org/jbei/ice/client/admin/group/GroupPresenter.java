package org.jbei.ice.client.admin.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupPresenter extends AdminPanelPresenter {

    private final GroupsPanel view;
    private String currentGroupSelection;
    private CreateGroupWidget widget;

    public GroupPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        this.view = new GroupsPanel(createDelegate());

        // handlers
        addGroupSelectionHandler();
        addCreateGroupHandler();

        widget = new CreateGroupWidget();
    }

    private DeleteActionCell.Delegate<AccountInfo> createDelegate() {
        return new DeleteActionCell.Delegate<AccountInfo>() {
            @Override
            public void execute(AccountInfo object, Callback callback) { // TODO : pass another delete that indicates
                // success
                // TODO : remove object from group
                if (object == null)
                    return;
            }
        };
    }

    private void addGroupSelectionHandler() {
        this.view.setGroupSelectionHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GroupInfo info = view.getGroupSelection(event);
                if (info == null)
                    return;

                currentGroupSelection = info.getUuid();
                retrieveGroupMembers(info);
            }
        });
    }

    private void addCreateGroupHandler() {
        this.view.setCreateGroupHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                widget.showPopup(true);
            }
        });
    }

    @Override
    public AdminPanel getView() {
        return this.view;
    }

    public void setGroups(ArrayList<GroupInfo> groups) {
        this.view.setGroups(groups);
    }

    protected void retrieveGroupMembers(final GroupInfo info) {
        new IceAsyncCallback<ArrayList<AccountInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<AccountInfo>> callback) throws AuthenticationException {
                service.retrieveGroupMembers(AppController.sessionId, info, callback);
            }

            @Override
            public void onSuccess(ArrayList<AccountInfo> result) {
                Collections.sort(result, new Comparator<AccountInfo>() {
                    @Override
                    public int compare(AccountInfo o1, AccountInfo o2) {
                        return o1.getFullName().compareTo(o2.getFullName());
                    }
                });
                if (!currentGroupSelection.equalsIgnoreCase(info.getUuid()))
                    return;

                view.setGroupMembers(result);
            }
        }.go(eventBus);
    }
}

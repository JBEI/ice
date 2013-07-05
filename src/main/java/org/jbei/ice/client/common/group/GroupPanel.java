package org.jbei.ice.client.common.group;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.profile.group.GroupsListWidget;
import org.jbei.ice.lib.shared.dto.AccountInfo;
import org.jbei.ice.lib.shared.dto.group.GroupInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Parent panel class for displaying groups and managing them
 *
 * @author Hector Plahar
 */
public abstract class GroupPanel extends Composite {

    protected GroupsListWidget groupsWidget;
    private VerticalPanel vPanel;
    private Button createGroup;

    public GroupPanel() {
        ScrollPanel scrollPanel = new ScrollPanel();
        initWidget(scrollPanel);
        initComponents();

        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(createGroup);
        vPanel.add(groupsWidget);

        scrollPanel.add(vPanel);
        addCreateGroupHandler();
    }

    protected void initComponents() {
        groupsWidget = new GroupsListWidget();
        vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        createGroup = new Button("<i class=\"blue " + FAIconType.GROUP.getStyleName() + "\"></i>"
                                         + "<i style=\"vertical-align: sub; font-size: 7px;\" class=\""
                                         + FAIconType.PLUS.getStyleName() + "\"></i>&nbsp; Create Group");
    }

    private void addCreateGroupHandler() {
        createGroup.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                groupsWidget.setCreateGroupVisibility(true);
            }
        });
    }

    public void setCreateGroupVisibility(boolean visible) {
        groupsWidget.setCreateGroupVisibility(visible);
    }

    public void setNewGroupHandler(ClickHandler handler) {
        groupsWidget.setCreateNewHandler(handler);
    }

    public void setDeleteMemberDelegate(ServiceDelegate<AccountInfo> deleteDelegate) {
        groupsWidget.setDeleteGroupMemberDelegate(deleteDelegate);
    }

    public void setDeleteGroupDelegate(ServiceDelegate<GroupInfo> deleteGroupDelegate) {
        groupsWidget.setDeleteGroupDelegate(deleteGroupDelegate);
    }

    public void setUpdateGroupDelegate(ServiceDelegate<GroupInfo> updateGroupDelegate) {
        groupsWidget.setEditGroupDelegate(updateGroupDelegate);
    }

    public void setGroupMemberSaveHandler(ClickHandler handler) {
        groupsWidget.setSaveHandler(handler);
    }

    public ArrayList<AccountInfo> getSelectedMembers() {
        return groupsWidget.getSelectedMembers();
    }

    public void displayGroups(ArrayList<GroupInfo> list) {
        groupsWidget.setGroupList(list);
    }

    public void addGroupDisplay(GroupInfo info) {
        groupsWidget.addGroup(info);
    }

    public void removeGroup(GroupInfo info) {
        groupsWidget.removeGroup(info);
    }

    public void setGroupSelectionHandler(ServiceDelegate<GroupInfo> handler) {
        groupsWidget.setSelectionHandler(handler);
    }

    public void setGroupMembers(GroupInfo group, ArrayList<AccountInfo> list) {
        groupsWidget.setGroupMembers(group, list);
    }

    public void setAvailableAccounts(ArrayList<AccountInfo> result) {
        groupsWidget.setAvailableAccounts(result);
    }

    public abstract GroupInfo getNewGroup();

    public void setVerifyRegisteredUserDelegate(ServiceDelegate<String> serviceDelegate) {
        groupsWidget.setVerifyUserEmailDelegate(serviceDelegate);
    }

    // if info is null, then verification was invalid
    public void addVerifiedAccount(AccountInfo info) {
        groupsWidget.addVerifiedAccount(info);
    }

    public void removeGroupMember(GroupInfo group, AccountInfo info) {
        groupsWidget.removeGroupMember(group, info);
    }
}

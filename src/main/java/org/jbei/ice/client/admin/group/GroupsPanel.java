package org.jbei.ice.client.admin.group;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.group.GroupInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Panel for managing groups. Also acts as the view
 *
 * @author Hector Plahar
 */
public class GroupsPanel extends Composite implements AdminPanel {

    private VerticalPanel vPanel;
    private Button createGroup;
    private GroupsWidget widget;
    private CreateGroupWidget createGroupWidget;
    private GroupInfo rootGroup;

    public GroupsPanel(ServiceDelegate<AccountInfo> deleteDelegate) {
        ScrollPanel scrollPanel = new ScrollPanel();
        initWidget(scrollPanel);
        initComponents(deleteDelegate);

        vPanel.add(createGroup);
        scrollPanel.add(vPanel);
        vPanel.setStyleName("margin-top-20");

        setCreateGroupHandler();
    }

    protected void initComponents(ServiceDelegate<AccountInfo> deleteDelegate) {
        widget = new GroupsWidget(deleteDelegate);
        vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        createGroupWidget = new CreateGroupWidget(null);
        createGroup = new Button("<i class=\"" + FAIconType.GROUP.getStyleName()
                                         + "\"></i><i style=\"vertical-align: sub; font-size: 7px\" class=\""
                                         + FAIconType.PLUS.getStyleName() + "\"></i>&nbsp; Create Group");
    }

    private void setCreateGroupHandler() {
        createGroup.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                createGroupWidget.showPopup(true);
            }
        });
    }

    public void addCreateGroupHandler(ClickHandler handler) {
        createGroupWidget.addCreateGroupHandler(handler);
    }

    public void setGroupCreationMembers(ArrayList<AccountInfo> list) {
        createGroupWidget.setGroupCreationMembers(list);
    }

    public void addNewGroup(GroupInfo info) {
        widget.addNewGroup(info);
    }

    public void setGroupMembers(ArrayList<AccountInfo> list) {
        widget.setGroupMembers(list);
    }

    public void setRootGroup(GroupInfo group) {
        widget.setRootGroup(group);
        widget.showDisplay();
        vPanel.add(widget);
        rootGroup = group;
    }

    public void setGroupSelectionHandler(ClickHandler handler) {
        widget.setClickHandler(handler);
    }

    public GroupInfo getGroupSelection(ClickEvent event) {
        return widget.getGroupSelection(event);
    }

    public void setRetrieveGroupMemberDelegate(ServiceDelegate<GroupInfo> serviceDelegate) {
        createGroupWidget.setGroupMemberDelegate(serviceDelegate);
    }

    public GroupInfo getNewGroup() {
        String name = createGroupWidget.getGroupName();
        String description = createGroupWidget.getGroupDescription();
        GroupInfo newGroup = new GroupInfo();
        newGroup.setLabel(name);
        newGroup.setDescription(description);
        return newGroup;
    }
}

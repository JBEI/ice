package org.jbei.ice.client.profile.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.group.GroupMembersWidget;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Widget that enables/facilitates adding users to a group
 *
 * @author Hector Plahar
 */
public class GroupAddMembersWidget extends Composite {

    private ListBox listBox;
    private GroupMembersWidget selectedGroupMembersWidget;
    private Icon icon;
    private HashMap<Long, AccountInfo> available;
    private FlexTable layout;
    private Button save;
    private ArrayList<AccountInfo> listList;
    private TextBox registeredUserBox;
    private Button addUser;
    private HTML cancel;

    public GroupAddMembersWidget() {
        initComponents();
        initWidget(layout);

        FlexTable table = new FlexTable();
        table.setCellPadding(5);
        table.setCellSpacing(0);

        table.setHTML(0, 0, "<b class=\"font-75em\">SELECT USERS FROM THE LIST OR ADD USERS BY EMAIL</b>");
        table.getFlexCellFormatter().setColSpan(0, 0, 3);

        table.setWidget(1, 0, listBox);
        table.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);

        table.setWidget(1, 1, icon);
        table.getFlexCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_MIDDLE);
        table.getFlexCellFormatter().setStyleName(1, 1, "pad-8");

        table.setWidget(1, 2, createMembersPanel());
        table.getFlexCellFormatter().setVerticalAlignment(1, 2, HasVerticalAlignment.ALIGN_TOP);

        table.setWidget(3, 0, createSaveCancelWidget());
        table.getFlexCellFormatter().setHorizontalAlignment(3, 0, HasAlignment.ALIGN_CENTER);
        table.getFlexCellFormatter().setColSpan(3, 0, 3);

        layout.setWidget(0, 0, table);
        setClickHandler();
    }

    protected void initComponents() {
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);

        listBox = new ListBox(true);
        listBox.setVisibleItemCount(15);
        listBox.setStyleName("pull_down");
        listBox.addStyleName("bg_white");
        listBox.setWidth("160px");

        selectedGroupMembersWidget = new GroupMembersWidget();
        selectedGroupMembersWidget.setDeleteMemberDelegate(new ServiceDelegate<AccountInfo>() {

            @Override
            public void execute(AccountInfo accountInfo) {
                selectedGroupMembersWidget.removeMember(accountInfo);
                listList.add(accountInfo);

                Collections.sort(listList, new Comparator<AccountInfo>() {
                    @Override
                    public int compare(AccountInfo o1, AccountInfo o2) {
                        return o1.getFullName().compareTo(o2.getFullName());
                    }
                });

                int index = listList.indexOf(accountInfo);
                listBox.insertItem(accountInfo.getFullName(), accountInfo.getId() + "", index);
            }
        });

        icon = new Icon(FAIconType.ARROW_RIGHT);
        icon.addStyleName("font-11em");

        available = new HashMap<Long, AccountInfo>();
        save = new Button("<span style=\"font-size: 12px\"><i class=\"" + FAIconType.SAVE.getStyleName()
                                  + "\"></i> Save</span>");
        cancel = new HTML("Cancel");
        cancel.setStyleName("display-inline");
        cancel.addStyleName("font-75em");
        cancel.addStyleName("footer_feedback_widget");
        listList = new ArrayList<AccountInfo>();

        registeredUserBox = new TextBox();
        registeredUserBox.setWidth("180px");
        registeredUserBox.setStyleName("input_box");
        registeredUserBox.getElement().setAttribute("placeHolder", "Enter registered user email");
        registeredUserBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                addUser.setEnabled(!registeredUserBox.getText().isEmpty());
            }
        });

        addUser = new Button("<i class=\"" + FAIconType.OK.getStyleName() + "\"></i> Add");
        addUser.setEnabled(false);
    }

    public String getRegisteredUserEmailInput() {
        return this.registeredUserBox.getText();
    }

    public void setAddUserClickHandler(ClickHandler handler) {
        this.addUser.addClickHandler(handler);
    }

    protected Widget createMembersPanel() {
        FlexTable verticalPanel = new FlexTable();
        verticalPanel.setWidget(0, 0, registeredUserBox);
        verticalPanel.getFlexCellFormatter().setHeight(0, 0, "30px");
        verticalPanel.getFlexCellFormatter().setWidth(0, 0, "190px");
        verticalPanel.setWidget(0, 1, addUser);
        verticalPanel.setWidget(1, 0, selectedGroupMembersWidget);
        verticalPanel.getFlexCellFormatter().setColSpan(1, 0, 2);
        return verticalPanel;
    }

    protected Widget createSaveCancelWidget() {
        HTMLPanel panel = new HTMLPanel("<span id=\"member_create_save\"></span>&nbsp;"
                                                + "<span id=\"create_cancel\"></span>");
        panel.add(save, "member_create_save");
        panel.add(cancel, "create_cancel");
        return panel;
    }

    public void setAvailableAccounts(ArrayList<AccountInfo> list) {
        for (AccountInfo info : list) {
            listBox.addItem(info.getFullName(), info.getId() + "");
            available.put(info.getId(), info);
            listList.add(info);
        }
    }

    public void addVerifiedMember(AccountInfo info) {
        if (info == null && registeredUserBox.getText() != null) {
            registeredUserBox.setStyleName("input_box_error");
            return;
        }

        registeredUserBox.setStyleName("input_box");
        registeredUserBox.setText("");
        selectedGroupMembersWidget.addMember(info);
    }

    public void setSaveHandler(ClickHandler handler) {
        save.addClickHandler(handler);
    }

    public void setCancelHandler(ClickHandler handler) {
        cancel.addClickHandler(handler);
    }

    /**
     * @return list of members selected to be added to the group
     */
    public ArrayList<AccountInfo> getSelectedMembers() {
        return selectedGroupMembersWidget.getMemberList();
    }

    public void setSelectedMembers(ArrayList<AccountInfo> members) {
        selectedGroupMembersWidget.setMemberList(members);

        for (AccountInfo member : members) {
            AccountInfo info = available.get(member.getId());

            if (info != null) {
                int index = listList.indexOf(info);
                if (index < 0)
                    continue;
                listList.remove(info);
                listBox.removeItem(index);
            }
        }
    }

    private void setClickHandler() {
        icon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int selected;

                while ((selected = listBox.getSelectedIndex()) != -1) {
                    String value = listBox.getValue(selected);
                    listBox.removeItem(selected);
                    long id = Long.decode(value).longValue();
                    AccountInfo info = available.get(id);
                    selectedGroupMembersWidget.addMember(info);
                    listList.remove(info);
                }
                event.stopPropagation();
                event.preventDefault();
            }
        });
    }

    public void reset() {
        selectedGroupMembersWidget.setMemberList(new ArrayList<AccountInfo>());
        // TODO : clear all selections
        // TODO : called when cancel is clicked
    }
}

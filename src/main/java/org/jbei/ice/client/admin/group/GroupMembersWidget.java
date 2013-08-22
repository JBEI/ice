package org.jbei.ice.client.admin.group;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Widget that displays details about members of a selected group
 *
 * @author Hector Plahar
 */
public class GroupMembersWidget extends Composite {

    private final ArrayList<User> infoList;
    private final ListDataProvider<User> dataProvider;
    private final VerticalPanel vPanel;
    private GroupMemberTable memberTable;

    public GroupMembersWidget() {
        infoList = new ArrayList<User>();
        dataProvider = new ListDataProvider<User>();
        vPanel = new VerticalPanel();
        initWidget(vPanel);
    }

    public void setDeleteMemberDelegate(ServiceDelegate<User> deleteDelegate) {
        memberTable = new GroupMemberTable(deleteDelegate);
        memberTable.setWidth("100%");
        ScrollPanel panel = new ScrollPanel(memberTable);
        dataProvider.addDataDisplay(memberTable);
        TablePager pager = new TablePager();
        pager.setDisplay(memberTable);
        vPanel.add(panel);
        vPanel.add(pager);
    }

    public void setMemberList(ArrayList<User> list) {
        infoList.clear();
        dataProvider.getList().clear();

        if (list.isEmpty())
            return;

        infoList.addAll(list);
        dataProvider.getList().addAll(infoList);
    }

    public ArrayList<User> getMemberList() {
        return infoList;
    }

    public void addMember(User info) {
        if (info == null)
            return;

        for (User user : infoList) {
            if (info.getEmail().equals(user.getEmail()))
                return;
        }

        infoList.add(info);
        dataProvider.getList().add(info);
        memberTable.setRowCount(infoList.size());
    }

    public void removeMember(User info) {
        if (info == null)
            return;

        infoList.remove(info);
        dataProvider.getList().remove(info);
        memberTable.setRowCount(infoList.size());
    }
}

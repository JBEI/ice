package org.jbei.ice.client.admin.group;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.shared.dto.AccountInfo;

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

    private final ArrayList<AccountInfo> infoList;
    private final ListDataProvider<AccountInfo> dataProvider;
    private final VerticalPanel vPanel;
    private GroupMemberTable memberTable;

    public GroupMembersWidget() {
        infoList = new ArrayList<AccountInfo>();
        dataProvider = new ListDataProvider<AccountInfo>();
        vPanel = new VerticalPanel();
        initWidget(vPanel);
    }

    public void setDeleteMemberDelegate(ServiceDelegate<AccountInfo> deleteDelegate) {
        memberTable = new GroupMemberTable(deleteDelegate);
        memberTable.setWidth("100%");
        ScrollPanel panel = new ScrollPanel(memberTable);
        dataProvider.addDataDisplay(memberTable);
        TablePager pager = new TablePager();
        pager.setDisplay(memberTable);
        vPanel.add(panel);
        vPanel.add(pager);
    }

    public void setMemberList(ArrayList<AccountInfo> list) {
        infoList.clear();
        infoList.addAll(list);
        dataProvider.setList(infoList);
    }

    public ArrayList<AccountInfo> getMemberList() {
        return infoList;
    }

    public void addMember(AccountInfo info) {
        infoList.add(info);
        dataProvider.getList().add(info);
        memberTable.setRowCount(infoList.size());
    }

    public void removeMember(AccountInfo info) {
        infoList.remove(info);
        dataProvider.getList().remove(info);
        memberTable.setRowCount(infoList.size());
    }
}

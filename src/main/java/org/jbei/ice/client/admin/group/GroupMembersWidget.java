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
        dataProvider.getList().clear();

        if (list.isEmpty())
            return;

        infoList.addAll(list);
        dataProvider.getList().addAll(infoList);
    }

    public ArrayList<AccountInfo> getMemberList() {
        return infoList;
    }

    public void addMember(AccountInfo info) {
        if (info == null)
            return;

        for (AccountInfo accountInfo : infoList) {
            if (info.getEmail().equals(accountInfo.getEmail()))
                return;
        }

        infoList.add(info);
        dataProvider.getList().add(info);
        memberTable.setRowCount(infoList.size());
    }

    public void removeMember(AccountInfo info) {
        if (info == null)
            return;

        infoList.remove(info);
        dataProvider.getList().remove(info);
        memberTable.setRowCount(infoList.size());
    }
}

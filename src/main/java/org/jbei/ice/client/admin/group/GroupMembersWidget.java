package org.jbei.ice.client.admin.group;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.entry.view.table.TablePager;
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

    public GroupMembersWidget(ServiceDelegate<AccountInfo> deleteDelegate) {
        infoList = new ArrayList<AccountInfo>();
        GroupMemberTable memberTable = new GroupMemberTable();
        memberTable.setDeleteDelegate(deleteDelegate);
        ScrollPanel panel = new ScrollPanel(memberTable);
        dataProvider = new ListDataProvider<AccountInfo>();
        dataProvider.addDataDisplay(memberTable);

        VerticalPanel vPanel = new VerticalPanel();
        TablePager pager = new TablePager();
        pager.setDisplay(memberTable);
        initWidget(vPanel);

        vPanel.add(panel);
        vPanel.add(pager);
    }

    public void setMemberList(ArrayList<AccountInfo> list) {
        infoList.clear();
        infoList.addAll(list);
        dataProvider.setList(infoList);
    }
}

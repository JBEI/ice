package org.jbei.ice.client.admin.group;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.view.client.ListDataProvider;

/**
 * @author Hector Plahar
 */
public class SelectGroupMemberTable extends GroupMemberTable {

    private final ListDataProvider<AccountInfo> dataProvider;

    public SelectGroupMemberTable() {
        super();
        this.dataProvider = new ListDataProvider<AccountInfo>();
        this.dataProvider.addDataDisplay(this);
    }

    public void setData(ArrayList<AccountInfo> data) {
        dataProvider.setList(data);
    }

    @Override
    protected void createColumns() {
        createSelectionColumn();
        createIDColumn();
        createNameColumn();
        createEmailColumn();
    }
}

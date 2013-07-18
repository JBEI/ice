package org.jbei.ice.client.admin.group;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.view.client.ListDataProvider;

/**
 * @author Hector Plahar
 */
public class SelectGroupMemberTable extends GroupMemberTable {

    private final ListDataProvider<User> dataProvider;

    public SelectGroupMemberTable() {
        super(null);
        this.dataProvider = new ListDataProvider<User>();
        this.dataProvider.addDataDisplay(this);
    }

    public void setData(ArrayList<User> data) {
        dataProvider.setList(data);
    }
}
package org.jbei.ice.client.bulkupload.widget;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Widget that allows user to select the global readable permissions for
 * groups
 *
 * @author Hector Plahar
 */
public class PermissionsSelection extends Composite {

    private ListBox selection;
    private HTMLPanel panel;

    public PermissionsSelection() {
        initComponents();

        initWidget(panel);
        panel.add(selection, "selection_visibility");
    }

    protected void initComponents() {

        panel = new HTMLPanel(
                "<span class=\"bulk_upload_visibility\">Group visibility </span><span " +
                        "id=\"selection_visibility\"></span>");

        selection = new ListBox();
        selection.setStyleName("pull_down");
    }

    public void setGroups(ArrayList<GroupInfo> result) {
        if (result == null)
            return;

        selection.clear();
        for (GroupInfo info : result) {
            selection.addItem(info.getLabel(), info.getUuid());
        }

        // add private
        selection.addItem("Private", "");

        // TODO : this is a hack and needs to go away when we have groups management
        if (selection.getItemCount() > 1)
            selection.setSelectedIndex(1);
    }

    public String getSelectedGroupUUID() {
        return selection.getValue(selection.getSelectedIndex());
    }

    public void setSelected(GroupInfo groupInfo) {

        for (int i = 0; i < selection.getItemCount(); i += 0) {

            // null group implies private selection
            String uuid = "";
            if (groupInfo != null) {
                uuid = groupInfo.getUuid();
            }

            if (uuid.equals(selection.getValue(i).trim())) {
                selection.setSelectedIndex(i);
                break;
            }
        }
    }
}

package org.jbei.ice.client.profile.group.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Widget for creating a group by entering name and description
 *
 * @author Hector Plahar
 */
public class CreateGroupCell extends Composite {

    private final TextBox groupName;
    private final TextBox groupDescription;

    public CreateGroupCell() {
        groupName = new TextBox();
        groupName.setStyleName("input_box");
        groupName.getElement().setAttribute("placeHolder", "Enter Group Name");
        groupName.setVisibleLength(35);
        groupName.setMaxLength(35);

        groupDescription = new TextBox();
        groupDescription.setStyleName("input_box");
        groupDescription.getElement().setAttribute("placeHolder", "Enter Group Description");
        groupDescription.setVisibleLength(35);
        groupDescription.setMaxLength(45);

        HTMLPanel panel = new HTMLPanel("<span id=\"new_group_name_input\"></span>"
                                                + "<span class=\"required\">*</span>"
                                                + "<br><span id=\"new_group_description_input\"></span>");
        panel.add(groupName, "new_group_name_input");
        panel.add(groupDescription, "new_group_description_input");

        initWidget(panel);
    }

    public String getGroupName() {
        return groupName.getText();
    }

    public void setGroupName(String name) {
        if (name.length() > 35)
            name = name.substring(0, 34);
        this.groupName.setText(name);
    }

    public String getGroupDescription() {
        return groupDescription.getText();
    }

    public void setGroupDescription(String description) {
        if (description.length() > 45)
            description = description.substring(0, 44);
        this.groupDescription.setText(description);
    }

    public void reset() {
        groupName.setText("");
        groupDescription.setText("");
    }

    public boolean validate() {
        if (groupName.getText().isEmpty()) {
            groupName.setStyleName("input_box_error");
            return false;
        }
        groupName.setStyleName("input_box");
        return true;
    }
}

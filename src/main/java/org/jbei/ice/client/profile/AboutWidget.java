package org.jbei.ice.client.profile;

import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

public class AboutWidget extends Composite {

    public AboutWidget(AccountInfo info) {

        FlexTable contents = new FlexTable();
        initWidget(contents);

        contents.setHTML(0, 0, "<b>Name:</b>");
        contents.getCellFormatter().setWidth(0, 0, "150px");
        String fullName = info.getFirstName() + " " + info.getLastName();
        contents.setHTML(0, 1, fullName);

        contents.setHTML(1, 0, "<b>Email:</b>");
        contents.setHTML(1, 1, info.getEmail());

        contents.setHTML(2, 0, "<b>Member since:</b>");
        contents.setHTML(2, 1, info.getSince());

        contents.setHTML(3, 0, "<b>Institution:</b>");
        contents.setHTML(3, 1, info.getInstitution());

        contents.setHTML(4, 0, "<b>Description:</b>");
        contents.setHTML(4, 1, info.getDescription());
    }
}

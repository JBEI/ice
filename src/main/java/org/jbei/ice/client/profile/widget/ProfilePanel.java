package org.jbei.ice.client.profile.widget;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author Hector Plahar
 */
public class ProfilePanel extends Composite {

    private final HTML panel;
    private final FlexTable table;
    private final Button editProfileButton;
    private final Button changePasswordButton;
    private HandlerRegistration editRegistration;
    private HandlerRegistration changeRegistration;

    public ProfilePanel() {
        panel = new HTML();
        table = new FlexTable();
        table.setWidth("100%");
        initWidget(table);

        table.setStyleName("margin-top-20");

        editProfileButton = new Button("<i class=\"" + FAIconType.EDIT.getStyleName() + "\"></i> " + "Edit Profile");
        editProfileButton.setVisible(false);
        changePasswordButton = new Button(
                "<i class=\"" + FAIconType.KEY.getStyleName() + "\"></i> " + "Change Password");
        changePasswordButton.setVisible(false);

        table.setWidget(0, 0, editProfileButton);
        table.getCellFormatter().setWidth(0, 0, "90px");
        table.setWidget(0, 1, changePasswordButton);
        table.setWidget(1, 0, panel);
        table.getFlexCellFormatter().setColSpan(1, 0, 2);
    }

    public void setAccountInfo(AccountInfo info) {
        panel.setHTML("<span style=\"padding-top: 10px; font-size: 0.80em; x\"> " + info.getDescription() + "</span>");
        table.setWidget(1, 0, panel);
    }

    public void setEditProfileButtonHandler(ClickHandler handler) {
        if (editRegistration != null)
            editRegistration.removeHandler();

        editProfileButton.setVisible(true);
        editRegistration = editProfileButton.addClickHandler(handler);
    }

    public void setChangePasswordButtonHandler(ClickHandler handler) {
        if (changeRegistration != null)
            changeRegistration.removeHandler();

        changePasswordButton.setVisible(true);
        changeRegistration = changePasswordButton.addClickHandler(handler);
    }
}

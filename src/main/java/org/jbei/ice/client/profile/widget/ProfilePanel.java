package org.jbei.ice.client.profile.widget;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.login.RegistrationDetails;
import org.jbei.ice.client.profile.ChangePasswordPanel;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * @author Hector Plahar
 */
public class ProfilePanel extends Composite implements IUserProfilePanel {

    private final FlexTable table;
    private final Button editProfile;
    private final Button changePasswordButton;
    private final Button sendMessage;
    private HandlerRegistration editRegistration;
    private HandlerRegistration changeRegistration;
    private EditProfilePanel editProfilePanel;
    private ChangePasswordPanel changePasswordPanel;

    public ProfilePanel() {
        table = new FlexTable();
        table.setWidth("100%");
        initWidget(table);

        sendMessage = new Button("<i class=\"blue " + FAIconType.ENVELOPE.getStyleName() + "\"></i> " + "Send Message");
        editProfile = new Button("<i class=\"blue " + FAIconType.EDIT.getStyleName() + "\"></i> " + "Edit Profile");
        editProfile.setVisible(false);
        changePasswordButton = new Button(
                "<i style=\"color: #007dbc\" class=\"" + FAIconType.KEY.getStyleName() + "\"></i> "
                        + "Change Password");
        changePasswordButton.setVisible(false);

        String html = "<span id=\"send_message_btn\"></span>"
                + " <span id=\"edit_profile_btn\"></span>"
                + " <span id=\"change_password_btn\"></span>";

        HTMLPanel panel = new HTMLPanel(html);
//        panel.add(sendMessage, "send_message_btn");
        panel.add(editProfile, "edit_profile_btn");
        panel.add(changePasswordButton, "change_password_btn");

        table.setWidget(0, 0, panel);
        table.setHTML(1, 0, "&nbsp;");
    }

    public void setAccountInfo(AccountInfo info) {
        table.setHTML(1, 0, "<br><span style=\"padding: 10px 0 0 3px; font-size: 0.90em; color: #444\"> "
                + info.getDescription() + "</span>");
        sendMessage.setVisible(!info.getEmail().equalsIgnoreCase(ClientController.account.getEmail()));
    }

    public void setEditProfileButtonHandler(ClickHandler handler) {
        if (editRegistration != null)
            editRegistration.removeHandler();

        editProfile.setVisible(true);
        editRegistration = editProfile.addClickHandler(handler);
    }

    public void setChangePasswordButtonHandler(ClickHandler handler) {
        if (changeRegistration != null)
            changeRegistration.removeHandler();

        changePasswordButton.setVisible(true);
        changeRegistration = changePasswordButton.addClickHandler(handler);
    }

    public String getUpdatedPassword() {
        return changePasswordPanel.getPassword();
    }

    public void editProfile(AccountInfo currentInfo, ClickHandler submitHandler, ClickHandler cancelHandler) {
        RegistrationDetails details = new RegistrationDetails();
        details.setAbout(currentInfo.getDescription());
        details.setFirstName(currentInfo.getFirstName());
        details.setLastName(currentInfo.getLastName());
        details.setInstitution(currentInfo.getInstitution());
        details.setInitials(currentInfo.getInitials());
        details.setEmail(currentInfo.getEmail());
        editProfilePanel = new EditProfilePanel(details);
        editProfilePanel.addSubmitClickHandler(submitHandler);
        editProfilePanel.addCancelHandler(cancelHandler);
        table.setWidget(1, 0, editProfilePanel);
    }

    public RegistrationDetails getUpdatedDetails() {
        if (editProfilePanel == null)
            return null;

        return editProfilePanel.getDetails();
    }

    public void changePasswordPanel(AccountInfo currentInfo, ClickHandler submitHandler, ClickHandler cancelHandler) {
        changePasswordPanel = new ChangePasswordPanel(currentInfo.getEmail());
        changePasswordPanel.addSubmitClickHandler(submitHandler);
        changePasswordPanel.addCancelHandler(cancelHandler);
        table.setWidget(1, 0, changePasswordPanel);
    }
}

package org.jbei.ice.client.profile;

import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.login.RegistrationDetails;
import org.jbei.ice.client.profile.widget.EditProfilePanel;
import org.jbei.ice.client.profile.widget.ProfilePanel;
import org.jbei.ice.client.profile.widget.UserOption;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * View for profile page and companion to {@link ProfilePresenter}
 *
 * @author Hector Plahar
 */
public class ProfileView extends AbstractLayout implements IProfileView {

    private ProfileViewMenu menu;
    private FlexTable mainContent;
    private EditProfilePanel panel;
    private ChangePasswordPanel changePasswordPanel;
    private HTML contentHeader;
    private ProfilePanel profilePanel;

    @Override
    protected Widget createContents() {
        contentHeader = new HTML();
        mainContent = new FlexTable();
        menu = new ProfileViewMenu();
        profilePanel = new ProfilePanel();
        return createMainContent();
    }

    @Override
    public SingleSelectionModel<UserOption> getUserSelectionModel() {
        return this.menu.getSelectionModel();
    }

    public String getUpdatedPassword() {
        return changePasswordPanel.getPassword();
    }

    protected Widget createMainContent() {
        mainContent.setWidth("100%");
        mainContent.setCellPadding(0);
        mainContent.setCellSpacing(0);
        mainContent.setWidget(0, 0, contentHeader);
        mainContent.getFlexCellFormatter().setColSpan(0, 0, 2);
        mainContent.setWidget(1, 0, menu);
        mainContent.getFlexCellFormatter().setWidth(1, 0, "200px");
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
        return mainContent;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setContents(AccountInfo info) {
        if (info == null) {
            Label widget = new Label("Could not retrieve user account information. Please try again.");
            mainContent.setWidget(1, 0, widget);
        } else {
            contentHeader.setHTML("<span style=\"margin-left: 12px; font-size: 11px; "
                                          + " background-color: #92AFD7;"
                                          + " padding: 5px; -webkit-border-radius: 2px; border-radius: 2px;"
                                          + "-moz-border-radius: 2px;\">Last Login: " + info.getLastLogin() + "</span>"
                                          + "<span style=\"margin-left: 65px; font-size: 2em; color: #777; "
                                          + "font-weight: bold;"
                                          + "text-transform: uppercase;\">" + info.getFullName() + "</span>"
                                          + "<br><span style=\"margin-left: 209px; font-size: 11px; font-weight: bold; "
                                          + "text-transform: uppercase; position: relative; top: -6px; color: #999\">"
                                          + info.getInstitution() + "</span>");

            profilePanel.setAccountInfo(info);
            if (menu.getSelectionModel().getSelectedObject() == UserOption.PROFILE)
                mainContent.setWidget(1, 1, profilePanel);
        }
    }

    @Override
    public void setEntryContent(CollectionDataTable collectionsDataTable) {
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("margin-top-20");
        panel.setWidth("100%");
        panel.add(collectionsDataTable);
        panel.add(collectionsDataTable.getPager());
        mainContent.setWidget(1, 1, panel);
    }

    @Override
    public void addEditProfileLinkHandler(ClickHandler editProfileHandler) {
        profilePanel.setEditProfileButtonHandler(editProfileHandler);
    }

    @Override
    public void addChangePasswordLinkHandler(ClickHandler changePasswordHandler) {
        profilePanel.setChangePasswordButtonHandler(changePasswordHandler);
    }

    @Override
    public void editProfile(AccountInfo currentInfo, ClickHandler submitHandler, ClickHandler cancelHandler) {
        RegistrationDetails details = new RegistrationDetails();
        details.setAbout(currentInfo.getDescription());
        details.setFirstName(currentInfo.getFirstName());
        details.setLastName(currentInfo.getLastName());
        details.setInstitution(currentInfo.getInstitution());
        details.setInitials(currentInfo.getInitials());
        details.setEmail(currentInfo.getEmail());
        panel = new EditProfilePanel(details);
        panel.addSubmitClickHandler(submitHandler);
        panel.addCancelHandler(cancelHandler);

        mainContent.setWidget(1, 1, panel);
    }

    @Override
    public RegistrationDetails getUpdatedDetails() {
        if (panel == null)
            return null;

        return panel.getDetails();
    }

    @Override
    public void changePasswordPanel(AccountInfo currentInfo, ClickHandler submitHandler, ClickHandler cancelHandler) {
        changePasswordPanel = new ChangePasswordPanel(currentInfo.getEmail());
        changePasswordPanel.addSubmitClickHandler(submitHandler);
        changePasswordPanel.addCancelHandler(cancelHandler);
        mainContent.setWidget(1, 1, changePasswordPanel);
    }
}

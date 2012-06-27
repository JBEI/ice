package org.jbei.ice.client.profile;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.collection.table.SamplesDataTable;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.login.RegistrationDetails;
import org.jbei.ice.shared.dto.AccountInfo;

public class ProfileView extends AbstractLayout implements IProfileView {

    private Widget sampleView;

    private Label contentHeader;
    private ProfileViewMenu menu;
    private FlexTable mainContent;
    private HTMLPanel profileHeader;
    private AboutWidget accountWidget;
    private EditProfilePanel panel;
    private ChangePasswordPanel changePasswordPanel;

    @Override
    protected Widget createContents() {
        contentHeader = new Label("");
        contentHeader.setStyleName("profile_header");

        accountWidget = new AboutWidget();

        mainContent = new FlexTable();
        //        createEntriesTablePanel();

        profileHeader = new HTMLPanel(
                "<span id=\"profile_header_text\"></span><div style=\"float: right\"><span " +
                        "id=\"edit_profile_link\"></span>"
                        + "<span style=\"color: #262626; font-size: 0.75em;\">|</span>"
                        + " <span id=\"change_password_link\"></span></div>");
        profileHeader.add(contentHeader, "profile_header_text");

        FlexTable contentTable = new FlexTable();
        contentTable.setWidth("100%");
        contentTable.setWidget(0, 0, createMenu());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getCellFormatter().setWidth(0, 1, "100%");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
        return contentTable;
    }

    public String getUpdatedPassword() {
        return changePasswordPanel.getPassword();
    }

    protected Widget createMenu() {
        menu = new ProfileViewMenu();
        return menu;
    }

    protected Widget createMainContent() {
        mainContent.setCellPadding(3);
        mainContent.setWidth("100%");
        mainContent.setCellSpacing(0);
        mainContent.addStyleName("add_new_entry_main_content_wrapper");
        mainContent.setWidget(0, 0, profileHeader);

        // sub content
        mainContent.setWidget(1, 0, new HTML("&nbsp;"));
        return mainContent;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setContents(AccountInfo info) {

        if (info == null) {
            Label widget = new Label(
                    "Could not retrieve user account information. Please try again.");
            mainContent.setWidget(1, 0, widget);
        } else {
            accountWidget.setAccountInfo(info);
            mainContent.setWidget(1, 0, accountWidget);
            contentHeader.setText(info.getFirstName() + " " + info.getLastName());
        }
    }

    @Override
    public void setEntryContent(CollectionDataTable collectionsDataTable) {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.add(collectionsDataTable);
        panel.add(collectionsDataTable.getPager());
        mainContent.setWidget(1, 0, panel);
    }

    @Override
    public ProfileViewMenu getMenu() {
        return this.menu;
    }

    private Widget createSamplesTablePanel(SamplesDataTable samplesTable) {

        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);

        samplesTable.addStyleName("gray_border");
        table.setWidget(0, 0, samplesTable);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(samplesTable);
        table.setWidget(1, 0, tablePager);

        return table;
    }

    @Override
    public void setSampleView(SamplesDataTable table) {
        if (sampleView == null)
            sampleView = createSamplesTablePanel(table);
        mainContent.setWidget(1, 0, sampleView);
    }

    @Override
    public void addEditProfileLinkHandler(ClickHandler editProfileHandler) {
        Label label = new Label("Edit Profile");
        label.addClickHandler(editProfileHandler);
        label.setStyleName("open_sequence_sub_link");
        profileHeader.add(label, "edit_profile_link");
    }

    @Override
    public void addChangePasswordLinkHandler(ClickHandler changePasswordHandler) {
        Label label = new Label("Change Password");
        label.addClickHandler(changePasswordHandler);
        label.setStyleName("open_sequence_sub_link");
        profileHeader.add(label, "change_password_link");
    }

    @Override
    public void editProfile(AccountInfo currentInfo, ClickHandler submitHandler,
            ClickHandler cancelHandler) {
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

        mainContent.setWidget(1, 0, panel);
    }

    @Override
    public RegistrationDetails getUpdatedDetails() {
        if (panel == null)
            return null;

        return panel.getDetails();
    }

    @Override
    public void changePasswordPanel(AccountInfo currentInfo, ClickHandler submitHandler,
            ClickHandler cancelHandler) {
        changePasswordPanel = new ChangePasswordPanel(currentInfo.getEmail());
        changePasswordPanel.addSubmitClickHandler(submitHandler);
        changePasswordPanel.addCancelHandler(cancelHandler);

        mainContent.setWidget(1, 0, changePasswordPanel);
    }
}

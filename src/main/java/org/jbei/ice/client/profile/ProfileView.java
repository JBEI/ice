package org.jbei.ice.client.profile;

import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.lib.shared.dto.AccountInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
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
    private HTML menuHeader;
    private HTML accountHeader;

    public ProfileView(String userId) {
        super();
        menu = new ProfileViewMenu(userId);
        menu.addStyleName("margin-top-20");
        mainContent.setWidget(1, 0, menu);
        mainContent.getFlexCellFormatter().setWidth(1, 0, "200px");
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        mainContent = new FlexTable();
        menuHeader = new HTML();
        accountHeader = new HTML();
    }

    @Override
    protected Widget createContents() {
        mainContent.setWidth("100%");
        mainContent.setCellPadding(0);
        mainContent.setCellSpacing(0);
        mainContent.setWidget(0, 0, menuHeader);
        mainContent.setWidget(0, 1, accountHeader);

        mainContent.setWidget(1, 0, new HTML("&nbsp;"));
        mainContent.getFlexCellFormatter().setWidth(1, 0, "200px");
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        return mainContent;
    }

    @Override
    public SingleSelectionModel<UserOption> getUserSelectionModel() {
        return this.menu.getSelectionModel();
    }

    @Override
    public void setMenuSelection(UserOption option) {
        menu.showSelected(option);
    }

    @Override
    public void setMenuOptions(UserOption... menuOptions) {
        menu.createMenu(menuOptions);
    }

    @Override
    public void show(UserOption selected, Widget widget) {
        mainContent.setWidget(1, 1, widget);
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setAccountInfo(AccountInfo info) {
        if (info == null) {
            Label widget = new Label("Could not retrieve user account information. Please try again.");
            mainContent.setWidget(1, 1, widget);
        } else {
            String lastLogin = DateUtilities.formatMediumDate(info.getLastLogin());
            menuHeader.setHTML("<span style=\"margin-left: 12px; font-size: 12px; background-color: #92AFD7; "
                                       + "text-shadow: #555 1px 1px 1px; color: #fefefe;"
                                       + " padding: 5px; border-radius: 2px;"
                                       + "\">Last Login: " + lastLogin + "</span>");

            accountHeader.setHTML("<span style=\"font-size: 2em; color: #777; "
                                          + "font-weight: bold;"
                                          + "text-transform: uppercase;\">" + info.getFullName() + "</span>"
                                          + "<br>"
                                          + "<span style=\"font-size: 11px; font-weight: bold;"
                                          + "text-transform: uppercase; position: relative; top: -6px; color: #999\">"
                                          + info.getInstitution() + "</span>");
        }
    }
}

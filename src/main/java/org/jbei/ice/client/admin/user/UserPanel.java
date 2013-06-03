package org.jbei.ice.client.admin.user;

import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.*;

/**
 * Admin user panel. contains the dialog for new user registration
 *
 * @author Hector Plahar
 */
public class UserPanel extends Composite implements IAdminPanel {

    private final UserTable table;
    private final Button createAccount;
    private RegistrationDialog dialog;

    public UserPanel() {
        ScrollPanel panel = new ScrollPanel();
        initWidget(panel);

        createAccount = new Button("<i class=\"blue " + FAIconType.USER.getStyleName() + "\"></i> Create Account");

        table = new UserTable();
        table.setWidth("100%");

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(createAccount);
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(table);
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(table);
        vPanel.add(pager);
        vPanel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_CENTER);

        panel.add(vPanel);
        addCreateAccountHandler();
    }

    public AccountInfo getNewUserDetails() {
        AccountInfo info = new AccountInfo();
        info.setEmail(dialog.getUserId());
        info.setFirstName(dialog.getFirstName());
        info.setLastName(dialog.getLastName());
        info.setInstitution(dialog.getInstitution());
        info.setDescription(dialog.getAbout());
        return info;
    }

    public void informOfDuplicateRegistrationEmail() {
        dialog.showAlreadyRegisteredEmailAlert();
    }

    private void addCreateAccountHandler() {
        dialog = new RegistrationDialog();
        createAccount.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialog.showDialog();
            }
        });
    }

    public void setRegistrationHandler(ClickHandler clickHandler) {
        dialog.addSubmitHandler(clickHandler);
    }

    public UserTable getUserTable() {
        return this.table;
    }

    public void showResult(String result) {
        if (result == null)
            dialog.showError();
        else
            dialog.showPassword(result);
    }

    private class RegistrationDialog extends Composite {

        private DialogBox box;
        private Button submit;
        private Label cancel;
        private TextBox givenName;
        private TextBox familyName;
        private TextBox userId;
        private TextArea aboutYourself;
        private TextBox institution;
        private Label alreadyRegistered;
        private HTMLPanel userIdPanel;
        private FlexTable inputTable;

        public RegistrationDialog() {
            initComponents();
            inputTable = new FlexTable();
            inputTable.setWidth("100%");
            inputTable.setCellPadding(3);
            initWidget(inputTable);

            layoutElements();
            box.setWidget(this);

            // handlers
            addCancelHandler();
        }

        public void addSubmitHandler(final ClickHandler handler) {
            submit.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (!validates())
                        return;
                    handler.onClick(event);
                }
            });
        }

        public String getUserId() {
            return userId.getText().trim();
        }

        public String getFirstName() {
            return givenName.getText().trim();
        }

        public String getLastName() {
            return familyName.getText().trim();
        }

        public String getInstitution() {
            return institution.getText().trim();
        }

        public String getAbout() {
            return aboutYourself.getText().trim();
        }

        protected boolean validates() {
            boolean validates = true;
            if (givenName.getText().trim().isEmpty()) {
                givenName.setStyleName("input_box_error");
                validates = false;
            } else {
                givenName.setStyleName("input_box");
            }

            if (familyName.getText().trim().isEmpty()) {
                familyName.setStyleName("input_box_error");
                validates = false;
            } else {
                familyName.setStyleName("input_box");
            }

            if (userId.getText().trim().isEmpty()) {
                userId.setStyleName("input_box_error");
                validates = false;
            } else {
                userId.setStyleName("input_box");
            }

            return validates;
        }

        private void initComponents() {
            submit = new Button("Submit");
            cancel = new Label("Cancel");
            cancel.setStyleName("footer_feedback_widget");
            cancel.addStyleName("font-80em");
            cancel.addStyleName("display-inline");

            givenName = createStandardTextBox("205px");
            familyName = createStandardTextBox("205px");
            userId = createStandardTextBox("205px");
            userId.getElement().setAttribute("placeHolder", "Enter unique user identifier");
            institution = createStandardTextBox("205px");
            aboutYourself = createTextArea("205px", "60px");
            alreadyRegistered = new Label("Already registered");
            alreadyRegistered.setStyleName("required");
            alreadyRegistered.addStyleName("font-70em");

            userIdPanel = new HTMLPanel("<span id=\"email_input_box\"></span> <span id=\"email_error_msg\"></span>");

            box = new DialogBox();
            box.setWidth("380px");
            box.setModal(true);
            box.setHTML("Enter registration information for new user");
            box.setGlassEnabled(true);
            box.setGlassStyleName("dialog_box_glass");
        }

        private void layoutElements() {
            int row = 0;

            // given name
            createLabel("Given Name", row, true);
            inputTable.setWidget(row, 1, givenName);

            // family name
            row += 1;
            createLabel("Family Name", row, true);
            inputTable.setWidget(row, 1, familyName);

            // user Id
            row += 1;
            createLabel("UserId", row, true);
            userIdPanel.add(userId, "email_input_box");
            inputTable.setWidget(row, 1, userIdPanel);

            // institution
            row += 1;
            createLabel("Institution", row, false);
            inputTable.setWidget(row, 1, institution);

            // about yourself
            row += 1;
            inputTable.setHTML(row, 0, "<span class=\"font-80em\" style=\"white-space:nowrap\"><b>About</b></span>");
            inputTable.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
            inputTable.getFlexCellFormatter().setWidth(row, 0, "100px");
            inputTable.setWidget(row, 1, aboutYourself);

            HTMLPanel buttonPanel = new HTMLPanel(
                    "<span id=\"submit_button\"></span> <span id=\"registration_cancel_link\"></span>");
            buttonPanel.add(submit, "submit_button");
            buttonPanel.add(cancel, "registration_cancel_link");

            row += 1;
            inputTable.getFlexCellFormatter().setColSpan(row, 0, 2);
            inputTable.setWidget(row, 0, buttonPanel);
            inputTable.getCellFormatter().setHorizontalAlignment(row, 0, HasAlignment.ALIGN_CENTER);
        }

        private void createLabel(String label, int row, boolean required) {
            String html = "<span class=\"font-80em\" style=\"white-space:nowrap\"><b>" + label + "</b>";
            if (required)
                html += " <span class=\"required\">*</span></span>";
            else
                html += "</span>";

            inputTable.setHTML(row, 0, html);
            inputTable.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
            inputTable.getFlexCellFormatter().setWidth(row, 0, "100px");
        }

        protected void addCancelHandler() {
            cancel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    box.hide();
                }
            });
        }

        protected TextBox createStandardTextBox(String width) {
            final TextBox box = new TextBox();
            box.setStyleName("input_box");
            box.addStyleName("pad-2");
            box.setWidth(width);
            return box;
        }

        protected TextArea createTextArea(String width, String height) {
            final TextArea area = new TextArea();
            area.setStyleName("input_box");
            area.setWidth(width);
            area.setHeight(height);
            return area;
        }

        public void showDialog() {
            box.center();
        }

        public void showPassword(String password) {
            String message = "<div style=\"padding: 8px 14px; font-size: 14px; text-shadow: 0 1px 0 #f3f3f3; "
                    + "background-color: #dff0d8; border-color: #468847; border-radius: 4px;\">"
                    + "<i style=\"color: #468847\" class=\"" + FAIconType.OK.getStyleName()
                    + "\"></i> Account with id \"" + userId.getText().trim() + "\" created successfully. "
                    + "The password is shown below <br><br><b style=\"font-size: 16px\">"
                    + password + "</b></div>";

            int row = inputTable.getRowCount() - 1;
            inputTable.getFlexCellFormatter().setColSpan(row, 0, 2);
            cancel.setText("Close");
            inputTable.setHTML(row, 0, message);

            row += 1;
            inputTable.getFlexCellFormatter().setColSpan(row, 0, 2);
            cancel.setText("Close");
            inputTable.setWidget(row, 0, cancel);
            inputTable.getCellFormatter().setHorizontalAlignment(row, 0, HasAlignment.ALIGN_RIGHT);
        }

        public void showError() {
            String message = "<div style=\"padding: 8px 14px; font-size: 14px; text-shadow: 0 1px 0 #f3f3f3; "
                    + "background-color: #f2dede; border-color: #b94a48; border-radius: 4px;\">"
                    + "<i style=\"color: #b94a48\" class=\"" + FAIconType.BAN_CIRCLE.getStyleName()
                    + "\"></i>Error creating account. Try again or contact the site admin.</div>";

            int row = inputTable.getRowCount() - 1;
            inputTable.getFlexCellFormatter().setColSpan(row, 0, 2);
            inputTable.setHTML(row, 0, message);

            row += 1;
            HTMLPanel buttonPanel = new HTMLPanel(
                    "<span id=\"submit_button\"></span> <span id=\"registration_cancel_link\"></span>");
            buttonPanel.add(submit, "submit_button");
            buttonPanel.add(cancel, "registration_cancel_link");

            inputTable.getFlexCellFormatter().setColSpan(row, 0, 2);
            inputTable.setWidget(row, 0, buttonPanel);
            inputTable.getCellFormatter().setHorizontalAlignment(row, 0, HasAlignment.ALIGN_CENTER);
        }

        public void showAlreadyRegisteredEmailAlert() {
            userId.setStyleName("input_box_error");
            userIdPanel.add(alreadyRegistered, "email_error_msg");
        }
    }
}

package org.jbei.ice.client.login;

import org.jbei.ice.client.login.RegistrationPanelPresenter.IRegistrationPanelView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for new user account registration
 *
 * @author Hector Plahar
 */
public class RegistrationPanel extends Composite implements IRegistrationPanelView {

    private Button submit;
    private Label cancel;
    private TextBox givenName;
    private TextBox familyName;
    private TextBox email;
    private TextArea aboutYourself;
    private TextBox institution;
    private Label alreadyRegistered;
    private HTMLPanel emailPanel;
    private FlexTable inputTable;

    private final RegistrationPanelPresenter presenter;

    public RegistrationPanel() {
        initComponents();

        submit.setStyleName("login_btn");
        cancel.setStyleName("footer_feedback_widget");
        cancel.addStyleName("font-80em");
        cancel.addStyleName("display-inline");

        alreadyRegistered.setStyleName("required");
        alreadyRegistered.addStyleName("font-70em");

        inputTable = new FlexTable();
        createInputTable();

        initWidget(inputTable);
        presenter = new RegistrationPanelPresenter(this);
    }

    public RegistrationPanelPresenter getPresenter() {
        return this.presenter;
    }

    @Override
    public boolean validates() {
        boolean validates = true;
        if (givenName.getText().isEmpty()) {
            givenName.setStyleName("input_box_error");
            validates = false;
        } else {
            givenName.setStyleName("input_box");
        }

        if (familyName.getText().isEmpty()) {
            familyName.setStyleName("input_box_error");
            validates = false;
        } else {
            familyName.setStyleName("input_box");
        }

        if (email.getText().isEmpty()) {
            email.setStyleName("input_box_error");
            validates = false;
        } else {
            email.setStyleName("input_box");
        }

        return validates;
    }

    @Override
    public HandlerRegistration addSubmitHandler(ClickHandler handler) {
        return submit.addClickHandler(handler);
    }

    @Override
    public HandlerRegistration addCancelHandler(ClickHandler handler) {
        return cancel.addClickHandler(handler);
    }

    private void initComponents() {
        submit = new Button("Submit");
        cancel = new Label("Cancel");
        givenName = createStandardTextBox("205px");
        familyName = createStandardTextBox("205px");
        email = createStandardTextBox("205px");
        institution = createStandardTextBox("205px");
        aboutYourself = createTextArea("205px", "60px");
        alreadyRegistered = new Label("Already registered");
        emailPanel = new HTMLPanel("<span id=\"email_input_box\"></span> <span id=\"email_error_msg\"></span>");
    }

    private Widget createInputTable() {
        int row = 0;

        inputTable.setHTML(row, 0, "<img style=\"margin-bottom: 30px;\" src=\"static/images/logo.png\" />");
        inputTable.getFlexCellFormatter().setColSpan(row, 0, 3);

        // given name
        row += 1;
        createLabel("Given Name", row, true);
        inputTable.setWidget(row, 1, givenName);

        // family name
        row += 1;
        createLabel("Family Name", row, true);
        inputTable.setWidget(row, 1, familyName);

        // email
        row += 1;
        createLabel("Email", row, true);
        emailPanel.add(email, "email_input_box");
        inputTable.setWidget(row, 1, emailPanel);

        // institution
        row += 1;
        createLabel("Institution", row, true);
        inputTable.setWidget(row, 1, institution);

        // about yourself
        row += 1;
        inputTable.setHTML(row, 0, "<span class=\"font-80em\" style=\"white-space:nowrap\">About yourself</span>");
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
        return inputTable;
    }

    private void createLabel(String label, int row, boolean required) {
        String html = "<span class=\"font-80em\" style=\"white-space:nowrap\">" + label;
        if (required)
            html += " <span class=\"required\">*</span></span>";
        else
            html += "</span>";

        inputTable.setHTML(row, 0, html);
        inputTable.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(row, 0, "100px");
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

    @Override
    public RegistrationDetails getDetails() {
        RegistrationDetails details = new RegistrationDetails();
        details.setAbout(this.aboutYourself.getText());
        details.setEmail(this.email.getText());
        details.setFirstName(this.givenName.getText());
        details.setLastName(this.familyName.getText());
        details.setInstitution(this.institution.getText());
        details.setInitials("");
        return details;
    }

    public void showAlreadyRegisteredEmailAlert() {
        email.setStyleName("input_box_error");
        emailPanel.add(alreadyRegistered, "email_error_msg");
    }
}

package org.jbei.ice.client.login;

import org.jbei.ice.client.login.RegistrationPanelPresenter.IRegistrationPanelView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class RegistrationPanel extends Composite implements IRegistrationPanelView {

    private FlowPanel panel = new FlowPanel();
    private Button submit;
    private Label cancel;
    private TextBox givenName;
    private TextBox familyName;
    private TextBox initials;
    private TextBox email;
    private TextArea aboutYourself;
    private TextBox institution;
    private Label alreadyRegistered;
    private HTMLPanel emailPanel;

    private final RegistrationPanelPresenter presenter;

    public RegistrationPanel() {

        initComponents();

        panel.setStyleName("login_panel");
        submit.setStyleName("login_btn");

        cancel.setStyleName("footer_feedback_widget");
        cancel.addStyleName("font-80em");
        cancel.addStyleName("display-inline");

        alreadyRegistered.setStyleName("required");
        alreadyRegistered.addStyleName("font-70em");

        FlexTable registrationTable = new FlexTable();
        registrationTable.setStyleName("login_table");
        registrationTable.setCellPadding(0);
        registrationTable.setCellSpacing(0);
        registrationTable.setHTML(1, 0, "<b>REGISTRATION</b>");
        registrationTable.getCellFormatter().setStyleName(1, 0, "pad-15");

        registrationTable.setHTML(2, 0, "<div style=\"height: 2px; background-color: #0082C0;"
                + "-webkit-box-shadow: 0px 1px 1px #999\"></div>");

        registrationTable.setWidget(3, 0, createInputTable());
        registrationTable.getFlexCellFormatter().setStyleName(3, 0, "pad-15");

        panel.add(registrationTable);
        initWidget(panel);
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
        initials = createStandardTextBox("50px");
        email = createStandardTextBox("205px");
        institution = createStandardTextBox("205px");
        aboutYourself = createTextArea("250px", "100px");
        alreadyRegistered = new Label("Aready registered");
        emailPanel = new HTMLPanel(
                "<span id=\"email_input_box\"></span> <span id=\"email_error_msg\"></span>");
    }

    private Widget createInputTable() {
        FlexTable inputTable = new FlexTable();

        // given name
        inputTable
                .setHTML(
                    0,
                    0,
                    "<span class=\"font-80em\" style=\"white-space:nowrap\">Given name <span class=\"required\">*</span></span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(0, 0, "150px");
        inputTable.setWidget(0, 1, givenName);

        // family name
        inputTable
                .setHTML(
                    1,
                    0,
                    "<span class=\"font-80em\" style=\"white-space:nowrap\">Family name <span class=\"required\">*</span></span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(1, 0, "150px");
        inputTable.setWidget(1, 1, familyName);

        // initials
        inputTable.setHTML(2, 0,
            "<span class=\"font-80em\" style=\"white-space:nowrap\">Initials</span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(2, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(2, 0, "150px");
        inputTable.setWidget(2, 1, initials);

        // email
        inputTable
                .setHTML(
                    3,
                    0,
                    "<span class=\"font-80em\" style=\"white-space:nowrap\">Email <span class=\"required\">*</span></span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(3, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(3, 0, "150px");

        emailPanel.add(email, "email_input_box");
        inputTable.setWidget(3, 1, emailPanel);

        // institution
        inputTable.setHTML(4, 0,
            "<span class=\"font-80em\" style=\"white-space:nowrap\">Institution</span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(4, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(4, 0, "150px");
        inputTable.setWidget(4, 1, institution);

        // about yourself
        inputTable.setHTML(5, 0,
            "<span class=\"font-80em\" style=\"white-space:nowrap\">About yourself</span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(5, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(5, 0, "150px");
        inputTable.setWidget(5, 1, aboutYourself);

        HTMLPanel buttonPanel = new HTMLPanel(
                "<span id=\"submit_button\"></span> <span id=\"registration_cancel_link\"></span>");
        buttonPanel.add(submit, "submit_button");
        buttonPanel.add(cancel, "registration_cancel_link");

        inputTable.getFlexCellFormatter().setColSpan(6, 0, 2);
        inputTable.setWidget(6, 0, buttonPanel);
        inputTable.getCellFormatter().setHorizontalAlignment(6, 0, HasAlignment.ALIGN_CENTER);

        return inputTable;
    }

    protected TextBox createStandardTextBox(String width) {
        final TextBox box = new TextBox();
        box.setStyleName("input_box");
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
        details.setInitials(this.initials.getText());
        return details;
    }

    public void showAlreadyRegisteredEmailAlert() {
        email.setStyleName("input_box_error");
        emailPanel.add(alreadyRegistered, "email_error_msg");
    }
}

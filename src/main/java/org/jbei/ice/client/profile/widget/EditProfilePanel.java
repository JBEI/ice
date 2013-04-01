package org.jbei.ice.client.profile.widget;

import org.jbei.ice.client.login.RegistrationDetails;

import com.google.gwt.event.dom.client.ClickEvent;
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
 * Panel for editing user profile
 *
 * @author Hector Plahar
 */
public class EditProfilePanel extends Composite {

    private Button submit;
    private Label cancel;
    private TextBox givenName;
    private TextBox familyName;
    private TextBox initials;
    private TextBox email;
    private TextArea aboutYourself;
    private TextBox institution;
    private final RegistrationDetails details;
    private FlexTable inputTable;
    private HandlerRegistration submitRegistration;
    private HandlerRegistration cancelRegistration;

    public EditProfilePanel(RegistrationDetails details) {
        this.details = details;
        initComponents();
        createInputTable();
        initWidget(inputTable);
    }

    private void initComponents() {
        inputTable = new FlexTable();
        inputTable.setStyleName("margin-top-20");

        submit = new Button("Submit");
        cancel = new Label("Cancel");
        cancel.setStyleName("footer_feedback_widget");
        cancel.addStyleName("font-75em");
        cancel.addStyleName("display-inline");

        givenName = createStandardTextBox("205px");
        givenName.setText(details.getFirstName());
        familyName = createStandardTextBox("205px");
        familyName.setText(details.getLastName());
        initials = createStandardTextBox("50px");
        initials.setText(details.getInitials());
        email = createStandardTextBox("205px");
        email.setText(details.getEmail());
        email.setEnabled(false);
        institution = createStandardTextBox("205px");
        institution.setText(details.getInstitution());
        aboutYourself = createTextArea("250px", "100px");
        aboutYourself.setText(details.getAbout());
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

    public RegistrationDetails getDetails() {
        details.setAbout(this.aboutYourself.getText());
        details.setEmail(this.email.getText());
        details.setFirstName(this.givenName.getText());
        details.setLastName(this.familyName.getText());
        details.setInstitution(this.institution.getText());
        details.setInitials(this.initials.getText());
        return details;
    }

    private Widget createInputTable() {

        // given name
        inputTable
                .setHTML(
                        0,
                        0,
                        "<span class=\"font-80em\" style=\"white-space:nowrap\">Given name <span " +
                                "class=\"required\">*</span></span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(0, 0, "150px");
        inputTable.setWidget(0, 1, givenName);

        // family name
        inputTable
                .setHTML(
                        1,
                        0,
                        "<span class=\"font-80em\" style=\"white-space:nowrap\">Family name <span " +
                                "class=\"required\">*</span></span>");
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
                        "<span class=\"font-80em\" style=\"white-space:nowrap\">Email <span " +
                                "class=\"required\">*</span></span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(3, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(3, 0, "150px");
        inputTable.setWidget(3, 1, email);

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

    public void addSubmitClickHandler(final ClickHandler handler) {
        if (submitRegistration != null)
            submitRegistration.removeHandler();
        submitRegistration = submit.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!validates())
                    return;

                handler.onClick(event);
            }
        });
    }

    public void addCancelHandler(ClickHandler handler) {
        if (cancelRegistration != null)
            cancelRegistration.removeHandler();
        cancelRegistration = cancel.addClickHandler(handler);
    }
}

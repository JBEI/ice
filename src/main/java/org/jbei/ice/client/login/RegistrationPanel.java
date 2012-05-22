package org.jbei.ice.client.login;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class RegistrationPanel extends Composite {

    private FlowPanel panel = new FlowPanel();
    private Button submit;
    private TextBox givenName;
    private TextBox familyName;
    private TextBox initials;
    private TextBox email;
    private TextArea aboutYourself;
    private TextBox institution;
    private HandlerRegistration registration;

    public RegistrationPanel() {

        initComponents();

        panel.setStyleName("login_panel");
        submit.setStyleName("login_btn");

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
    }

    public void addSubmitHandler(ClickHandler handler) {
        if (registration != null) {
            registration.removeHandler();
        }
        registration = submit.addClickHandler(handler);
    }

    private void initComponents() {
        submit = new Button("Submit");
        givenName = createStandardTextBox("205px");
        familyName = createStandardTextBox("205px");
        initials = createStandardTextBox("50px");
        email = createStandardTextBox("205px");
        institution = createStandardTextBox("205px");
        aboutYourself = createTextArea("250px", "100px");
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
                "<span id=\"cancel_link\"></span>&nbsp;<span id=\"submit_button\"></span>");
        buttonPanel.add(submit, "submit_button");

        inputTable.setHTML(6, 0, "&nbsp;");
        inputTable.setWidget(6, 1, buttonPanel);

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
}

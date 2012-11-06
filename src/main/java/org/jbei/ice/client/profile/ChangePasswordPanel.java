package org.jbei.ice.client.profile;

import org.jbei.ice.client.profile.ChangePasswordPresenter.IChangePasswordView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

public class ChangePasswordPanel extends Composite implements IChangePasswordView {

    private Button submit;
    private Label cancel;
    private PasswordTextBox password;
    private PasswordTextBox passwordConfirm;
    private FlexTable inputTable;
    private HandlerRegistration submitRegistration;
    private HandlerRegistration cancelRegistration;
    private final String email;
    private ChangePasswordPresenter presenter;
    private HTMLPanel passwordPanel;
    private HTMLPanel passwordConfirmPanel;
    private Label passwordError;
    private Label passwordConfirmError;

    public ChangePasswordPanel(String email) {
        this.email = email;
        initComponents();
        createInputTable();
        initWidget(inputTable);
        inputTable.setStyleName("margin-top-20");
        presenter = new ChangePasswordPresenter(this);
    }

    private void initComponents() {
        inputTable = new FlexTable();
        submit = new Button("Submit");
        submit.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getCharCode() != KeyCodes.KEY_ENTER)
                    return;

                submit.click();
            }
        });

        cancel = new Label("Cancel");
        cancel.setStyleName("footer_feedback_widget");
        cancel.addStyleName("font-75em");
        cancel.addStyleName("display-inline");

        passwordError = new Label("");
        passwordError.setStyleName("required");
        passwordError.addStyleName("font-70em");

        passwordConfirmError = new Label("");
        passwordConfirmError.setStyleName("required");
        passwordConfirmError.addStyleName("font-70em");

        password = createStandardTextBox("205px");
        passwordConfirm = createStandardTextBox("205px");
        passwordPanel = new HTMLPanel(
                "<span id=\"password_input_box\"></span> <span id=\"password_input_box_error_msg\"></span>");
        passwordConfirmPanel = new HTMLPanel(
                "<span id=\"password_confirm_input_box\"></span> <span " +
                        "id=\"password_confirm_input_box_error_msg\"></span>");
    }

    @Override
    public String getPassword() {
        return this.password.getText();
    }

    @Override
    public String getPasswordConfirm() {
        return this.passwordConfirm.getText();
    }

    public String getEmail() {
        return this.email;
    }

    @Override
    public void passwordError(String msg) {
        if (msg == null || msg.isEmpty()) {
            password.setStyleName("input_box");
            passwordError.setText("");
            return;
        }

        password.setStyleName("input_box_error");
        passwordError.setText(msg);
    }

    @Override
    public void passwordConfirmError(String msg) {
        if (msg == null || msg.isEmpty()) {
            passwordConfirm.setStyleName("input_box");
            passwordConfirmError.setText("");
            return;
        }

        passwordConfirm.setStyleName("input_box_error");
        passwordConfirmError.setText(msg);
    }

    protected PasswordTextBox createStandardTextBox(String width) {
        final PasswordTextBox box = new PasswordTextBox();
        box.setStyleName("input_box");
        box.setWidth(width);
        return box;
    }

    public void addSubmitClickHandler(final ClickHandler handler) {
        if (submitRegistration != null)
            submitRegistration.removeHandler();

        submitRegistration = submit.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!presenter.validates())
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

    private Widget createInputTable() {

        // password
        inputTable.setHTML(0, 0, "<span class=\"font-80em\" style=\"white-space:nowrap\"> "
                + "New Password <span class=\"required\">*</span></span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(0, 0, "150px");
        passwordPanel.add(password, "password_input_box");
        passwordPanel.add(passwordError, "password_input_box_error_msg");
        inputTable.setWidget(0, 1, passwordPanel);

        // password confirm
        inputTable.setHTML(1, 0, "<span class=\"font-80em\" style=\"white-space:nowrap\">"
                + "Confirm Password <span class=\"required\">*</span></span>");
        inputTable.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        inputTable.getFlexCellFormatter().setWidth(1, 0, "150px");
        passwordConfirmPanel.add(passwordConfirm, "password_confirm_input_box");
        passwordConfirmPanel.add(passwordConfirmError, "password_confirm_input_box_error_msg");
        inputTable.setWidget(1, 1, passwordConfirmPanel);

        HTMLPanel buttonPanel = new HTMLPanel(
                "<span id=\"submit_button\"></span> <span id=\"registration_cancel_link\"></span>");
        buttonPanel.add(submit, "submit_button");
        buttonPanel.add(cancel, "registration_cancel_link");

        inputTable.getFlexCellFormatter().setColSpan(6, 0, 2);
        inputTable.setWidget(6, 0, buttonPanel);
        inputTable.getCellFormatter().setHorizontalAlignment(6, 0, HasAlignment.ALIGN_CENTER);

        return inputTable;
    }
}

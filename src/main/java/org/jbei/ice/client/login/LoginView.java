package org.jbei.ice.client.login;

import org.jbei.ice.client.common.footer.Footer;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Hector Plahar
 */
public class LoginView extends Composite implements ILoginView {

    private Button submitButton;
    private TextBox loginInput;
    private Label loginErrorLabel;

    private TextBox passwordInput;
    private Label passwordErrorLabel;
    private CheckBox remember;
    private Label forgotPasswordLabel;
    private Label registerLabel;
    private Label rememberLabel;
    private Label passwordLabel;
    private FlexTable loginTable;

    private FlexTable inputTable;

    public LoginView() {
        FlexTable layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);

        initComponents();

        layout.setWidget(0, 0, registerLabel);
        layout.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_RIGHT);

        layout.setWidget(1, 0, createContents());
        layout.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        layout.getCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_CENTER);
        layout.getCellFormatter().setHeight(1, 0, "100%");

        layout.setWidget(2, 0, createFooter());
    }

    private void initComponents() {
        loginInput = new TextBox();
        loginInput.setStyleName("login_input");

        passwordInput = new PasswordTextBox();
        passwordInput.setStyleName("login_input");

        submitButton = new Button("Login");
        submitButton.setStyleName("login_btn");
        remember = new CheckBox();
        loginErrorLabel = new Label();
        loginErrorLabel.setStyleName("login_error_msg");
        loginErrorLabel.setVisible(false);
        passwordErrorLabel = new Label();
        passwordErrorLabel.setStyleName("login_error_msg");
        passwordErrorLabel.setVisible(false);

        forgotPasswordLabel = new Label("Forgot your password?");
        forgotPasswordLabel.setStyleName("footer_feedback_widget");
        forgotPasswordLabel.addStyleName("display-inline");
        forgotPasswordLabel.addStyleName("font-70em");
        forgotPasswordLabel.setVisible(false);

        // register
        registerLabel = new Label("Register");
        registerLabel.setStyleName("footer_feedback_widget");
        registerLabel.addStyleName("font-85em");
        registerLabel.addStyleName("pad-6");
        registerLabel.addStyleName("display-inline");
        registerLabel.setVisible(false);

        // remember me on this computer label
        rememberLabel = new Label("Remember me on this computer");
        rememberLabel.setStyleName("font-80em");
        rememberLabel.addStyleName("display-inline");

        // password Label
        passwordLabel = new Label("Password");
        passwordLabel.setStyleName("font-90em");
        passwordLabel.addStyleName("display-inline");

        // input table
        inputTable = new FlexTable();
        inputTable.setWidth("365px");
    }

    protected Widget createContents() {
        FlowPanel panel = new FlowPanel();
        panel.add(createLoginWidget());
        return panel;
    }

    private Widget createLoginWidget() {
        FlowPanel panel = new FlowPanel();
        panel.setStyleName("login_panel");
        loginTable = new FlexTable();
        loginTable.setStyleName("login_table");
        loginTable.setCellPadding(0);
        loginTable.setCellSpacing(0);
        loginTable.setHTML(1, 0, "<b>LOG IN</b>");
        loginTable.getCellFormatter().setStyleName(1, 0, "pad-15");

        loginTable.setHTML(2, 0, "<div style=\"height: 2px; background-color: #0082C0;"
                + "-webkit-box-shadow: 0px 1px 1px #999\"></div>"); // TODO : move it to styles

        HTMLPanel userInputPanel = new HTMLPanel(
                "<span class=\"font-90em\">Username</span><br><span id=\"user_login_input\"></span><div id=\"user_login_error_message\"></div>");
        userInputPanel.add(loginInput, "user_login_input");
        userInputPanel.add(loginErrorLabel, "user_login_error_message");

        HTMLPanel passwordInputPanel = new HTMLPanel(
                "<span><span id=\"password_label\"></span><span id=\"forgot_password_link\" style=\"float: right\"></span></span><br><span id=\"user_password_input\"></span><div id=\"user_password_error_message\"></div>");

        passwordInputPanel.add(passwordInput, "user_password_input");
        passwordInputPanel.add(passwordErrorLabel, "user_password_error_message");
        passwordInputPanel.add(forgotPasswordLabel, "forgot_password_link");
        passwordInputPanel.add(passwordLabel, "password_label");

        inputTable.setWidget(0, 0, userInputPanel);
        inputTable.setHTML(1, 0, "&nbsp;");
        inputTable.setWidget(2, 0, passwordInputPanel);

        loginTable.setWidget(3, 0, inputTable);
        loginTable.getFlexCellFormatter().setStyleName(3, 0, "pad-40");
        HTMLPanel submitPanel = new HTMLPanel(
                "<span id=\"login_button\"></span> <span id=\"remember_user_login_checkbox\"></span> <span id=\"remember_user_label\"></span>");
        submitPanel.add(submitButton, "login_button");
        submitPanel.add(remember, "remember_user_login_checkbox");
        submitPanel.add(rememberLabel, "remember_user_label");

        loginTable.setWidget(4, 0, submitPanel);
        loginTable.getFlexCellFormatter().setStyleName(4, 0, "pad-left-40");

        panel.add(loginTable);
        return panel;
    }

    protected Widget createFooter() {
        return Footer.getInstance();
    }

    @Override
    public void setSubmitKeyPressHandler(KeyPressHandler handler) {
        submitButton.addKeyPressHandler(handler);
        passwordInput.addKeyPressHandler(handler);
        loginInput.addKeyPressHandler(handler);
    }

    @Override
    public void setSubmitClickHandler(ClickHandler handler) {
        submitButton.addClickHandler(handler);
    }

    @Override
    public String getLoginName() {
        return loginInput.getText();
    }

    @Override
    public String getLoginPass() {
        return passwordInput.getText();
    }

    @Override
    public boolean rememberUserOnComputer() {
        return remember.getValue();
    }

    @Override
    public void setInputFieldsEnable(boolean enable) {
        this.passwordInput.setEnabled(enable);
        this.loginInput.setEnabled(enable);
        this.submitButton.setEnabled(enable);
    }

    @Override
    public void setLoginNameError(String errorMsg) {
        loginErrorLabel.setText(errorMsg);
        loginInput.addStyleName("login_input_error");
        loginErrorLabel.setVisible(true);
    }

    @Override
    public void setLoginPassError(String errorMsg) {
        passwordErrorLabel.setText(errorMsg);
        passwordInput.addStyleName("login_input_error");
        passwordErrorLabel.setVisible(true);
    }

    @Override
    public void clearErrorMessages() {
        loginErrorLabel.setVisible(false);
        passwordInput.removeStyleName("login_input_error");
        loginInput.removeStyleName("login_input_error");
        passwordErrorLabel.setVisible(false);
    }

    @Override
    public void addForgotPasswordHandler(ClickHandler forgotPasswordHandler) {
        forgotPasswordLabel.addClickHandler(forgotPasswordHandler);
        forgotPasswordLabel.setVisible(true);
    }

    @Override
    public void addRegisterHandler(ClickHandler handler) {
        registerLabel.addClickHandler(handler);
        registerLabel.setVisible(true);
    }

    public void switchToForgotPasswordMode() {
        submitButton.setText("Submit");
        remember.setVisible(false);
        rememberLabel.setVisible(false);

        inputTable.getFlexCellFormatter().setVisible(1, 0, false);
        inputTable.getFlexCellFormatter().setVisible(2, 0, false);
        loginTable.setHTML(1, 0, "<b>PASSWORD REMINDER</b>");
    }

    public void switchToRegisterMode() {
        loginTable.setHTML(1, 0, "<b>REGISTRATION</b>");
    }
}

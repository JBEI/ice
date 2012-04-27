package org.jbei.ice.client.login;

import org.jbei.ice.client.common.Footer;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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

    public LoginView() {
        FlexTable layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);

        initComponents();

        layout.setWidget(0, 0, getRegisterPasswordPanel());
        layout.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_RIGHT);

        layout.setWidget(1, 0, createContents());
        layout.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        layout.getCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_CENTER);
        layout.getCellFormatter().setHeight(1, 0, "100%");

        layout.setWidget(2, 0, createFooter());
    }

    private Widget getRegisterPasswordPanel() {
        return new HTML("&nbsp;");
        // TODO : use jbei settings to check if user can change password (PASSWORD_CHANGE_ALLOWED)
        //        HorizontalPanel panel = new HorizontalPanel();
        //        panel.setStyleName("font-85em");
        //        panel.setSpacing(10);
        //        panel.add(new Hyperlink("Forgot your password?", "foo"));
        //        panel.add(new HTML("<span style=\"color: #ccc\">|</span>"));
        //        panel.add(new Hyperlink("Register", "foo1"));
        //        return panel;
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
    }

    protected Widget createContents() {
        FlowPanel panel = new FlowPanel();
        panel.add(createLoginWidget());
        return panel;
    }

    private Widget createLoginWidget() {
        FlowPanel panel = new FlowPanel();
        panel.setStyleName("login_panel");
        FlexTable table = new FlexTable();
        table.setStyleName("login_table");
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setHTML(1, 0, "<b>LOG IN</b>");
        table.getCellFormatter().setStyleName(1, 0, "pad-15");
        table.setHTML(2, 0, "<div style=\"height: 2px; background-color: #0082C0;"
                + "-webkit-box-shadow: 0px 1px 1px #999\"></div>"); // TODO : move it to styles

        String html = "<span>Username</span><br><span id=\"user_login_input\"></span><div id=\"user_login_error_message\"></div>"
                + "<br><span>Password</span><br><span id=\"user_password_input\"></span><div id=\"user_password_error_message\"></div>";

        HTMLPanel htmlPanel = new HTMLPanel(html);
        htmlPanel.add(loginInput, "user_login_input");
        htmlPanel.add(passwordInput, "user_password_input");
        htmlPanel.add(loginErrorLabel, "user_login_error_message");
        htmlPanel.add(passwordErrorLabel, "user_password_error_message");

        table.setWidget(3, 0, htmlPanel);
        table.getFlexCellFormatter().setStyleName(3, 0, "pad-40");
        HTMLPanel submitPanel = new HTMLPanel(
                "<span id=\"login_button\"></span> <span id=\"remember_user_login_checkbox\"></span> <span class=\"font-80em\">Remember me on this computer</span>");
        submitPanel.add(submitButton, "login_button");
        submitPanel.add(remember, "remember_user_login_checkbox");
        table.setWidget(4, 0, submitPanel);
        table.getFlexCellFormatter().setStyleName(4, 0, "pad-left-40");

        panel.add(table);
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
}

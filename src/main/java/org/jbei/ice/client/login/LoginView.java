package org.jbei.ice.client.login;

import org.jbei.ice.client.common.footer.Footer;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;

/**
 * View for the Login page. Companion to the ${@link LoginPresenter}
 *
 * @author Hector Plahar
 */
public class LoginView extends Composite implements ILoginView {

    private Button submitButton;
    private TextBox loginInput;
    private TextBox passwordInput;
    private CheckBox remember;
    private Label forgotPasswordLabel;
    private Label registerLabel;
    private HTML loginMessagePanel;
    private FlexTable loginTable;
    private RegistrationPanel registrationPanel;
    private ForgotPasswordPanel forgotPasswordPanel;
    private FlexTable layout;
    private HandlerRegistration submitRegistration;

    public LoginView() {
        initComponents();
        initWidget(layout);

        layout.setWidget(0, 0, createLoginWidget());
        layout.getFlexCellFormatter().setHeight(0, 0, "100%");
        layout.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        layout.setWidget(1, 0, createFooter());

        ClickHandler cancelHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                switchToLoginMode();
            }
        };
        forgotPasswordPanel.setCancelHandler(cancelHandler);
        addKeyHandlerToClearErrorInput(loginInput);
        addKeyHandlerToClearErrorInput(passwordInput);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                loginInput.setFocus(true);
            }
        });
    }

    private void initComponents() {
        // layout
        layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);

        loginInput = new TextBox();
        loginInput.setStyleName("login_input");
        loginInput.getElement().setAttribute("placeHolder", "Username");

        forgotPasswordPanel = new ForgotPasswordPanel();

        passwordInput = new PasswordTextBox();
        passwordInput.setStyleName("login_input");
        passwordInput.getElement().setAttribute("placeHolder", "Password");

        submitButton = new Button("Sign in");
        submitButton.setStyleName("login_btn");

        remember = new CheckBox();
        forgotPasswordLabel = new Label("Forgot your password?");
        forgotPasswordLabel.setStyleName("footer_feedback_widget");
        forgotPasswordLabel.addStyleName("display-inline");
        forgotPasswordLabel.addStyleName("font-80em");
        forgotPasswordLabel.setVisible(false);

        // register
        registerLabel = new Label("Create account");
        registerLabel.setStyleName("footer_feedback_widget");
        registerLabel.addStyleName("font-80em");
        registerLabel.addStyleName("display-inline");
        registerLabel.setVisible(false);

        // login message panel
        loginMessagePanel = new HTML();
        loginMessagePanel.setStyleName("login_message_panel");
        loginMessagePanel.setHTML(
                "The Joint BioEnergy Institute (<a href=\"http://www.jbei.org\" target=\"_blank\">JBEI</a>) "
                        + "is a San Francisco Bay Area scientific partnership led by "
                        + "<a href=\"http://www.lbl.gov\" target=\"_blank\">Lawrence Berkeley National Laboratory</a> "
                        + "and including the <a href=\"http://www.sandia.gov\" target=\"_blank\">"
                        + "Sandia National Laboratories</a>, the "
                        + "<a href=\"http://www.universityofcalifornia.edu\" target=\"_blank\">University of "
                        + "California</a> campuses of Berkeley and Davis, the "
                        + "<a href=\"http://carnegiescience.edu\" target=\"_blank\">Carnegie Institution for "
                        + "Science</a>, <a href=\"http://www.llnl.gov\" target=\"_blank\">"
                        + "Lawrence Livermore National Laboratory</a>, and "
                        + "<a href=\"http://www.pnnl.gov\" target=\"_blank\">Pacific Northwest National Laboratory</a>."
                        + "<p>JBEI's primary scientific mission is to advance the development of the next generation "
                        + "of biofuels - drop-in liquid fuels derived from the solar energy stored in plant biomass. "
                        + "JBEI is one of three U.S. <a href=\"http://energy.gov\" target=\"_blank\">Department of "
                        + "Energy</a> Bioenergy Research Centers.");
    }

    private void addKeyHandlerToClearErrorInput(final TextBox box) {
        box.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (box.getText().isEmpty() || box.getText().length() > 1)
                    return;

                if (box.getStyleName().contains("login_input_error"))
                    box.removeStyleName("login_input_error");
            }
        });
    }

    private Widget createLoginWidget() {
        loginTable = new FlexTable();
        loginTable.setStyleName("login_layout");
        loginTable.setCellPadding(0);
        loginTable.setCellSpacing(0);

        loginTable.setWidget(0, 0, loginMessagePanel);
        loginTable.setHTML(0, 1, "&nbsp;");
        loginTable.getFlexCellFormatter().setWidth(0, 1, "50px");

        String html = "<br><img src=\"static/images/logo.png\" /><br><br><br>"
                + "<span id=\"user_login_input\"></span><br>"
                + "<span id=\"user_password_input\"></span><br>"
                + "<span id=\"remember_user_login_checkbox\"></span>"
                + "<span class=\"font-80em\">"
                + "Remember me on this computer</span><br>"
                + "<span id=\"login_button\"></span> &nbsp; <span id=\"create_new_account\"></span>"
                + "<div style=\"margin-top: 45px;\" id=\"forgot_password\"></div>";
        HTMLPanel htmlPanel = new HTMLPanel(html);
        htmlPanel.add(loginInput, "user_login_input");
        htmlPanel.add(passwordInput, "user_password_input");
        htmlPanel.add(remember, "remember_user_login_checkbox");
        htmlPanel.add(submitButton, "login_button");
        htmlPanel.add(registerLabel, "create_new_account");
        htmlPanel.add(forgotPasswordLabel, "forgot_password");
        loginTable.setWidget(0, 2, htmlPanel);

        return loginTable;
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
    public void setLoginHandler(ClickHandler handler) {
        if (submitRegistration != null)
            submitRegistration.removeHandler();

        submitRegistration = submitButton.addClickHandler(handler);
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
        loginInput.addStyleName("login_input_error");
        loginInput.getElement().setAttribute("placeHolder", errorMsg);
    }

    @Override
    public void setLoginPassError(String errorMsg) {
        passwordInput.addStyleName("login_input_error");
        passwordInput.setText("");
        passwordInput.getElement().setAttribute("placeHolder", errorMsg);
    }

    @Override
    public void clearErrorMessages() {
        passwordInput.removeStyleName("login_input_error");
        loginInput.removeStyleName("login_input_error");
        passwordInput.getElement().setAttribute("placeHolder", "Password");
        loginInput.getElement().setAttribute("placeHolder", "Username");
    }

    @Override
    public void addForgotPasswordLinkHandler(ClickHandler forgotPasswordHandler) {
        forgotPasswordLabel.addClickHandler(forgotPasswordHandler);
        forgotPasswordLabel.setVisible(true);
    }

    @Override
    public void addRegisterHandler(ClickHandler handler) {
        registerLabel.addClickHandler(handler);
        registerLabel.setVisible(true);
    }

    @Override
    public void switchToForgotPasswordMode() {
        forgotPasswordPanel.reset();
        loginTable.setWidget(0, 2, forgotPasswordPanel);
    }

    @Override
    public void switchToRegisterMode(ClickHandler submitHandler, ClickHandler cancelHandler) {
        registrationPanel = new RegistrationPanel();
        registrationPanel.getPresenter().addCancelHandler(cancelHandler);
        registrationPanel.getPresenter().addSubmitHandler(submitHandler);
        loginTable.setWidget(0, 2, registrationPanel);
    }

    @Override
    public void informOfDuplicateRegistrationEmail() {
        if (registrationPanel == null)
            return;
        registrationPanel.showAlreadyRegisteredEmailAlert();
    }

    @Override
    public void setResetPasswordHandler(ClickHandler handler) {
        forgotPasswordPanel.setSubmitClickHandler(handler);
    }

    @Override
    public void switchToLoginMode() {
        layout.setWidget(0, 0, createLoginWidget());
    }

    @Override
    public RegistrationDetails getRegistrationDetails() {
        return registrationPanel.getDetails();
    }

    @Override
    public String getForgotPasswordLogin() {
        return forgotPasswordPanel.getLogin();
    }
}

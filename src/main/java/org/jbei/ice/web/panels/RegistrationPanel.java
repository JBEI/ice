package org.jbei.ice.web.panels;

import java.util.Calendar;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.RegistrationSuccessfulPage;
import org.jbei.ice.web.pages.WelcomePage;

public class RegistrationPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public RegistrationPanel(String id) {
        super(id);

        class RegistrationForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;
            private String firstName;
            private String lastName;
            private String initials;
            private String email;
            private String password;
            private String confirmPassword;
            private String institution;
            private String description;

            public RegistrationForm(String id) {
                super(id);
                IceSession session = IceSession.get();
                if (session.isAuthenticated()) {
                    setResponsePage(WelcomePage.class);
                    return;
                }
                setModel(new CompoundPropertyModel<Object>(this));
                add(new TextField<String>("firstName").setRequired(true).setLabel(
                    new Model<String>("Given name")).add(
                    new StringValidator.MaximumLengthValidator(50)));
                add(new TextField<String>("lastName").setRequired(true).setLabel(
                    new Model<String>("Family name")).add(
                    new StringValidator.MaximumLengthValidator(50)));
                add(new TextField<String>("initials").setLabel(new Model<String>("Initials")).add(
                    new StringValidator.MaximumLengthValidator(10)));
                add(new TextField<String>("email").setRequired(true).setLabel(
                    new Model<String>("Email"))
                        .add(new StringValidator.MaximumLengthValidator(100)).add(
                            EmailAddressValidator.getInstance()));
                add(new PasswordTextField("password").setRequired(true).setLabel(
                    new Model<String>("Password")).add(
                    new StringValidator.MinimumLengthValidator(6)));
                add(new PasswordTextField("confirmPassword").setRequired(true).setLabel(
                    new Model<String>("Confirm"))
                        .add(new StringValidator.MinimumLengthValidator(6)));
                add(new TextField<String>("institution").setLabel(new Model<String>("Institution")));
                add(new TextArea<String>("description").setLabel(new Model<String>("Description")));
                add(new Button("submitButton", new Model<String>("Submit")));
                add(new FeedbackPanel("feedback"));
            }

            @Override
            protected void onSubmit() {
                if (!password.equals(confirmPassword)) {
                    error("Password and Confirm does not match.");
                } else {
                    try {
                        Account account = AccountController.getByEmail(email);
                        if (account != null) {
                            error("Account with this email address already registered");
                            return;
                        }
                        if (initials == null) {
                            initials = "";
                        }
                        if (institution == null) {
                            institution = "";
                        }
                        if (description == null) {
                            description = "";
                        }

                        account = new Account(firstName, lastName, initials, email,
                                AccountController.encryptPassword(password), institution,
                                description);
                        account.setIp("");
                        account.setIsSubscribed(1);
                        account.setCreationTime(Calendar.getInstance().getTime());
                        AccountController.save(account);
                        setResponsePage(RegistrationSuccessfulPage.class);
                        Emailer.send(email, "Account created successfully", "Dear " + firstName
                                + " " + lastName + ",\n\nThank you for creating "
                                + JbeirSettings.getSetting("PROJECT_NAME")
                                + " account.\n\nBest regards,\nRegistry Team");
                    } catch (ControllerException e) {
                        throw new ViewException(e);
                    } catch (Exception e) {
                        throw new ViewException(e);
                    }
                }
            }
        }
        add(new RegistrationForm("registrationForm"));
    }
}

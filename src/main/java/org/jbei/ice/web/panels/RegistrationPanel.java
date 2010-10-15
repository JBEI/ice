package org.jbei.ice.web.panels;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.pages.RegistrationSuccessfulPage;
import org.jbei.ice.web.pages.WelcomePage;

public class RegistrationPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public RegistrationPanel(String id, boolean isSelfEdit) {
        super(id);

        class RegistrationForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;
            private String firstName;
            private String lastName;
            private String initials;
            private String email;
            private String institution;
            private String description;

            public RegistrationForm(String id, boolean isSelfEdit) {
                super(id);
                IceSession session = IceSession.get();
                if (isSelfEdit && session.isAuthenticated()) {
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
                add(new TextField<String>("institution").setLabel(new Model<String>("Institution")));
                add(new TextArea<String>("description").setLabel(new Model<String>("Description")));
                add(new Button("submitButton", new Model<String>("Submit")));
                add(new FeedbackPanel("feedback"));
            }

            @Override
            protected void onSubmit() {
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

                    String newPassword = Utils.generateUUID().substring(24);
                    account = new Account(firstName, lastName, initials, email, AccountController
                            .encryptPassword(newPassword), institution, description);
                    account.setIp("");
                    account.setIsSubscribed(1);
                    account.setCreationTime(Calendar.getInstance().getTime());
                    AccountController.save(account);

                    CharSequence resetPasswordPage = WebRequestCycle.get()
                            .urlFor(ProfilePage.class,
                                new PageParameters("0=password,1=" + account.getEmail()));
                    WebRequestCycle webRequestCycle = (WebRequestCycle) WebRequestCycle.get();
                    HttpServletRequest httpServletRequest = webRequestCycle.getWebRequest()
                            .getHttpServletRequest();

                    String urlHeader = (httpServletRequest.isSecure()) ? "https://" : "http://";
                    urlHeader = urlHeader + httpServletRequest.getServerName() + ":"
                            + httpServletRequest.getLocalPort() + "/";
                    String resetPasswordPageUrl = urlHeader + resetPasswordPage;

                    String subject = "Account created successfully";

                    StringBuilder stringBuilder = new StringBuilder();
                    Formatter formatter = new Formatter(stringBuilder, Locale.US);

                    String body = "Dear %1$s, %n%n Thank you for creating a %2$s account. %nBy accessing "
                            + "this site with the password provided at the bottom "
                            + "you agree to the following terms:%n%n%3$s%n%nYour new password is: %4$s%n"
                            + "Please go to the following link and change your password:%n%n"
                            + resetPasswordPageUrl.toString();

                    String terms = "Biological Parts IP Disclaimer: \n\n"
                            + "The JBEI Registry of Biological Parts Software is licensed under a standard BSD\n"
                            + "license. Permission or license to use the biological parts registered in\n"
                            + "the JBEI Registry of Biological Parts is not included in the BSD license\n"
                            + "to use the JBEI Registry Software. Berkeley Lab and JBEI make no representation\n"
                            + "that the use of the biological parts registered in the JBEI Registry of\n"
                            + "Biological Parts will not infringe any patent or other proprietary right.";
                    formatter.format(body, email, JbeirSettings.getSetting("PROJECT_NAME"), terms,
                        newPassword);
                    Emailer.send(email, subject, stringBuilder.toString());
                    setResponsePage(RegistrationSuccessfulPage.class);
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (Exception e) {
                    throw new ViewException(e);
                }

            }
        }
        add(new RegistrationForm("registrationForm", isSelfEdit));
    }
}

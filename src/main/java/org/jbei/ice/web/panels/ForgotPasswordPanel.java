package org.jbei.ice.web.panels;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.ProfilePage;

public class ForgotPasswordPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public ForgotPasswordPanel(String id) {
        super(id);

        class ReminderForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;
            private String email;

            public String getEmail() {
                return email;
            }

            @SuppressWarnings("unused")
            public void setEmail(String email) {
                this.email = email;
            }

            public ReminderForm(String id) {
                super(id);
                setModel(new CompoundPropertyModel<Object>(this));

                add(new TextField<String>("email").setRequired(true).setLabel(
                    new Model<String>("Email")).add(EmailAddressValidator.getInstance()));
                add(new AjaxButton("submitButton", new Model<String>("Submit")) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onError(AjaxRequestTarget target, Form<?> form) {
                        Component temp = getParent().getParent().get("feedback");
                        target.addComponent(temp);
                    }

                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                        ForgotPasswordPanel thisPanel = (ForgotPasswordPanel) getParent()
                                .getParent();
                        Account account = null;

                        try {
                            account = AccountController.getByEmail(getEmail());
                        } catch (ControllerException e) {
                            throw new ViewException(e);
                        }

                        if (account != null) {
                            String newPassword = Utils.generateUUID().substring(24);
                            try {
                                account.setPassword(AccountController.encryptPassword(newPassword));
                                AccountController.save(account);
                            } catch (ControllerException e) {
                                throw new ViewException(e);
                            }

                            CharSequence resetPasswordPage = WebRequestCycle.get().urlFor(
                                ProfilePage.class,
                                new PageParameters("0=password,1=" + account.getEmail()));
                            WebRequestCycle webRequestCycle = (WebRequestCycle) WebRequestCycle
                                    .get();
                            HttpServletRequest httpServletRequest = webRequestCycle.getWebRequest()
                                    .getHttpServletRequest();

                            String urlHeader = (httpServletRequest.isSecure()) ? "https://"
                                    : "http://";
                            urlHeader = urlHeader + httpServletRequest.getServerName() + ":"
                                    + httpServletRequest.getLocalPort() + "/";
                            String resetPasswordPageUrl = urlHeader + resetPasswordPage;
                            String subject = "JBEIRegistry Password Reminder";
                            String body = "Someone (maybe you) have requested to reset your password.\n\n";
                            body = body + "Your new password is " + newPassword + ".\n\n";
                            body = body
                                    + "Please go to the following link and change your password.\n\n";
                            body = body + resetPasswordPageUrl.toString();

                            try {
                                Emailer.send(account.getEmail(), subject, body);
                            } catch (Exception e) {
                                throw new ViewException(e);
                            }

                            Panel responsePanel = new EmptyMessagePanel(thisPanel.getId(),
                                    "A new password has been emailed to you.");
                            thisPanel.getParent().replace(responsePanel.setOutputMarkupId(true));
                            target.addComponent(responsePanel);
                        } else {
                            error("Unknown user.");
                        }
                    }
                });
            }

            @Override
            protected void onSubmit() {
                // handled by ajax button
            }
        }

        Form<?> reminderForm = new ReminderForm("reminderForm");
        add(reminderForm.setOutputMarkupId(true));
        add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    }
}

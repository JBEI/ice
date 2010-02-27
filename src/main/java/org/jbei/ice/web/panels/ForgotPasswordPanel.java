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
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.pages.UpdatePasswordPage;

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
                            account = AccountManager.getByEmail(getEmail());
                        } catch (ManagerException e) {
                            String msg = "Manager exception on request new password from "
                                    + getEmail();
                            Logger.info(msg);
                            error("Unknown User Exception.");
                        }
                        if (account != null) {
                            String newPassword = Utils.generateUUID().substring(24);
                            account.setPassword(AccountManager.encryptPassword(newPassword));
                            try {
                                AccountManager.save(account);
                            } catch (ManagerException e) {
                                Logger.error("Could not save new password: " + e.toString(), e);
                                error("Could not generate new password");
                            }
                            CharSequence resetPasswordPage = WebRequestCycle.get().urlFor(
                                    UpdatePasswordPage.class, new PageParameters());
                            WebRequestCycle webRequestCycle = (WebRequestCycle) WebRequestCycle
                                    .get();
                            HttpServletRequest httpServletRequest = webRequestCycle.getWebRequest()
                                    .getHttpServletRequest();

                            //String temp2 = ((WebRequest) request).getHttpServletRequest()
                            //      .getRemoteAddr();
                            String urlHeader = (httpServletRequest.isSecure()) ? "https://"
                                    : "http://";
                            urlHeader = urlHeader + httpServletRequest.getRemoteHost() + ":"
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
                                //Emailer.send(getEmail(), subject, body);
                            } catch (Exception e) {
                                e.printStackTrace();
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

package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.web.IceSession;

public class FeedbackPage extends UnprotectedPage {
    public FeedbackPage(PageParameters parameters) {
        super(parameters);

        class FeedbackForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;

            private String email;
            private String subject;
            private String message;

            public FeedbackForm(String id) {
                super(id);

                setModel(new CompoundPropertyModel<Object>(this));

                IceSession session = IceSession.get();

                if (session.isAuthenticated()) {
                    email = session.getAccount().getEmail();
                }

                add(new TextField<String>("email").setRequired(true).setLabel(
                        new Model<String>("Email")).add(
                        new StringValidator.MaximumLengthValidator(100)).add(
                        EmailAddressValidator.getInstance()));
                add(new TextField<String>("subject").setRequired(true).setLabel(
                        new Model<String>("Subject")));
                add(new TextArea<String>("message").setLabel(new Model<String>("Message")));

                add(new Button("submitButton", new Model<String>("Submit")));

                add(new FeedbackPanel("feedback"));
            }

            @Override
            protected void onSubmit() {
                Emailer.send(email, JbeirSettings.getSetting("PROJECT_NAME"),
                        "Thank you for sending your feedback.\n\nBest regards,\nRegistry Team");

                Emailer.send(JbeirSettings.getSetting("ADMIN_EMAIL"), subject, message);
                if (!JbeirSettings.getSetting("ADMIN_EMAIL").equals(
                        JbeirSettings.getSetting("MODERATOR_EMAIL"))) {
                    Emailer.send(JbeirSettings.getSetting("MODERATOR_EMAIL"), subject, message);
                }

                setResponsePage(FeedbackSuccessfulPage.class);
            }
        }

        add(new FeedbackForm("feedbackForm"));
    }

    @Override
    protected String getTitle() {
        return "Feedback - " + super.getTitle();
    }
}

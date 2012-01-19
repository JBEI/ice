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
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class ReportProblemPage extends ProtectedPage {
    public ReportProblemPage(PageParameters parameters) {
        super(parameters);

        add(new ReportProblemForm("reportProblemForm", parameters));
    }

    class ReportProblemForm extends StatelessForm<Object> {
        private static final long serialVersionUID = 1L;

        private String email = "";
        private String subject = "";
        private String message = "";
        private long id = 0;

        public ReportProblemForm(String id, PageParameters parameters) {
            super(id);

            try {
                this.id = Long.parseLong(parameters.getString("0"));

            } catch (NumberFormatException e) {
                throw new ViewException("Could not parse id", e);
            }

            String idString = String.valueOf(this.id);
            String partNumber = "";
            try {
                partNumber = EntryManager.get(this.id).getOnePartNumber().getPartNumber();
            } catch (ManagerException e) {
                // No worries. Just pass.
            }
            idString = partNumber + " (" + idString + ")";
            subject = JbeirSettings.getSetting("ERROR_EMAIL_EXCEPTION_PREFIX")
                    + " Problem reported with entry " + idString + " by " + email;

            message = "* Problem with this Entry?\n* Non viable? \n* Missing from collection? \n* Missing info?\n* Missing plasmid?\n* Other?";
            initializeControls(parameters);
        }

        @Override
        protected void onSubmit() {
            Emailer.send(JbeirSettings.getSetting("ADMIN_EMAIL"), subject, message);
            if (!JbeirSettings.getSetting("ADMIN_EMAIL").equals(
                JbeirSettings.getSetting("MODERATOR_EMAIL"))) {
                Emailer.send(JbeirSettings.getSetting("MODERATOR_EMAIL"), subject, message);
            }
            setResponsePage(EntryViewPage.class, new PageParameters("0=" + id));
        }

        private void initializeControls(PageParameters parameters) {
            if (id == 0) {
                throw new ViewException("Did not receive Id");
            }
            setModel(new CompoundPropertyModel<Object>(this));

            IceSession session = IceSession.get();

            if (session.isAuthenticated()) {
                email = session.getAccount().getEmail();
            }

            add(new TextField<String>("email").setRequired(true)
                    .setLabel(new Model<String>("Email"))
                    .add(new StringValidator.MaximumLengthValidator(100))
                    .add(EmailAddressValidator.getInstance()).setEnabled(false));
            add(new TextField<String>("subject").setLabel(new Model<String>("Subject")).setEnabled(
                false));
            add(new TextArea<String>("message").setLabel(new Model<String>("Message")));

            add(new Button("submitButton", new Model<String>("Submit")));

            add(new FeedbackPanel("reportProblem"));
        }
    }
}

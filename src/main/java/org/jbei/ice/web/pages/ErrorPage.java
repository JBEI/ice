package org.jbei.ice.web.pages;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

public class ErrorPage extends WebPage {
    protected static final long serialVersionUID = 1L;

    public ErrorPage(Throwable throwable) {
        super();

        add(new StyleSheetReference("stylesheet", ErrorPage.class, "main.css"));

        add(new Label("title", "System Internal Error"));

        add(new BookmarkablePageLink<FeedbackPage>("feedbackLink", FeedbackPage.class));
        add(new ExternalLink("emailLink", "mailto:" + JbeirSettings.getSetting("ADMIN_EMAIL"))
                .add(new Label("emailLabel", JbeirSettings.getSetting("ADMIN_EMAIL"))));

        add(new Label("message", new Model<String>(throwable.getMessage())));
        add(new Label("traceback", new Model<String>(Utils.stackTraceToString(throwable))));

        if (JbeirSettings.getSetting("SEND_EMAIL_ON_ERRORS").equals("YES")) {
            sendEmail(throwable);
        }
    }

    private void sendEmail(Throwable throwable) {
        if (throwable != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String body = "System Time: " + dateFormatter.format((new Date())) + "\n\n";
            body = body + Utils.stackTraceToString(throwable);
            String subject = (throwable.getMessage().length() > 50) ? (throwable.getMessage()
                    .substring(0, 50) + "...") : throwable.getMessage();

            Emailer.error(JbeirSettings.getSetting("ERROR_EMAIL_EXCEPTION_PREFIX") + " " + subject,
                    body);

            Logger.error(throwable.getMessage(), throwable);
            Logger.error(body, throwable);
        }
    }
}

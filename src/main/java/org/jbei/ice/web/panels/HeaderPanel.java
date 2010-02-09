package org.jbei.ice.web.panels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.pages.WelcomePage;

public class HeaderPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public HeaderPanel(String id) {
        super(id);

        add(new BookmarkablePageLink<String>("homeLink", WelcomePage.class).add(new Image(
                "logoImage", new ResourceReference(UnprotectedPage.class,
                        UnprotectedPage.IMAGES_RESOURCE_LOCATION + "logo.gif"))));
        add(new LoginStatusPanel("loginStatusPanel"));
        add(new Label("numberOfPartsLabel", new Model<String>(String.valueOf(EntryManager
                .getNumberOfEntries()))));
        DateFormat dateFormat = new SimpleDateFormat("EEEE, MMMMM d, yyyy");
        add(new Label("dateLabel", new Model<String>(dateFormat.format(new Date()))));
    }
}

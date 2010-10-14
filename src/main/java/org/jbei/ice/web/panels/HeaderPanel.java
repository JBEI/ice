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
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.pages.WelcomePage;

public class HeaderPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public HeaderPanel(String id) {
        super(id);

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        add(new BookmarkablePageLink<String>("homeLink", WelcomePage.class).add(new Image(
                "logoImage", new ResourceReference(UnprotectedPage.class,
                        UnprotectedPage.IMAGES_RESOURCE_LOCATION + "logo.gif"))));
        add(new LoginStatusPanel("loginStatusPanel"));

        long numberOfEntries = 0;

        try {
            numberOfEntries = entryController.getNumberOfVisibleEntries();
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        add(new Label("numberOfPartsLabel", new Model<String>(String.valueOf(numberOfEntries))));
        DateFormat dateFormat = new SimpleDateFormat("EEEE, MMMMM d, yyyy");
        add(new Label("dateLabel", new Model<String>(dateFormat.format(new Date()))));
    }
}

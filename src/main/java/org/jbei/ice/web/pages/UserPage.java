package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.panels.UserEntriesViewPanel;
import org.jbei.ice.web.panels.UserSamplesViewPanel;

public class UserPage extends ProtectedPage {
    public Component currentPanel;
    public Component entriesPanel;
    public Component samplesPanel;

    public BookmarkablePageLink<Object> entriesLink;
    public BookmarkablePageLink<Object> samplesLink;

    public String currentPage = null;

    public UserPage(PageParameters parameters) {
        super(parameters);

        currentPage = parameters.getString("0");

        entriesLink = new BookmarkablePageLink<Object>("entriesLink", UserPage.class,
                new PageParameters("0=entries"));
        entriesLink.setOutputMarkupId(true);
        samplesLink = new BookmarkablePageLink<Object>("samplesLink", UserPage.class,
                new PageParameters("0=samples"));
        samplesLink.setOutputMarkupId(true);

        updateTab();

        add(entriesLink);
        add(samplesLink);

        if (currentPage != null && currentPage.equals("samples")) {
            currentPanel = createSamplesPanel();
        } else {
            currentPanel = createEntriesPanel();
        }

        add(currentPanel);
    }

    private void updateTab() {
        entriesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        samplesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);

        if (currentPage != null && currentPage.equals("samples")) {
            samplesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        } else {
            entriesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        }
    }

    private Panel createEntriesPanel() {
        UserEntriesViewPanel userEntriesViewPanel = new UserEntriesViewPanel("centerPanel");

        userEntriesViewPanel.setOutputMarkupId(true);

        return userEntriesViewPanel;
    }

    private Panel createSamplesPanel() {
        UserSamplesViewPanel userSamplesViewPanel = new UserSamplesViewPanel("centerPanel");

        userSamplesViewPanel.setOutputMarkupId(true);

        return userSamplesViewPanel;
    }

    @Override
    protected String getTitle() {
        return "My Entries - " + super.getTitle();
    }
}

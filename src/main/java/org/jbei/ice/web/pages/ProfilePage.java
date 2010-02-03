package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.panels.ProfileAboutUserPanel;
import org.jbei.ice.web.panels.ProfileEntriesPanel;
import org.jbei.ice.web.panels.ProfileSamplesPanel;

public class ProfilePage extends ProtectedPage {
    private static final long serialVersionUID = 1L;

    public Component currentPanel;
    public Component aboutPanel;
    public Component entriesPanel;
    public Component samplesPanel;

    public BookmarkablePageLink<Object> aboutLink;
    public BookmarkablePageLink<Object> entriesLink;
    public BookmarkablePageLink<Object> samplesLink;

    public String currentPage = null;
    public String accountEmail = null;

    public ProfilePage(PageParameters parameters) {
        super(parameters);

        currentPage = parameters.getString("0");
        accountEmail = parameters.getString("1");

        aboutLink = new BookmarkablePageLink<Object>("aboutLink", ProfilePage.class,
                new PageParameters("0=about,1=" + accountEmail));
        aboutLink.setOutputMarkupId(true);
        entriesLink = new BookmarkablePageLink<Object>("entriesLink", ProfilePage.class,
                new PageParameters("0=entries,1=" + accountEmail));
        entriesLink.setOutputMarkupId(true);
        samplesLink = new BookmarkablePageLink<Object>("samplesLink", ProfilePage.class,
                new PageParameters("0=samples,1=" + accountEmail));
        samplesLink.setOutputMarkupId(true);

        updateTab();

        add(aboutLink);
        add(entriesLink);
        add(samplesLink);

        if (currentPage != null && currentPage.equals("samples")) {
            currentPanel = createSamplesPanel();
        } else if (currentPage != null && currentPage.equals("entries")) {
            currentPanel = createEntriesPanel();
        } else {
            currentPanel = createAboutPanel();
        }

        add(currentPanel);
    }

    private void updateTab() {
        aboutLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        entriesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        samplesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);

        if (currentPage != null && currentPage.equals("samples")) {
            samplesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        } else if (currentPage != null && currentPage.equals("entries")) {
            entriesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        } else {
            aboutLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        }
    }

    private Panel createAboutPanel() {
        ProfileAboutUserPanel profileAboutUserPanel = new ProfileAboutUserPanel("centerPanel",
                accountEmail);

        profileAboutUserPanel.setOutputMarkupId(true);

        return profileAboutUserPanel;
    }

    private Panel createEntriesPanel() {
        ProfileEntriesPanel profileEntriesPanel = new ProfileEntriesPanel("centerPanel",
                accountEmail);

        profileEntriesPanel.setOutputMarkupId(true);

        return profileEntriesPanel;
    }

    private Panel createSamplesPanel() {
        ProfileSamplesPanel profileSamplesPanel = new ProfileSamplesPanel("centerPanel",
                accountEmail);

        profileSamplesPanel.setOutputMarkupId(true);

        return profileSamplesPanel;
    }
}

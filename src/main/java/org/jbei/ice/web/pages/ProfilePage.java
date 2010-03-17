package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.panels.EmptyMessagePanel;
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
    private Account account;

    public ProfilePage(PageParameters parameters) {
        super(parameters);

        initialize(parameters);
    }

    private void initialize(PageParameters parameters) {
        if (parameters == null || parameters.size() < 2) {
            throw new ViewException("Parameters are missing!");
        }

        currentPage = parameters.getString("0");
        accountEmail = parameters.getString("1");

        try {
            account = AccountController.getByEmail(accountEmail);
        } catch (ControllerException e) {
            account = null;
        }

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

        add(new Label("username", "Profile for "
                + (account == null ? accountEmail : account.getFullName())));

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
        Panel profileAboutUserPanel;

        if (account == null) {
            profileAboutUserPanel = new EmptyMessagePanel("centerPanel",
                    "Account is not registered.");
        } else {
            profileAboutUserPanel = new ProfileAboutUserPanel("centerPanel", accountEmail);
        }

        profileAboutUserPanel.setOutputMarkupId(true);

        return profileAboutUserPanel;
    }

    private Panel createEntriesPanel() {
        Panel profileEntriesPanel;

        if (account == null) {
            profileEntriesPanel = new EmptyMessagePanel("centerPanel",
                    "Couldn't lookup entries for account. Account isn't registered.");
        } else {
            profileEntriesPanel = new ProfileEntriesPanel("centerPanel", account);
        }

        profileEntriesPanel.setOutputMarkupId(true);

        return profileEntriesPanel;
    }

    private Panel createSamplesPanel() {
        Panel profileSamplesPanel;

        if (account == null) {
            profileSamplesPanel = new EmptyMessagePanel("centerPanel",
                    "Couldn't lookup samples for account. Account isn't registered.");
        } else {
            profileSamplesPanel = new ProfileSamplesPanel("centerPanel", accountEmail);
        }

        profileSamplesPanel.setOutputMarkupId(true);

        return profileSamplesPanel;
    }

    @Override
    protected String getTitle() {
        return "Profile - " + super.getTitle();
    }
}

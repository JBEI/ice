package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.panels.MostRecentEntriesPanel;

public class EntriesPage extends ProtectedPage {
    public Component currentPanel;
    public Component recentEntriesPanel;

    public BookmarkablePageLink<Object> recentEntriesLink;

    private int perPage = 15;

    public EntriesPage(PageParameters parameters) {
        super(parameters);

        perPage = (parameters.size() > 1) ? parameters.getInt("1") : perPage;

        recentEntriesLink = new BookmarkablePageLink<Object>("recentEntriesLink",
                EntriesPage.class, new PageParameters("0=recent"));
        recentEntriesLink.setOutputMarkupId(true);

        updateTab();

        add(recentEntriesLink);

        currentPanel = createRecentEntriesPanel();

        add(currentPanel);
    }

    private void updateTab() {
        recentEntriesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(
                true);
    }

    private Panel createRecentEntriesPanel() {
        MostRecentEntriesPanel mostRecentEntriesPanel = new MostRecentEntriesPanel("centerPanel",
                perPage);

        mostRecentEntriesPanel.setOutputMarkupId(true);

        return mostRecentEntriesPanel;
    }
}

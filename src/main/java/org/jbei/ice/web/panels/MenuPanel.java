package org.jbei.ice.web.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.pages.EntriesPage;
import org.jbei.ice.web.pages.EntryNewPage;
import org.jbei.ice.web.pages.UserPage;

public class MenuPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public class MenuItem implements Serializable {
        private static final long serialVersionUID = 1L;

        protected BookmarkablePageLink<WebPage> pageLink = null;
        protected Label label = null;

        public MenuItem(BookmarkablePageLink<WebPage> pageLink, Label label) {
            this.pageLink = pageLink;
            this.label = label;
        }

        public BookmarkablePageLink<WebPage> getPageLink() {
            return this.pageLink;
        }

        public Label getLabel() {
            return this.label;
        }
    }

    private List<MenuItem> menuItems = new ArrayList<MenuItem>();

    public MenuPanel(String id) {
        super(id);

        addPageLinkMenuItem(UserPage.class, "My Entries");
        addPageLinkMenuItem(EntriesPage.class, "All Entries");
        addPageLinkMenuItem(EntryNewPage.class, "Add new entry");

        ListView<MenuItem> menuList = new ListView<MenuItem>("menuList", menuItems) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<MenuItem> item) {
                MenuItem menuItem = item.getModelObject();
                BookmarkablePageLink<WebPage> link = menuItem.getPageLink();
                link.add(menuItem.getLabel());
                item.add(link);
            }
        };

        add(menuList);
    }

    @SuppressWarnings("unchecked")
    public void addPageLinkMenuItem(Class webPage, String label) {
        MenuItem menuItem = new MenuItem(new BookmarkablePageLink<WebPage>("menuItem", webPage),
                new Label("label", label));
        menuItems.add(menuItem);
    }
}

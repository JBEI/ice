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
import org.jbei.ice.web.pages.FeedbackPage;
import org.jbei.ice.web.pages.HomePage;
import org.jbei.ice.web.pages.LogOutPage;
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

        addPageLinkMenuItem(HomePage.class, "Home");
        addPageLinkMenuItem(HomePage.class, "News");
        addPageLinkMenuItem(EntriesPage.class, "Entries");
        addPageLinkMenuItem(UserPage.class, "My Entries");
        addPageLinkMenuItem(EntryNewPage.class, "Add new entry");
        addPageLinkMenuItem(FeedbackPage.class, "Feedback");
        addPageLinkMenuItem(LogOutPage.class, "Log Out");

        ListView<MenuItem> menuList = new ListView<MenuItem>("menuList", menuItems) {
            private static final long serialVersionUID = 1L;

            protected void populateItem(ListItem<MenuItem> item) {
                MenuItem menuItem = (MenuItem) item.getModelObject();
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

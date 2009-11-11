package org.jbei.ice.web.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.HomePage;
import org.jbei.ice.web.LoginPage;
import org.jbei.ice.web.forms.EntryFormPage;
import org.jbei.ice.web.forms.EntryFormPanel;
import org.jbei.ice.web.forms.PlasmidForm;

public class MenuPanel extends Panel {
	
	public class MenuItem implements Serializable {
		
		protected BookmarkablePageLink pageLink = null;
		protected Label label = null;
		
		public MenuItem() {
			
		}
		public MenuItem(BookmarkablePageLink pageLink, Label label) {
			this.pageLink = pageLink;
			this.label = label;
		}
		
		public BookmarkablePageLink getPageLink() {
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
		addPageLinkMenuItem(LoginPage.class, "Login");
		addPageLinkMenuItem(HomePage.class, "Home");
		addPageLinkMenuItem(EntryFormPage.class, "Entry Form");
		addPageLinkMenuItem(EntriesTablePage.class, "List View");
		
		ListView menuList = new ListView ("menuList", menuItems) {
			protected void populateItem(ListItem item) {
				
				MenuItem menuItem = (MenuItem) item.getModelObject();
				BookmarkablePageLink link = menuItem.getPageLink();
				link.add(menuItem.getLabel());
				item.add(link);
			}
		};
		add(menuList);
	
	}
	
	private void addPanelLinkMenuItem(Class<EntryFormPanel> c, String label) {
		//MenuItem menuItem = new MenuItem(stuff, new Label("label", label));
		
	}

	public void addPageLinkMenuItem(Class c, String label) {
		MenuItem menuItem = new MenuItem(new BookmarkablePageLink("menuItem", c), 
				new Label("label", label));
		menuItems.add(menuItem);
		
	}

}

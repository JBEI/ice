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
import org.jbei.ice.web.forms.EntryFormPage;
import org.jbei.ice.web.forms.EntryFormPanel;
import org.jbei.ice.web.forms.PlasmidForm;
import org.jbei.ice.web.pages.EntriesTablePage;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.pages.LoginPage;
import org.jbei.ice.web.pages.PlasmidPage;
import org.jbei.ice.web.pages.StrainPage;
import org.jbei.ice.web.pages.UserEntryPage;
import org.jbei.ice.web.pages.WorkSpacePage;

public class MenuPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	public class MenuItem implements Serializable {

		private static final long serialVersionUID = 1L;	
		@SuppressWarnings("unchecked")
		protected BookmarkablePageLink pageLink = null;
		protected Label label = null;
		
		public MenuItem() {
			
		}
		
		@SuppressWarnings("unchecked")
		public MenuItem(BookmarkablePageLink pageLink, Label label) {
			this.pageLink = pageLink;
			this.label = label;
		}
		
		@SuppressWarnings("unchecked")
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
		addPageLinkMenuItem(UnprotectedPage.class, "Home");
		addPageLinkMenuItem(LoginPage.class, "Login");
		addPageLinkMenuItem(UserEntryPage.class, "My Entries");
		//addPageLinkMenuItem(WorkSpacePage.class, "WorkSpace");
		addPageLinkMenuItem(EntryFormPage.class, "Entry Form");
		addPageLinkMenuItem(EntriesTablePage.class, "List View");
		addPageLinkMenuItem(PlasmidPage.class, "Plasmid Page");
		addPageLinkMenuItem(StrainPage.class, "Strain Page");
		
		
		
		@SuppressWarnings("unchecked")
		ListView menuList = new ListView ("menuList", menuItems) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem item) {
				
				MenuItem menuItem = (MenuItem) item.getModelObject();
				BookmarkablePageLink link = menuItem.getPageLink();
				link.add(menuItem.getLabel());
				item.add(link);
			}
		};
		add(menuList);
	
	}
	
	/*
	private void addPanelLinkMenuItem(Class<EntryFormPanel> c, String label) {
		//MenuItem menuItem = new MenuItem(stuff, new Label("label", label));
		
	}
	 */
	
	@SuppressWarnings("unchecked")
	public void addPageLinkMenuItem(Class c, String label) {
		MenuItem menuItem = new MenuItem(new BookmarkablePageLink("menuItem", c), 
				new Label("label", label));
		menuItems.add(menuItem);
		
	}

}

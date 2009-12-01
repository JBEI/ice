package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.web.pages.PlasmidPage;

public class SearchBarFormPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	
	public SearchBarFormPanel(String id) {
		super(id);

		class SearchBarForm extends StatelessForm<Object> {

			private static final long serialVersionUID = 1L;
			private String searchQuery;
			
			public SearchBarForm(String id) {
				super(id);
				setModel(new CompoundPropertyModel<Object>(this));
				add(new TextField<String>("searchQuery"));
			}
			
			//overridden methods
			@Override
			protected void onSubmit() {
				System.out.println(getSearchQuery());
			}
			
			//setters and getters
			@SuppressWarnings("unused")
			public void setSearchQuery(String searchQuery) {
				this.searchQuery = searchQuery;
			}
			public String getSearchQuery() {
				return searchQuery;
			}

		}
		
		Form<?> searchBarForm = new SearchBarForm("searchBarForm");
		//TODO advanced search
		searchBarForm.	add(new BookmarkablePageLink("advancedSearchLink", PlasmidPage.class));
		//TODO blast search
		searchBarForm.	add(new BookmarkablePageLink("blastSearchLink", PlasmidPage.class));
		searchBarForm.add(new Button("submitButton", new Model<String>("Search")) {
			private static final long serialVersionUID = 1L; 
			});
		add(searchBarForm);
	}
	




}

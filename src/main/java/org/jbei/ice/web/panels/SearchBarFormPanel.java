package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.Search;
import org.jbei.ice.lib.search.SearchResult;
import org.jbei.ice.web.pages.UserEntryPage;

public class SearchBarFormPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	
	@SuppressWarnings("unchecked")
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
				//submit handled by ajax button
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
		searchBarForm.	add(new BookmarkablePageLink("advancedSearchLink", UserEntryPage.class));
		//TODO blast search
		searchBarForm.	add(new BookmarkablePageLink("blastSearchLink", UserEntryPage.class));
		AjaxButton ajaxButton = new AjaxButton("submitButton", new Model<String>("Search"), searchBarForm) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				Logger.info("Search query: " + ((SearchBarForm)form).getSearchQuery());
				ArrayList<SearchResult> searchResults = null;
				Panel searchResultPanel = null;
				try {
					searchResults = Search.getInstance().query(((SearchBarForm)form).getSearchQuery());
					if (searchResults.size() == 0) {
						searchResultPanel = new EmptyMessagePanel("workSpacePanel", "No results found");
					} else {
						searchResultPanel = new SearchResultPanel("workSpacePanel", searchResults, 10);
					}

					searchResultPanel.setOutputMarkupId(true);
					form.getPage().replace(searchResultPanel);
					target.addComponent(searchResultPanel);
					
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
			
		};
		searchBarForm.add(ajaxButton);
		add(searchBarForm);
	}
	




}

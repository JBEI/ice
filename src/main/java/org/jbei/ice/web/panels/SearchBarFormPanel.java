package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.jbei.ice.web.pages.SearchResultPage;
import org.jbei.ice.web.pages.UserEntryPage;

public class SearchBarFormPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public SearchBarFormPanel(String id) {
		this(id, "");
	}
	
	@SuppressWarnings("unchecked")
	public SearchBarFormPanel(String id, String queryString) {
		super(id);
		
		class SearchBarForm extends StatelessForm<Object> {
						
			private static final long serialVersionUID = 1L;
			private String searchQuery;
			
			public SearchBarForm(String id, String formQueryString) {
				super(id);
				setSearchQuery(formQueryString);
				setModel(new CompoundPropertyModel<Object>(this));
				add(new TextField<String>("searchQuery"));
			}

			@Override
			protected void onSubmit() {
				setRedirect(true);
				setResponsePage(SearchResultPage.class, new PageParameters(
						"search=" + getSearchQuery()));

				System.out.println("submmited: " + getSearchQuery());
			}

			public void setSearchQuery(String searchQuery) {
				this.searchQuery = searchQuery;
			}

			public String getSearchQuery() {
				return searchQuery;
			}
		}

		Form<?> searchBarForm = new SearchBarForm("searchBarForm", queryString);
		// TODO advanced search
		searchBarForm.add(new BookmarkablePageLink("advancedSearchLink",
				UserEntryPage.class));
		// TODO blast search
		searchBarForm.add(new BookmarkablePageLink("blastSearchLink",
				UserEntryPage.class));
		searchBarForm.add(new Button("submitButton"));

		add(searchBarForm);
	}

	
}

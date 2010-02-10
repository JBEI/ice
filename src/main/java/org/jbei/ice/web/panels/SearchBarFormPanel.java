package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.web.pages.BlastPage;
import org.jbei.ice.web.pages.QueryPage;
import org.jbei.ice.web.pages.SearchResultPage;

public class SearchBarFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private boolean isRendered = false;

    public SearchBarFormPanel(String id) {
        super(id);
    }

    protected void onBeforeRender() {
        if (!isRendered) {
            String queryString = getPage().getPageParameters().getString("search");

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
                    setResponsePage(SearchResultPage.class, new PageParameters("search="
                            + getSearchQuery()));
                }

                public void setSearchQuery(String searchQuery) {
                    this.searchQuery = searchQuery;
                }

                public String getSearchQuery() {
                    return searchQuery;
                }
            }

            Form<?> searchBarForm = new SearchBarForm("searchBarForm", queryString);
            searchBarForm.add(new BookmarkablePageLink<QueryPage>("advancedSearchLink",
                    QueryPage.class));
            searchBarForm.add(new BookmarkablePageLink<BlastPage>("blastSearchLink",
                    BlastPage.class));

            AjaxButton ajaxButton = new AjaxButton("submitButton", new Model<String>("Search"),
                    searchBarForm) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    setRedirect(true);
                    SearchBarForm temp = (SearchBarForm) getParent();
                    String searchQuery = temp.getSearchQuery();
                    setResponsePage(SearchResultPage.class, new PageParameters("search="
                            + searchQuery));
                }
            };

            searchBarForm.add(ajaxButton);
            add(searchBarForm);

            isRendered = true;
        }

        super.onBeforeRender();
    }
}

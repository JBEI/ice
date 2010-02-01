package org.jbei.ice.web.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.query.Filter;
import org.jbei.ice.lib.query.Query;
import org.jbei.ice.lib.search.SearchResult;
import org.jbei.ice.web.forms.CustomChoice;
import org.jbei.ice.web.panels.QueryItemPanel;
import org.jbei.ice.web.panels.QueryResultPanel;

public class QueryPage extends ProtectedPage {
    private LinkedList<QueryItemPanel> filterPanels = new LinkedList<QueryItemPanel>();

    public QueryPage(PageParameters parameters) {
        super(parameters);

        Form<Object> queryFilterForm = new Form<Object>("queryFiltersForm");

        add(queryFilterForm);

        ListView<QueryItemPanel> filtersListView = new ListView<QueryItemPanel>("filtersListView",
                new PropertyModel<List<QueryItemPanel>>(this, "filterPanels")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<QueryItemPanel> item) {
                item.add(item.getModelObject());
            }
        };

        filtersListView.setOutputMarkupId(true);

        QueryItemPanel queryItemPanel = new QueryItemPanel("queryItemPanel");
        queryItemPanel.setOutputMarkupId(true);

        filterPanels.add(queryItemPanel);

        queryFilterForm.add(filtersListView);
        queryFilterForm.add(new AjaxButton("evaluateButton") {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                ListView<QueryItemPanel> formListView = (ListView<QueryItemPanel>) form.get(0); // should be ListView component

                ArrayList<String[]> queries = new ArrayList<String[]>();

                for (Iterator<ListItem> iterator = (Iterator<ListItem>) formListView.iterator(); iterator
                        .hasNext();) {

                    QueryItemPanel queryItemPanel = (QueryItemPanel) iterator.next().get(0);

                    DropDownChoice<Filter> filterDropDownChoice = (DropDownChoice<Filter>) queryItemPanel
                            .get("filtersDropDownChoice");

                    String filter = filterDropDownChoice.getRawInput();

                    if (filter == null || filter == "") {
                        continue;
                    }

                    Fragment queryFragment = (Fragment) queryItemPanel.get("filterPanel");

                    Model<String> fragmentType = (Model<String>) queryFragment.get("fragmentType")
                            .getDefaultModel();

                    if (fragmentType.getObject().equals("string")) {
                        DropDownChoice<CustomChoice> stringFilterPrefixSelect = (DropDownChoice<CustomChoice>) queryFragment
                                .get("stringFilterPrefixSelect");
                        TextField<String> stringFilterInput = (TextField<String>) queryFragment
                                .get("stringFilterInput");

                        String prefix = stringFilterPrefixSelect.getRawInput();
                        String value = stringFilterInput.getRawInput();

                        if (value.isEmpty()) {
                            continue;
                        }

                        queries.add(new String[] { filter, prefix + value });
                    } else if (fragmentType.getObject().equals("selection")) {
                        DropDownChoice<CustomChoice> stringFilterPrefixSelect = (DropDownChoice<CustomChoice>) queryFragment
                                .get("selectionFilterPrefixSelect");
                        DropDownChoice<CustomChoice> selectionFilterValues = (DropDownChoice<CustomChoice>) queryFragment
                                .get("selectionFilterValues");

                        String prefix = stringFilterPrefixSelect.getRawInput();
                        String value = selectionFilterValues.getRawInput();

                        if (value.isEmpty()) {
                            continue;
                        }

                        queries.add(new String[] { filter, prefix + value });
                    } else if (fragmentType.getObject().equals("radio")) {
                        RadioChoice<CustomChoice> radioChoicesValues = (RadioChoice<CustomChoice>) queryFragment
                                .get("radioFilter");

                        String value = radioChoicesValues.getRawInput();

                        if (value.isEmpty()) {
                            continue;
                        }

                        queries.add(new String[] { filter, value });
                    }
                }

                LinkedHashSet<Entry> queryResultSet = Query.getInstance().query(queries);

                ArrayList<SearchResult> queryTableResults = new ArrayList<SearchResult>();

                for (Iterator<Entry> iterator = queryResultSet.iterator(); iterator.hasNext();) {
                    queryTableResults.add(new SearchResult(iterator.next(), 0));
                }

                QueryResultPanel queryResultPanel = (QueryResultPanel) getPage().get(
                        "queryResultPanel");

                queryResultPanel.setQueryResults(queryTableResults);

                target.addComponent(queryResultPanel);
            }
        }.setDefaultFormProcessing(false));

        add(JavascriptPackageResource.getHeaderContribution(UserEntryPage.class, "jquery-1.3.2.js"));
        add(JavascriptPackageResource.getHeaderContribution(UserEntryPage.class,
                "jquery-ui-1.7.2.custom.min.js"));
        add(JavascriptPackageResource.getHeaderContribution(UserEntryPage.class,
                "jquery.cluetip.js"));
        add(CSSPackageResource.getHeaderContribution(UserEntryPage.class, "jquery.cluetip.css"));
        add(new QueryResultPanel("queryResultPanel", 15).setOutputMarkupId(true));
    }

    public List<QueryItemPanel> getFilterPanels() {
        return filterPanels;
    }
}

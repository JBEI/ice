package org.jbei.ice.web.panels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.query.Filter;
import org.jbei.ice.lib.query.Query;
import org.jbei.ice.lib.query.RadioFilter;
import org.jbei.ice.lib.query.SelectionFilter;
import org.jbei.ice.lib.query.StringFilter;
import org.jbei.ice.web.common.CustomChoice;
import org.jbei.ice.web.pages.QueryPage;

public class QueryItemPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public Fragment visibleFilterFragment = null;
    public Filter currentFilter = null;

    public QueryItemPanel(String id) {
        super(id);

        ArrayList<Filter> filters = Query.getInstance().filters();

        add(new DropDownChoice<Filter>("filtersDropDownChoice", new Model<Filter>(currentFilter),
                filters, new ChoiceRenderer<Filter>("name", "key"))
                .add(new AjaxFormComponentUpdatingBehavior("onchange") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        QueryItemPanel queryItemPanel = (QueryItemPanel) getParent().get(0);

                        if (this.getComponent().getDefaultModelObject() instanceof StringFilter) {
                            queryItemPanel.visibleFilterFragment = createStringFilterFragment();

                            queryItemPanel.replace(queryItemPanel.visibleFilterFragment);
                            target.addComponent(visibleFilterFragment);
                        } else if (this.getComponent().getDefaultModelObject() instanceof SelectionFilter) {
                            SelectionFilter filter = (SelectionFilter) this.getComponent()
                                    .getDefaultModelObject();

                            queryItemPanel.visibleFilterFragment = createSelectionFilterFragment(filter
                                    .getChoices());

                            queryItemPanel.replace(queryItemPanel.visibleFilterFragment);
                            target.addComponent(visibleFilterFragment);
                        } else if (this.getComponent().getDefaultModelObject() instanceof RadioFilter) {
                            RadioFilter filter = (RadioFilter) this.getComponent()
                                    .getDefaultModelObject();

                            queryItemPanel.visibleFilterFragment = createRadioFilterFragment(filter
                                    .getChoices());

                            queryItemPanel.replace(queryItemPanel.visibleFilterFragment);
                            target.addComponent(visibleFilterFragment);
                        } else {
                            queryItemPanel.visibleFilterFragment = createEmptyFilterFragment();

                            queryItemPanel.replace(queryItemPanel.visibleFilterFragment);
                            target.addComponent(visibleFilterFragment);
                        }
                    }
                }));

        add(new AjaxButton("addButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                QueryPage queryPage = (QueryPage) getPage();
                MarkupContainer queryFiltersForm = getParent().getParent().getParent().getParent();

                List<QueryItemPanel> panels = queryPage.getFilterPanels();

                panels.add(new QueryItemPanel("queryItemPanel"));

                target.addComponent(queryFiltersForm);
            }
        }.setDefaultFormProcessing(false));

        add(new AjaxButton("removeButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                QueryPage queryPage = (QueryPage) getPage();
                MarkupContainer queryForm = getParent().getParent().getParent().getParent();

                List<QueryItemPanel> panels = queryPage.getFilterPanels();

                if (panels.size() == 1) { // shouldn't remove last panel
                    return;
                }

                int currentPanelIndex = panels.indexOf(getParent());
                panels.remove(currentPanelIndex);

                target.addComponent(queryForm);
            }
        }.setDefaultFormProcessing(false));

        visibleFilterFragment = createEmptyFilterFragment();

        add(visibleFilterFragment);

        visibleFilterFragment.setVisible(true);
    }

    private Fragment createEmptyFilterFragment() {
        Fragment fragment = new Fragment("filterPanel", "emptyFilterFragment", this);

        fragment.add(new HiddenField<String>("fragmentType", new Model<String>("empty")));

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);

        return fragment;
    }

    private Fragment createStringFilterFragment() {
        Fragment fragment = new Fragment("filterPanel", "stringFilterFragment", this);

        ArrayList<CustomChoice> choices = new ArrayList<CustomChoice>();
        choices.add(new CustomChoice("~", "contains"));
        choices.add(new CustomChoice("!~", "doesn't contain"));
        choices.add(new CustomChoice("^", "begins with"));
        choices.add(new CustomChoice("$", "ends with"));
        choices.add(new CustomChoice("=", "is"));
        choices.add(new CustomChoice("!", "is not"));

        CustomChoice stringFilterPrefixChoice = choices.get(0);
        DropDownChoice<CustomChoice> stringFilterPrefixDropDownChoice = new DropDownChoice<CustomChoice>(
                "stringFilterPrefixSelect", new Model<CustomChoice>(stringFilterPrefixChoice),
                new Model<ArrayList<CustomChoice>>(choices), new ChoiceRenderer<CustomChoice>(
                        "value", "name"));

        fragment.add(stringFilterPrefixDropDownChoice);
        fragment.add(new TextField<String>("stringFilterInput"));
        fragment.add(new HiddenField<String>("fragmentType", new Model<String>("string")));

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);

        return fragment;
    }

    private Fragment createSelectionFilterFragment(Map<String, String> data) {
        Fragment fragment = new Fragment("filterPanel", "selectionFilterFragment", this);

        ArrayList<CustomChoice> choices = new ArrayList<CustomChoice>();
        choices.add(new CustomChoice("=", "is"));
        choices.add(new CustomChoice("!", "is not"));

        CustomChoice selectionFilterPrefixChoice = choices.get(0);
        DropDownChoice<CustomChoice> selectionFilterPrefixDropDownChoice = new DropDownChoice<CustomChoice>(
                "selectionFilterPrefixSelect",
                new Model<CustomChoice>(selectionFilterPrefixChoice),
                new Model<ArrayList<CustomChoice>>(choices), new ChoiceRenderer<CustomChoice>(
                        "value", "name"));

        fragment.add(selectionFilterPrefixDropDownChoice);

        ArrayList<CustomChoice> values = new ArrayList<CustomChoice>();
        for (Iterator<Entry<String, String>> iterator = data.entrySet().iterator(); iterator
                .hasNext();) {
            Entry<String, String> pairs = iterator.next();

            values.add(new CustomChoice(pairs.getKey(), pairs.getValue()));
        }

        CustomChoice selectionFilterValuesChoice = values.get(0);
        DropDownChoice<CustomChoice> selectionFilterValuesDropDownChoice = new DropDownChoice<CustomChoice>(
                "selectionFilterValues", new Model<CustomChoice>(selectionFilterValuesChoice),
                new Model<ArrayList<CustomChoice>>(values), new ChoiceRenderer<CustomChoice>(
                        "value", "name"));

        fragment.add(selectionFilterValuesDropDownChoice);
        fragment.add(new HiddenField<String>("fragmentType", new Model<String>("selection")));

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);

        return fragment;
    }

    private Fragment createRadioFilterFragment(Map<String, String> data) {
        Fragment fragment = new Fragment("filterPanel", "radioFilterFragment", this);

        ArrayList<CustomChoice> values = new ArrayList<CustomChoice>();
        for (Iterator<Entry<String, String>> iterator = data.entrySet().iterator(); iterator
                .hasNext();) {
            Entry<String, String> pairs = iterator.next();

            values.add(new CustomChoice(pairs.getKey(), pairs.getValue()));
        }

        RadioChoice<CustomChoice> radioChoices = new RadioChoice<CustomChoice>("radioFilter",
                new Model<ArrayList<CustomChoice>>(values), new ChoiceRenderer<CustomChoice>(
                        "value", "name"));
        radioChoices.setSuffix(" ");
        fragment.add(radioChoices);

        fragment.add(new HiddenField<String>("fragmentType", new Model<String>("radio")));

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);

        return fragment;
    }
}

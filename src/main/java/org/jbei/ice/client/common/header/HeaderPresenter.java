package org.jbei.ice.client.common.header;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ListBox;

// TODO : need associated model to go with this
public class HeaderPresenter {
    private final HeaderView view;
    private FilterOperand currentSelected;
    private ArrayList<FilterOperand> filters;
    private final HeaderModel model;

    public HeaderPresenter(HeaderModel model, HeaderView view) {
        filters = new ArrayList<FilterOperand>();
        this.model = model;

        this.view = view;
        SearchOption option = view.getSearchOption();

        // search options
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put("Select Filter", "");

        // regular search
        for (SearchFilterType type : SearchFilterType.values())
            options.put(type.displayName(), type.name());

        option.setOptions(options);

        // handlers
        setHandlers(option);

        if (this.view.getSearchComposite() != null) {
            PopupHandler handler = new PopupHandler(option, this.view.getSearchComposite()
                    .getTextBox().getElement(), -318, 17);
            this.view.getPullDownArea().addClickHandler(handler);
            view.getSearchButton().addClickHandler(getSearchHandler());
        }
    }

    protected void setHandlers(final SearchOption option) {

        final ListBox filterOptions = option.getFilterOptions();

        // change handler for filter options
        filterOptions.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {

                // get type from value of new selection
                int index = filterOptions.getSelectedIndex();
                String value = filterOptions.getValue(index);

                // now that there is not a separate blast page, BlastProgram is rolled into SearchFilterType
                SearchFilterType type = SearchFilterType.filterValueOf(value);
                if (type == null) {
                    return;
                }

                currentSelected = type.getOperatorAndOperands();
                option.setFilterOperands(currentSelected);
            }
        });

        // button handler for the "Add Filter" button
        option.getAddFilter().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (currentSelected == null)
                    return;

                filters.add(currentSelected);

                // display to user
                String operand = currentSelected.getOperand();
                if (operand.length() > 10) {
                    BlastXSearchFilter filter = new BlastXSearchFilter(operand);
                    view.getSearchComposite().addSearchWidget(filter);
                } else {
                    String search = currentSelected.getType().getShortName().toLowerCase();
                    search += currentSelected.getSelectedOperator().value();
                    search += operand;
                    view.getSearchComposite().appendFilter(search);
                }
            }
        });
    }

    /**
     * @return new handler for searches that occur
     */
    public ClickHandler getSearchHandler() {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // TODO : validation for the search box
                model.submitSearch(filters);
            }
        };
    }
}

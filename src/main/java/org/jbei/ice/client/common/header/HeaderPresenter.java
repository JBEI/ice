package org.jbei.ice.client.common.header;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ListBox;

public class HeaderPresenter {
    private final HeaderView view;
    private FilterOperand currentSelected;
    private final HeaderModel model;
    private SearchFilterInfo blastInfo;

    public HeaderPresenter(HeaderModel model, HeaderView view) {
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
            PopupHandler handler = new PopupHandler(option, view.getSearchComposite().getTextBox()
                    .getElement(), -342, 8); // TODO : needs to be moved to view
            view.getSearchComposite().getPullDownArea().addClickHandler(handler);
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
        option.getAddFilter().addClickHandler(new AddFilterHandler());
    }

    /**
     * @return new handler for searches that occur
     */
    public ClickHandler getSearchHandler() {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // TODO : validation for the search box
                ArrayList<SearchFilterInfo> parse = QuickSearchParser.parse(view.getSearchInput());
                if (blastInfo != null)
                    parse.add(blastInfo);
                model.submitSearch(parse);
            }
        };
    }

    /**
     * Handler for when user clicks "Add filter"
     */
    private class AddFilterHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            if (currentSelected == null)
                return;

            // display to user
            String operand = currentSelected.getOperand();
            switch (currentSelected.getType()) {

            case BLAST:
                final BlastSearchFilter filter = new BlastSearchFilter(operand, currentSelected
                        .getSelectedOperator().value());
                filter.setCloseHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        view.getSearchComposite().removeSearchWidget(filter);
                        blastInfo = null;
                    }
                });

                view.getSearchComposite().addSearchWidget(filter);
                blastInfo = new SearchFilterInfo(currentSelected.getSelectedOperator().value(),
                        currentSelected.getSelectedOperator().value(), operand);
                break;

            default:
                String type = currentSelected.getType().getShortName().toLowerCase();
                String operator = currentSelected.getSelectedOperator().value();

                int indexOfType = view.getSearchInput().indexOf(type);
                if (indexOfType == -1) {
                    String currentFilter = type + operator + operand;
                    view.getSearchComposite().appendFilter(currentFilter);
                } else {
                    String parsed = QuickSearchParser.containsType(view.getSearchInput(),
                        currentSelected.getType(), operand, operator);
                    view.getSearchComposite().setTextFilter(parsed);
                }
            }
        }
    }
}

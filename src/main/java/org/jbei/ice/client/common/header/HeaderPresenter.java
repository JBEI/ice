package org.jbei.ice.client.common.header;

import java.util.LinkedHashMap;

import org.jbei.ice.client.common.FilterWidget;
import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.SearchFilterInfo;
import org.jbei.ice.shared.dto.search.EntrySearchFilter;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class HeaderPresenter {
    private final HeaderView view;
    private FilterWidget currentSelected;
    private SearchFilterInfo blastInfo;
    private EntryType type;           // select search type. null for all
    private EntrySearchFilter searchFilter;

    public HeaderPresenter(HeaderView view) {
        this.view = view;
        searchFilter = new EntrySearchFilter();

        // set handlers.
        view.setFilterChangeHandler(new FilterOptionChangeHandler());
        view.setAddFilterHandler(new AddFilterHandler());
        view.setEntryTypeChangeHandler(new SearchEntryTypeChangeHandler());

        // search options
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put("Select Filter", "");

        // regular search
        for (SearchFilterType type : searchFilter.getSearchFilters())
            options.put(type.displayName(), type.name());

        view.setSearchOptions(options);
        view.createPullDownHandler();
    }

    private class FilterOptionChangeHandler implements ChangeHandler {

        @Override
        public void onChange(ChangeEvent event) {

            // get type from value of new selection
            String value = view.getSelectedFilterValue();

            // now that there is not a separate blast page, BlastProgram is rolled into SearchFilterType
            SearchFilterType type = SearchFilterType.filterValueOf(value);
            if (type == null) {
                return;
            }

            currentSelected = searchFilter.getFilterWidget(type);
            view.setFilterOperands(currentSelected);
        }
    }

    private class SearchEntryTypeChangeHandler implements ChangeHandler {

        /**
         * Called when a change event is fired.
         *
         * @param event the {@link com.google.gwt.event.dom.client.ChangeEvent} that was fired
         */
        @Override
        public void onChange(ChangeEvent event) {
            String value[] = view.getSelectedSearchType();
//            type = EntryType.nameToType(value);
        }
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
            String operand = currentSelected.getSelectedOperand();
            switch (currentSelected.getType()) {

                case BLAST:
                    final BlastSearchFilter filter = new BlastSearchFilter(operand, currentSelected
                            .getSelectedOperator().symbol());
                    filter.setCloseHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            view.getSearchComposite().removeSearchWidget(filter);
                            blastInfo = null;
                        }
                    });

                    view.getSearchComposite().setSearchWidget(filter);
                    blastInfo = new SearchFilterInfo(currentSelected.getSelectedOperator().symbol(),
                                                     currentSelected.getSelectedOperator().symbol(), operand);
                    break;

                default:
                    String type = currentSelected.getType().getShortName().toLowerCase();
                    String operator = currentSelected.getSelectedOperator().symbol();

                    int indexOfType = view.getSearchInput().indexOf(type);
                    if (indexOfType == -1) {
                        String currentFilter = type + operator;
                        if (operand.contains(" "))
                            currentFilter += ("\"" + operand + "\"");
                        else
                            currentFilter += operand;
                        view.getSearchComposite().appendFilter(currentFilter);
                    } else {
                        String parsed = QuickSearchParser.containsType(view.getSearchInput(),
                                                                       currentSelected, operand, operator);
                        view.getSearchComposite().setTextFilter(parsed);
                    }
            }
        }
    }

    public SearchFilterInfo getBlastInfo() {
        return blastInfo;
    }
}

package org.jbei.ice.client.common.header;

import java.util.LinkedHashMap;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class HeaderPresenter {
    private final HeaderView view;
    private FilterOperand currentSelected;
    private SearchFilterInfo blastInfo;

    public HeaderPresenter(HeaderView view) {
        this.view = view;

        // search options
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put("Select Filter", "");

        // regular search
        for (SearchFilterType type : SearchFilterType.values())
            options.put(type.displayName(), type.name());

        view.setSearchOptions(options);

        // handlers
        setHandlers();

        view.createPullDownHandler();
    }

    protected void setHandlers() {
        view.setFilterChangeHandler(new FilterOptionChangeHandler());
        view.setAddFilterHandler(new AddFilterHandler());
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

            currentSelected = type.getOperatorAndOperands();
            view.setFilterOperands(currentSelected);
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

    public boolean isModerator() {
        return AppController.accountInfo.isAdmin();
    }
}

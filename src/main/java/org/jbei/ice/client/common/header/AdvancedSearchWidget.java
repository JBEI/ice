package org.jbei.ice.client.common.header;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.shared.dto.EntryType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Options widget for search
 *
 * @author Hector Plahar
 */
public class AdvancedSearchWidget extends Composite {

    private final EntryTypeFilterWidget entryTypes;  // entry type filter
    private final Button addFilter;
    private final Button runSearch;
    private final FlexTable panel;
    private final FlexTable filterOptionsPanel;
    private int currentRow;
    private ChangeHandler optionChangeHandler;
    private HashMap<String, String> listOptions;
    private final Set<String> selectedOptions; // filter options that have already been selected
    private final HashMap<Integer, ListBox> rowBox; // mapping of row->list box

    public AdvancedSearchWidget() {
        panel = new FlexTable();
        panel.setCellPadding(0);
        panel.setCellSpacing(0);
        initWidget(panel);
        panel.setStyleName("bg_white");

        // init components
        filterOptionsPanel = new FlexTable();
        filterOptionsPanel.setWidth("100%");
        filterOptionsPanel.setHeight("100%");

        listOptions = new HashMap<String, String>();
        selectedOptions = new HashSet<String>();
        rowBox = new HashMap<Integer, ListBox>();

        addFilter = new Button("Add Filter(s)");
        runSearch = new Button("Search");
        entryTypes = new EntryTypeFilterWidget();

        panel.setWidget(0, 0, entryTypes);
        panel.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        panel.getFlexCellFormatter().setColSpan(0, 0, 2);
        panel.getFlexCellFormatter().setHeight(0, 0, "23px");
    }

    // meant to be called only once to set the options availabe for searching
    public void initializeWidget(LinkedHashMap<String, String> listOptions) {
        if (listOptions == null)
            return;

        this.listOptions = listOptions;

        panel.setWidget(1, 0, filterOptionsPanel);
        panel.getFlexCellFormatter().setColSpan(1, 0, 2);
        panel.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);

        panel.setWidget(2, 0, addFilter);
        panel.setWidget(2, 1, runSearch);
        panel.getFlexCellFormatter().setWidth(2, 1, "50px");
        panel.getFlexCellFormatter().setHorizontalAlignment(2, 0, HasAlignment.ALIGN_RIGHT);

        filterOptionsPanel.removeAllRows();
        ListBox filterBox = createFilterBox();

        if (filterBox != null) {
            filterOptionsPanel.setWidget(currentRow, 0, filterBox);
            rowBox.put(currentRow, filterBox);
        }
    }

    /**
     * @return a list box whose options consists of available filters sans
     *         those that have already been selected
     */
    protected ListBox createFilterBox() {
        ListBox options = new ListBox();
        options.setWidth("120px");
        options.setStyleName("pull_down");
        if (this.optionChangeHandler != null) {
            options.addChangeHandler(this.optionChangeHandler);
        }

        for (Map.Entry<String, String> entry : listOptions.entrySet()) {
            String key = entry.getKey();
            if (selectedOptions.contains(key))
                continue;
            options.addItem(entry.getKey(), entry.getValue());
        }
        return options;
    }

    public void setFilterOperands(Widget operand, EntryType... restrictions) {
        filterOptionsPanel.setWidget(currentRow, 1, operand);

        // add filter icon and handler
        Icon icon = new Icon(FAIconType.PLUS_SIGN);
        icon.addStyleName("add_filter_style");
        icon.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GWT.log("Adding new filter to row " + currentRow);
                addNewFilter(currentRow);
                currentRow += 1;

            }
        }, ClickEvent.getType());
        filterOptionsPanel.setWidget(currentRow, 2, icon);

        // remove filter icon and handler
        Icon removeIcon = new Icon(FAIconType.MINUS_SIGN);
        removeIcon.addStyleName("remove_filter");
        removeIcon.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GWT.log("Removing filter from row " + currentRow);
                removeFilter(currentRow);
                currentRow -= 1;
            }
        }, ClickEvent.getType());
        filterOptionsPanel.setWidget(currentRow, 3, removeIcon);
    }

    protected void addNewFilter(int afterRow) {
        ListBox filterBox = createFilterBox();
        filterOptionsPanel.setWidget(afterRow, 0, filterBox); // TODO : remove selected option from options
        rowBox.put(afterRow, filterBox);
    }

    protected void removeFilter(int atRow) {
        rowBox.remove(atRow);
    }

    public HasClickHandlers getAddFilter() {
        return this.addFilter;
    }

    public void setOptionChangeHandler(ChangeHandler handler) {
        this.optionChangeHandler = handler;
    }

    public String getSelectedFilter() {
        final ListBox filterOptions = rowBox.get(currentRow);
        if (filterOptions == null)
            return "";

        int index = filterOptions.getSelectedIndex();
        String selected = filterOptions.getValue(index);
        selectedOptions.add(selected);
        return selected;
    }

    public String[] getSelectedEntrySearch() {
        return entryTypes.getSelected();
    }
}

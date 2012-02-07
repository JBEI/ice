package org.jbei.ice.client.common.header;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchEventHandler;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

// TODO : need associated model to go with this
public class HeaderPresenter {
    private final HeaderView view;
    private FilterOperand currentSelected;
    private ArrayList<FilterOperand> filters;

    public HeaderPresenter(HeaderView view) {
        filters = new ArrayList<FilterOperand>();

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
                    .getTextBox().getElement());
            this.view.getSearchArrow().addClickHandler(handler);
            view.getSearchButton().addClickHandler(getSearchHandler());
        }
    }

    protected void setHandlers(final SearchOption option) {

        final ListBox filterOptions = option.getFilterOptions();

        filterOptions.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {

                // get type from value of new selection
                int index = filterOptions.getSelectedIndex();
                String value = filterOptions.getValue(index);

                // TODO : now that there is not a separate blast page, combine BlastProgram with SearchFilterType
                SearchFilterType type = SearchFilterType.filterValueOf(value);
                if (type == null) {
                    return;
                }

                currentSelected = type.getOperatorAndOperands();
                option.setFilterOperands(currentSelected);

                // set the textArea for the view
                // TODO : pass to model

                //                if (type == null) {
                //                    panel.setHTML(2, 0, "&nbsp;");
                //                    panel.setHTML(3, 0, "&nbsp;");
                //                    return;
                //                }
                //
                //                // operand
                //                operand = type.getOperatorAndOperands();
                //                if (operand != null)
                //                    panel.setWidget(2, 0, operand);
                //                else {
                //                    panel.setHTML(2, 0, "&nbsp;");
                //                    panel.setHTML(3, 0, "&nbsp;");
                //                    return;
                //                }
                //
                //                panel.setWidget(3, 0, addFilter);
                //                panel.getFlexCellFormatter().setHorizontalAlignment(3, 0, HasAlignment.ALIGN_RIGHT);

            }
        });

        // button handler
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

                // TODO : send message on event bus with list of filters as parameter
                //                String query = view.getSearchInput();
                //                if (query == null || query.isEmpty())
                //                    view.getSearchComposite().getTextBox().setFocus(true);
                //                else
                //                    History.newItem(Page.QUERY.getLink() + ";id=" + query, false); // causes app controller to handle the logic
            }
        };
    }

    //
    // inner classes
    //
    private class PopupHandler implements ClickHandler {

        private final PopupPanel popup;
        private final int xOffset = -318;

        public PopupHandler(Widget widget, Element autoHide) {
            this.popup = new PopupPanel();
            this.popup.setStyleName("add_to_popup");
            this.popup.setAutoHideEnabled(true);
            this.popup.addAutoHidePartner(autoHide);
            this.popup.setWidget(widget);
            this.popup.setGlassEnabled(true);
        }

        @Override
        public void onClick(ClickEvent event) {
            if (!popup.isShowing()) {
                Widget source = (Widget) event.getSource();
                int x = source.getAbsoluteLeft() + xOffset;
                int y = source.getOffsetHeight() + source.getAbsoluteTop() + 17;
                popup.setPopupPosition(x, y);
                popup.show();
            } else {
                popup.hide();
            }
        }

        public void hidePopup() {
            if (this.popup == null || !this.popup.isShowing())
                return;

            this.popup.hide();
        }
    }

    private static class SearchHandler implements SearchEventHandler {

        @Override
        public void onSearch(SearchEvent event) {
            // TODO Auto-generated method stub
        }
    }
}

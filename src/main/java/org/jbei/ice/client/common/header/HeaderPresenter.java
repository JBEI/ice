package org.jbei.ice.client.common.header;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchEventHandler;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class HeaderPresenter {
    private final HeaderView view;
    private final SearchOption option;

    public HeaderPresenter(HeaderView view) {
        this.view = view;
        option = new SearchOption();
        option.addStyleName("background_white");
        option.setWidth("350px");
        option.setHeight("150px");
        if (this.view.getSearchComposite() != null) {
            MenuClickHandler handler = new MenuClickHandler(option, this.view.getSearchComposite()
                    .getTextBox().getElement());
            this.view.getSearchArrow().addClickHandler(handler);
            view.getSearchButton().addClickHandler(getSearchHandler());
        }
    }

    public ClickHandler getSearchHandler() {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String query = view.getSearchInput();
                if (query == null || query.isEmpty())
                    view.getSearchComposite().getTextBox().setFocus(true);
                else
                    History.newItem(Page.QUERY.getLink() + ";id=" + query);
            }
        };
    }

    //
    // inner classes
    //
    private class SearchOption extends Composite {

        private final ListBox options; // search options
        private FilterOperand operand;
        private final Button addFilter;
        private final FlexTable panel;

        public SearchOption() {
            panel = new FlexTable();
            panel.setCellPadding(5);
            panel.setCellSpacing(0);
            initWidget(panel);

            addFilter = new Button("Add Filter");
            options = new ListBox();
            options.setWidth("150px");
            options.setStyleName("pull_down");

            for (SearchFilterType type : SearchFilterType.values())
                this.options.addItem(type.displayName(), type.name());

            panel.setHTML(0, 0,
                "<span class=\"font-85em font-bold\" style=\"color: #999\">FILTERS</span>");
            panel.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
            panel.getFlexCellFormatter().setHeight(0, 0, "20px");

            panel.setWidget(1, 0, options);
            panel.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
            bind();
        }

        private void bind() {

            options.addChangeHandler(new ChangeHandler() {

                @Override
                public void onChange(ChangeEvent event) {

                    // get type from value of new selection
                    int index = options.getSelectedIndex();
                    String value = options.getValue(index);
                    SearchFilterType type = SearchFilterType.valueOf(value);
                    if (type == null) {
                        panel.setHTML(2, 0, "&nbsp;");
                        panel.setHTML(3, 0, "&nbsp;");
                        return;
                    }

                    // operand
                    operand = type.getOperatorAndOperands();
                    if (operand != null)
                        panel.setWidget(2, 0, operand);
                    else {
                        panel.setHTML(2, 0, "&nbsp;");
                        panel.setHTML(3, 0, "&nbsp;");
                        return;
                    }

                    panel.setWidget(3, 0, addFilter);
                    panel.getFlexCellFormatter().setHorizontalAlignment(3, 0,
                        HasAlignment.ALIGN_RIGHT);
                }
            });

            // button handler
            addFilter.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    TextBox box = view.getSearchComposite().getTextBox();
                    String text = getSearchType().name().toLowerCase();
                    text += getOperator().value();
                    text += getOperand();
                    if (box.getText().isEmpty())
                        box.setText(text);
                    else
                        box.setText(box.getText() + " " + text);
                }
            });
        }

        public SearchFilterType getSearchType() {
            return operand.getType();
        }

        public QueryOperator getOperator() {
            return operand.getSelectedOperator();
        }

        public String getOperand() {
            return operand.getOperand();
        }
    }

    private class MenuClickHandler implements ClickHandler {

        private final PopupPanel popup;

        public MenuClickHandler(Widget widget, Element autoHide) {
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
                int x = source.getAbsoluteLeft() - 295;
                int y = source.getOffsetHeight() + source.getAbsoluteTop() + 12;
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

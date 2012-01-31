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
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderPresenter {
    private final HeaderView view;
    private final SearchOption option;

    public HeaderPresenter(HeaderView view) {
        this.view = view;
        option = new SearchOption();
        option.addStyleName("background_white");
        option.setWidth("350px");
        option.setHeight("180px");
        if (this.view.getSearchComposite() != null) {
            MenuClickHandler handler = new MenuClickHandler(option, this.view.getSearchComposite()
                    .getTextBox().getElement());
            this.view.getSearchArrow().addClickHandler(handler);

            view.getSearchButton().addClickHandler(getSearchHandler());
        }

        // search
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

        private final VerticalPanel panel;
        private final ListBox options; // search options
        private FilterOperand operand;
        private final Button addFilter;

        public SearchOption() {
            panel = new VerticalPanel();
            initWidget(panel);

            addFilter = new Button("Add Filter");
            options = new ListBox();
            options.setWidth("120px");
            options.setStyleName("input_box");

            for (SearchFilterType type : SearchFilterType.values())
                this.options.addItem(type.displayName(), type.name());

            panel.add(options);
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
                    if (type == null)
                        return;

                    if (panel.getWidgetCount() > 1) {
                        panel.remove(1);
                    }

                    // operand
                    operand = type.getOperatorAndOperands();
                    if (operand != null)
                        panel.add(operand);

                    panel.add(addFilter);
                    panel.setCellHorizontalAlignment(addFilter, HasAlignment.ALIGN_RIGHT);
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

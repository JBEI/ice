package org.jbei.ice.client.common.header;

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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
                QuickSearchParser.parse(view.getSearchInput());
            }
        };
    }

    //
    // inner classes
    //
    private class SearchOption extends Composite {

        private final ListBox options;
        private final HorizontalPanel panel;
        private FilterOperand operand;

        public SearchOption() {
            panel = new HorizontalPanel();
            initWidget(panel);

            options = new ListBox();
            options.setWidth("120px");

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

                    TextBox box = view.getSearchComposite().getTextBox();
                    String text = box.getText();
                    text += value;
                    box.setText(text);

                    if (panel.getWidgetCount() > 1) {
                        panel.remove(1);
                    }

                    // set the filter widgets 
                    operand = type.getOperatorAndOperands();
                    if (operand != null)
                        panel.add(operand);
                }
            });
        }

        public SearchFilterType getSearchType() {
            return operand.getType();
        }

        public QueryOperator getOperator() {
            return operand.getOperator();
        }

        public String getOperand() {
            return operand.getOperand();
        }
    }

    private class MenuClickHandler implements ClickHandler {

        private final PopupPanel popup;

        public MenuClickHandler(Widget widget, Element autoHide) {
            this.popup = new PopupPanel();
            this.popup.setAutoHideEnabled(true);
            this.popup.addAutoHidePartner(autoHide);
            this.popup.setWidget(widget);
            this.popup.setGlassEnabled(true);
        }

        @Override
        public void onClick(ClickEvent event) {
            if (!popup.isShowing()) {
                Widget source = (Widget) event.getSource();
                int x = source.getAbsoluteLeft() - 300;
                int y = source.getOffsetHeight() + source.getAbsoluteTop() + 1;
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

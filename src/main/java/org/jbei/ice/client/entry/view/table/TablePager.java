package org.jbei.ice.client.entry.view.table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.HasRows;

public class TablePager extends AbstractPager {

    private Label label;
    private NavLink prevLink;
    private NavLink nextLink;

    public TablePager() {
        this.label = new Label();
        this.label.setStyleName("font-bold");

        nextLink = new NavLink("&gt;", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int indx = TablePager.super.getPage() + 1;
                TablePager.super.setPage(indx);
            }
        });
        nextLink.addStyleName("nav-right");

        prevLink = new NavLink("&lt;", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int indx = TablePager.super.getPage() - 1;
                TablePager.super.setPage(indx);
            }
        });
        prevLink.addStyleName("nav-left");

        HorizontalPanel layout = new HorizontalPanel();
        layout.setSpacing(5);
        layout.setStyleName("font-75em");
        layout.add(label);
        layout.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
        layout.add(prevLink);
        layout.add(nextLink);
        initWidget(layout);
    }

    @Override
    protected void onRangeOrRowCountChanged() {
        this.label.setText(this.createText());

        prevLink.setDisabled(!hasPreviousPage());
        nextLink.setDisabled(!hasNextPage());
    }

    protected String createText() {

        HasRows display = getDisplay();
        int dataSize = display.getRowCount();
        if (dataSize == 0)
            return "No Records";

        // Default text is 1 based.
        NumberFormat formatter = NumberFormat.getFormat("#,###");

        int pageStart = getPageStart() + 1;
        int pageSize = getPageSize();

        int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
        endIndex = Math.max(pageStart, endIndex);
        boolean exact = display.isRowCountExact();

        return formatter.format(pageStart) + " - " + formatter.format(endIndex)
                + (exact ? " of " : " of over ") + formatter.format(dataSize);
    }

    // 
    // inner classes
    //
    private static class NavLink extends Button {

        public NavLink(String html, ClickHandler handler) {

            super(html, handler);
            addStyles();
        }

        protected void addStyles() {

            this.setStyleName("button_to_link");
            this.addMouseOverHandler(new MouseOverHandler() {

                @Override
                public void onMouseOver(MouseOverEvent event) {
                    setStyleName("button_to_link_hover");
                }
            });

            this.addMouseOutHandler(new MouseOutHandler() {

                @Override
                public void onMouseOut(MouseOutEvent event) {
                    setStyleName("button_to_link");
                }
            });
        }

        public void setDisabled(boolean isDisabled) {

            if (this.isEnabled() == !isDisabled)
                return;

            this.setEnabled(!isDisabled);
            if (!this.isEnabled())
                setStyleName("button_to_link_disabled");
            else
                setStyleName("button_to_link");
        }
    }

}

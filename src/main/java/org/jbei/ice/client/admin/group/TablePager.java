package org.jbei.ice.client.admin.group;

import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.HasRows;

public class TablePager extends AbstractPager {

    private Label label;
    private NavLink prevLink;
    private NavLink nextLink;

    public TablePager() {
        this.label = new Label();
        this.label.setStyleName("font-bold");
        this.label.addStyleName("font-75em");

        nextLink = new NavLink("Next <i class=\"" + FAIconType.CHEVRON_RIGHT.getStyleName() + "\"></i>",
                               new ClickHandler() {

                                   @Override
                                   public void onClick(ClickEvent event) {
                                       int indx = TablePager.super.getPage() + 1;
                                       TablePager.super.setPage(indx);
                                   }
                               });
        nextLink.addStyleName("nav-right");

        prevLink = new NavLink("<i class=\"" + FAIconType.CHEVRON_LEFT.getStyleName() + "\"></i> Prev",
                               new ClickHandler() {

                                   @Override
                                   public void onClick(ClickEvent event) {
                                       int indx = TablePager.super.getPage() - 1;
                                       TablePager.super.setPage(indx);
                                   }
                               });
        prevLink.addStyleName("nav-left");

        FlexTable layout = new FlexTable();
        layout.setWidth("100%");
        layout.setStyleName("border-bottom");

        layout.setWidget(0, 0, prevLink);
        layout.getFlexCellFormatter().setWidth(0, 0, "60px");

        layout.setWidget(0, 1, label);
        layout.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_CENTER);

        layout.setWidget(0, 2, nextLink);
        layout.getFlexCellFormatter().setWidth(0, 2, "60px");
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
            return "0/0";

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
            setStyleName("button_to_link");
        }

        public void setDisabled(boolean isDisabled) {
            if (this.isEnabled() == !isDisabled)
                return;

            this.setEnabled(!isDisabled);
            if (!this.isEnabled())
                setStyleName("button_to_link");
        }
    }
}

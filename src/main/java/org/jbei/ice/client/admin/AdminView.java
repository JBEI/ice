package org.jbei.ice.client.admin;

import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class AdminView extends AbstractLayout {

    private TabLayoutPanel panel;

    @Override
    protected void initComponents() {
        super.initComponents();

        panel = new TabLayoutPanel(1.5, Unit.EM);
        panel.setHeight("100%");
        panel.getElement().getStyle().setMarginBottom(10.0, Unit.PX);
    }

    public void addLayoutHandler(SelectionHandler<Integer> handler) {
        panel.addSelectionHandler(handler);
    }

    public void setTabWidget(AdminTab tab, AdminPanel<?> view) {
        panel.insert(view, view.getTabTitle(), tab.ordinal());
    }

    @Override
    protected Widget createContents() {
        return panel;
    }
}

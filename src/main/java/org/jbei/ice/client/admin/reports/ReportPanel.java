package org.jbei.ice.client.admin.reports;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

public class ReportPanel extends Composite implements AdminPanel {

    private VerticalPanel panel;

    public ReportPanel() {
        this.panel = new VerticalPanel();
        initWidget(this.panel);
    }

    public void addWidget(Widget widget) {
        panel.clear();
        panel.add(widget);
    }

    public String getTabTitle() {
        return "Reports";
    }

    @Override
    public HasData<AccountInfo> getDisplay() {
        return null;
    }
}

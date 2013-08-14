package org.jbei.ice.client.admin.part;

import java.util.Set;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Admin panel for transferred parts. Works with {@link AdminTransferredPartPresenter}
 *
 * @author Hector Plahar
 */
public class AdminTransferredPartPanel extends Composite implements IAdminPanel {

    private final TransferredPartTable table;
    private final Button approve;
    private final Button reject;
    private final VerticalPanel vPanel;

    public AdminTransferredPartPanel(ServiceDelegate<PartData> delegate) {
        table = new TransferredPartTable(delegate);
        approve = new Button("Approve");
        reject = new Button("Reject");

        vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        initView();
        initWidget(vPanel);
    }

    public EntryDataTable<PartData> getDataTable() {
        return this.table;
    }

    public void setApproveClickHandler(ClickHandler handler) {
        approve.addClickHandler(handler);
    }

    public void setRejectClickHandler(ClickHandler handler) {
        reject.addClickHandler(handler);
    }

    public void setMainContent(Widget widget) {
        vPanel.clear();
        vPanel.add(new HTML("&nbsp;"));
        HTMLPanel panel = new HTMLPanel("<span id=\"approve_button\"></span><span id=\"reject_button\"></span>");
        panel.add(approve, "approve_button");
        panel.add(reject, "reject_button");
        vPanel.add(panel);
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(widget);
    }

    public Set<Long> getSelectParts() {
        return table.getSelectedEntrySet();
    }

    public void setEnableApproveReject(boolean enable) {
        approve.setEnabled(enable);
        reject.setEnabled(enable);
    }

    public void initView() {
        vPanel.clear();
        vPanel.add(new HTML("&nbsp;"));
        HTMLPanel panel = new HTMLPanel("<span id=\"approve_button\"></span><span id=\"reject_button\"></span>");
        panel.add(approve, "approve_button");
        panel.add(reject, "reject_button");
        vPanel.add(panel);
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(table);
        vPanel.add(table.getPager());

        approve.setEnabled(false);
        reject.setEnabled(false);
    }

    public void refresh() {
        History.fireCurrentHistoryState();
    }
}

package org.jbei.ice.client.admin.part;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Admin panel for transferred parts. Works with {@link AdminTransferredPartPresenter}
 *
 * @author Hector Plahar
 */
public class AdminTransferredPartPanel extends Composite implements IAdminPanel {

    private final TransferredPartTable table;
    private final Button createAccount;
    private final VerticalPanel vPanel;

    public AdminTransferredPartPanel(ServiceDelegate<PartData> delegate) {
        table = new TransferredPartTable(delegate);
        createAccount = new Button("<i class=\"blue " + FAIconType.USER.getStyleName() + "\"></i> Create Account");

        vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(createAccount);
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(table);
        vPanel.add(table.getPager());
        initWidget(vPanel);
    }

    public DataTable<PartData> getDataTable() {
        return this.table;
    }

    public void setMainContent(Widget widget) {
        vPanel.clear();
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(widget);
    }
}

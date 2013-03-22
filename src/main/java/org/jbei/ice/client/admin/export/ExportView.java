package org.jbei.ice.client.admin.export;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminTab;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.view.client.HasData;

/**
 * @author Hector Plahar
 */
public class ExportView extends Composite implements AdminPanel<EntryInfo> {

    private final FlexTable layout;
    private final TextArea idList;
    private final Button exportButton;

    public ExportView() {
        layout = new FlexTable();
        layout.setWidth("100%");
        initWidget(layout);

        idList = new TextArea();
        idList.setVisibleLines(20);
        idList.setStyleName("input_box");
        exportButton = new Button("Export");

        layout.setWidget(0, 0, idList);
        layout.setWidget(1, 0, exportButton);
    }

    @Override
    public String getTabTitle() {
        return "Export";
    }

    @Override
    public HasData<EntryInfo> getDisplay() {
        return null;
    }

    @Override
    public AdminTab getTab() {
        return AdminTab.EXPORT;
    }

    public String getIdList() {
        return idList.getText();
    }

    public void setExportHandler(ClickHandler exportClickHandler) {
        exportButton.addClickHandler(exportClickHandler);
    }
}

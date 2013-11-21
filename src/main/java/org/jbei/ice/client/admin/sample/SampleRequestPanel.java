package org.jbei.ice.client.admin.sample;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * Admin panel view specifically for sample requests
 *
 * @author Hector Plahar
 */
public class SampleRequestPanel extends Composite implements IAdminPanel {

    private final SampleRequestTable sampleTable;

    public SampleRequestPanel(Delegate<SampleRequest> delegate) {
        FlexTable table = new FlexTable();
        initWidget(table);
        sampleTable = new SampleRequestTable(delegate);
        table.setHTML(0, 0, "&nbsp;");
        table.setWidget(1, 0, sampleTable);
    }

    public SampleRequestTable getTable() {
        return this.sampleTable;
    }
}

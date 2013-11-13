package org.jbei.ice.client.admin.sample;

import org.jbei.ice.client.admin.IAdminPanel;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * Admin panel view specifically for sample requests
 *
 * @author Hector Plahar
 */
public class SampleRequestPanel extends Composite implements IAdminPanel {

    public SampleRequestPanel() {
        FlexTable table = new FlexTable();
        initWidget(table);
    }
}

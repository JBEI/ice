package org.jbei.ice.client.admin.setting;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * @author Hector Plahar
 */
public class SystemSettingsWidget extends Composite {

    private final FlexTable layout;

    public SystemSettingsWidget() {
        layout = new FlexTable();
        initWidget(layout);
    }

    public void setData(HashMap<String, String> settings) {
        layout.clear();
        int row = 0;
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            layout.setHTML(row, 0, entry.getKey());
            layout.setHTML(row, 1, entry.getValue());
            row += 1;
        }
    }
}

package org.jbei.ice.client.admin.setting;

import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.dto.ConfigurationKey;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

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

            String key;
            ConfigurationKey configurationKey = ConfigurationKey.stringToEnum(entry.getKey());
            if (configurationKey == null || configurationKey.toString().isEmpty())
                key = entry.getKey();
            else
                key = configurationKey.toString();

            layout.setHTML(row, 0, "<b class=\"font-75em\" style=\"text-transform: uppercase\">" + key + "</b>");
            layout.setHTML(row, 1, "<span class=\"font-75em\" style=\"margin-left: 30px; margin-right: 30px\">"
                    + entry.getValue() + "</span>");
            layout.setHTML(row, 2, "<span class=\"font-70em\"><i class=\"" + FAIconType.EDIT.getStyleName()
                    + "\"> Edit</span>");
            row += 1;
        }
    }

    // for each category create a widget for it that holds all the settings
    protected Widget createCategoryWidget() {
        return null;
    }
}

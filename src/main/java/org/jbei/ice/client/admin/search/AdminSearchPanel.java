package org.jbei.ice.client.admin.search;

import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.admin.setting.SettingPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Admin panel for search management. Works with {@link AdminSearchPresenter}
 *
 * @author Hector Plahar
 */
public class AdminSearchPanel extends Composite implements IAdminPanel {

    private FlexTable layout;
    private Button reIndex;

    public AdminSearchPanel() {
        initComponents();

        layout.setHTML(0, 0, "&nbsp;");
        layout.setWidget(1, 0, reIndex);

        initWidget(layout);
    }

    public void setRebuildIndexesHandler(final ClickHandler handler) {
        reIndex.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!Window.confirm(
                        "This action will cause search results to be incomplete until rebuilding is done. Continue?"))
                    return;

                handler.onClick(event);
            }
        });
    }

    public void setConfigValue(ConfigurationKey key, int row, String value) {
        for (int i = 0; i < layout.getRowCount(); i += 1) {
            Widget widget = layout.getWidget(i, 0);
            if (!(widget instanceof SettingPanel))
                continue;

            SettingPanel panel = (SettingPanel) widget;
            panel.updateConfigSetting(key, row, value);
        }
    }

    protected void initComponents() {
        layout = new FlexTable();
        layout.setWidth("100%");
        reIndex = new Button("<i class=\"blue " + FAIconType.REFRESH.getStyleName() + "\"></i>&nbsp; Rebuild Indexes");
    }

    public void setSearchSetting(SettingPanel settingPanel) {
        layout.setWidget(2, 0, settingPanel);
        layout.getFlexCellFormatter().setStyleName(2, 0, "pad_top");
    }
}

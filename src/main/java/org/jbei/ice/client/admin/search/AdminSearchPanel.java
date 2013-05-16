package org.jbei.ice.client.admin.search;

import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Admin panel for search management. Works with {@link AdminSearchPresenter}
 *
 * @author Hector Plahar
 */
public class AdminSearchPanel extends Composite implements IAdminPanel {

    private VerticalPanel vPanel;
    private Button reIndex;

    public AdminSearchPanel() {
        initComponents();
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(reIndex);
        initWidget(vPanel);
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

    protected void initComponents() {
        vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        reIndex = new Button("<i class=\"blue " + FAIconType.REFRESH.getStyleName() + "\"></i>&nbsp; Rebuild Indexes");
    }

}

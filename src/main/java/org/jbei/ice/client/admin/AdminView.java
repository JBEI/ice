package org.jbei.ice.client.admin;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.admin.reports.ReportPanel;
import org.jbei.ice.client.admin.usermanagement.EditUserPanel;
import org.jbei.ice.client.collection.menu.CollectionMenu;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class AdminView extends AbstractLayout {

    private FlexTable contentTable;
    private CollectionMenu draftsMenu; // TODO
    private TabLayoutPanel panel;

    @Override
    protected void initComponents() {
        super.initComponents();

        contentTable = new FlexTable();
        panel = new TabLayoutPanel(1.5, Unit.EM);
        draftsMenu = new CollectionMenu(false, "BULK IMPORT");

        contentTable.setWidth("100%");
        contentTable.setHTML(0, 0, "&nbsp;");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        panel.add(contentTable, "Verify Bulk Import");
        panel.setHeight("100%");
        panel.getElement().getStyle().setMarginBottom(10.0, Unit.PX);

        // user management
        EditUserPanel editUser = new EditUserPanel();
        panel.add(editUser, editUser.getTabTitle());

        // report
        ReportPanel reportPanel = new ReportPanel();
        panel.add(reportPanel, reportPanel.getTabTitle());
    }

    public void addLayoutHandler(SelectionHandler<Integer> handler) {
        panel.addSelectionHandler(handler);
    }

    public void setTabPresenter(int index, AdminPanelPresenter presenter) {
        presenter.go((AdminPanel) panel.getWidget(index));
    }

    @Override
    protected Widget createContents() {
        return panel;
    }

    protected Widget createMainContent() {
        return new HTML("&nbsp;");
    }

    public void setSavedDraftsData(ArrayList<MenuItem> data) {
        draftsMenu.setMenuItems(data, null);
        contentTable.setWidget(0, 0, draftsMenu);
        contentTable.getFlexCellFormatter().setWidth(0, 0, "220px");
    }

    public SingleSelectionModel<MenuItem> getDraftMenuModel() {
        return draftsMenu.getSelectionModel();
    }

    public void setSheet(BulkImportDraftInfo result, boolean b) {

        String url = GWT.getHostPageBaseURL();
        String html = "<object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540002\" id=\"VectorEditor\" width=\"100%\" height=\"100%\" codebase=\"https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab\"> "
                + "<param name=\"movie\" value=\"EntryBulkImport.swf\">"
                + "<param name=\"quality\" value=\"high\">"
                + "<param name=\"bgcolor\" value=\"#869ca7\">"
                + "<param name=\"wmode\" value=\"opaque\">"
                + "<param name=\"allowScriptAccess\" value=\"sameDomain\">"
                + "<embed src=\""
                + url
                + "static/swf/bi/EntryBulkImport.swf"
                + "?id="
                + result.getId()
                + "&amp;sessionId="
                + AppController.sessionId
                + "&importId="
                + result.getId()
                + "\" quality=\"high\" bgcolor=\"#869ca7\" width=\"100%\" wmode=\"opaque\" height=\"100%\" name=\"VectorEditor\" align=\"middle\" play=\"true\" loop=\"false\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.adobe.com/go/getflashplayer\"></object>";
        HTML widget = new HTML(html);
        widget.setStyleName("z-index-low");
        widget.setHeight("100%");
        widget.setWidth("100%");

        contentTable.setWidget(0, 1, widget);
        contentTable.getFlexCellFormatter().setHeight(0, 1, "600px");
    }
}

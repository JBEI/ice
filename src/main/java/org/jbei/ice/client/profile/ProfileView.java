package org.jbei.ice.client.profile;

import org.jbei.ice.client.collection.table.SamplesDataTable;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProfileView extends AbstractLayout implements IProfileView {

    private SamplesDataTable samplesTable;

    private Label contentHeader;
    private ProfileViewMenu menu;
    private FlexTable mainContent;

    @Override
    protected Widget createContents() {
        contentHeader = new Label("");
        mainContent = new FlexTable();
        //        createEntriesTablePanel();
        this.createSamplesTablePanel();

        FlexTable contentTable = new FlexTable();
        contentTable.setWidth("100%");
        contentTable.setWidget(0, 0, createMenu());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        // TODO : middle sliver goes here
        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getCellFormatter().setWidth(0, 1, "100%");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
        return contentTable;
    }

    protected Widget createMenu() {
        menu = new ProfileViewMenu();
        return menu;
    }

    protected Widget createMainContent() {
        mainContent.setCellPadding(3);
        mainContent.setWidth("100%");
        mainContent.setCellSpacing(0);
        mainContent.addStyleName("add_new_entry_main_content_wrapper");
        mainContent.setWidget(0, 0, contentHeader);
        mainContent.getCellFormatter().setStyleName(0, 0, "add_new_entry_main_content_header");

        // sub content
        mainContent.setWidget(1, 0, new HTML("&nbsp;"));
        return mainContent;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setContents(Widget widget) {
        mainContent.setWidget(1, 0, widget);
    }

    @Override
    public ProfileViewMenu getMenu() {
        return this.menu;
    }

    @Override
    public void setHeaderText(String text) {
        contentHeader.setText(text);
    }

    private VerticalPanel createSamplesTablePanel() {

        samplesTable = new SamplesDataTable();
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        samplesTable.addStyleName("gray_border");
        panel.add(samplesTable);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(samplesTable);
        panel.add(tablePager);

        return panel;
    }

    @Override
    public HasEntryDataTable<SampleInfo> getSamplesTable() {
        return this.samplesTable;
    }
}

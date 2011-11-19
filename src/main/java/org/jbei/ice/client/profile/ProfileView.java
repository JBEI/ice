package org.jbei.ice.client.profile;

import java.util.ArrayList;

import org.jbei.ice.client.collection.table.SamplesDataTable;
import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.common.HeaderView;
import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProfileView extends Composite implements ProfilePresenter.Display {

    private EntryDataTable<EntryData> entriesTable;
    private SamplesDataTable samplesTable;

    private final Label contentHeader;
    private CellList<CellEntry> menu;
    private FlexTable mainContent;

    public ProfileView() {
        FlexTable layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);

        contentHeader = new Label("");
        mainContent = new FlexTable();
        createEntriesTablePanel();
        this.createSamplesTablePanel();

        // layout
        layout.setWidget(0, 0, createHeader());
        layout.setWidget(1, 0, createContents());
        layout.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        layout.getCellFormatter().setHeight(1, 0, "100%");
        layout.setWidget(2, 0, createFooter());
    }

    protected Widget createHeader() {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.add(new HeaderView());
        panel.add(new HeaderMenu());
        return panel;
    }

    protected Widget createFooter() {
        return Footer.getInstance();
    }

    protected Widget createContents() {
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
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("collection_menu_table");
        layout.setHTML(0, 0, "User Profile");
        layout.getCellFormatter().setStyleName(0, 0, "collections_menu_header");

        // cell to render value
        menu = new CellList<CellEntry>(new ProfileMenuCell());
        layout.setWidget(1, 0, menu);
        return layout;
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
    public CellList<CellEntry> getMenu() {
        return this.menu;
    }

    public void setHeaderText(String text) {
        contentHeader.setText(text);
    }

    private VerticalPanel createEntriesTablePanel() {

        entriesTable = new EntryDataTable<EntryData>() {

            @Override
            protected ArrayList<DataTableColumn<?>> createColumns() {

                ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();
                columns.add(super.addTypeColumn(true));
                columns.add(super.addPartIdColumn(true));
                super.addNameColumn();
                super.addSummaryColumn();
                super.addStatusColumn();
                super.addHasAttachmentColumn();
                super.addHasSampleColumn();
                super.addHasSequenceColumn();
                super.addCreatedColumn();

                return columns;
            }
        };

        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        entriesTable.addStyleName("gray_border");
        panel.add(entriesTable);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(entriesTable);
        panel.add(tablePager);

        return panel;
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

    @Override
    public EntryDataTable<EntryData> getEntryDataTable() {
        return this.entriesTable;
    }
}

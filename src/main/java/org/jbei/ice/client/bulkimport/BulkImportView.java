package org.jbei.ice.client.bulkimport;

import org.jbei.ice.client.bulkimport.model.BulkImportMenu;
import org.jbei.ice.client.bulkimport.sheet.StrainSheet;
import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class BulkImportView extends AbstractLayout implements IBulkImportView {

    private CellList<ImportType> menu;
    private BulkImportMenu draftsMenu;
    private Label contentHeader;
    private TextBox draftInput;
    private Button draftSave;
    private Button submit;
    private Button reset;
    private FlexTable mainContent;

    public BulkImportView() {
    }

    @Override
    protected void initComponents() {
        draftsMenu = new BulkImportMenu();
        menu = new CellList<ImportType>(new ImportListCell());

        draftInput = new TextBox();
        draftInput.setStylePrimaryName("input_box");
        draftInput.setText("Enter Name");

        draftSave = new Button("Save Draft");
        reset = new Button("Reset");
        submit = new Button("Submit");
    }

    @Override
    protected Widget createContents() {
        FlexTable contentTable = new FlexTable();
        contentTable.setWidth("100%");
        contentTable.setWidget(0, 0, createMenu());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        contentTable.setWidget(1, 0, createDraftsMenu());
        contentTable.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);

        // TODO : middle sliver goes here
        contentTable.getFlexCellFormatter().setRowSpan(0, 1, 2);
        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        return contentTable;
    }

    protected Widget createMenu() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(3);
        layout.setCellSpacing(0);
        layout.addStyleName("collection_menu_table");
        layout.setHTML(0, 0, "Select A Type");
        layout.getCellFormatter().setStyleName(0, 0, "collections_menu_header");

        // cell to render value
        layout.setWidget(1, 0, menu);
        return layout;
    }

    protected Widget createDraftsMenu() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(3);
        layout.setCellSpacing(0);
        layout.addStyleName("collection_menu_table");
        layout.setHTML(0, 0, "Saved Drafts");
        layout.getCellFormatter().setStyleName(0, 0, "collections_menu_header");
        layout.setWidget(1, 0, draftsMenu);

        return layout;
    }

    protected Widget createMainContent() {
        mainContent = new FlexTable(); // wrapper
        mainContent.setCellPadding(3);
        mainContent.setCellSpacing(0);

        mainContent.addStyleName("bulk_import_main_content_wrapper");

        // content header (label)
        contentHeader = new Label("Bulk Import");
        contentHeader.setStyleName("bulk_import_main_content_header");
        mainContent.setWidget(0, 0, contentHeader);

        mainContent.setWidget(1, 0, createContentMenu());

        // sub content
        mainContent.setWidget(2, 0, new StrainSheet());
        return mainContent;
    }

    private Widget createContentMenu() {

        String html = "<span style=\"width: 50%; text-align: left; display: inline-block;\"><span id=\"input_draft_name\"></span><span id=\"btn_save_draft\"></span></span>"
                + "<span style=\"width: 47%; text-align: right; display: inline-block;\"><span id=\"btn_reset\"></span><span id=\"btn_submit\"></span></span>";
        HTMLPanel panel = new HTMLPanel(html);

        panel.add(draftInput, "input_draft_name");
        panel.add(draftSave, "btn_save_draft");
        panel.add(reset, "btn_reset");
        panel.add(submit, "btn_submit");

        return panel;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public Button getSaveDraftButton() {
        return this.draftSave;
    }

    @Override
    public CellList<ImportType> getMenu() {
        return this.menu;
    }

    @Override
    public BulkImportMenu getDraftMenu() {
        return this.draftsMenu;
    }

    @Override
    public void setHeader(String header) {
        contentHeader.setText(header);
    }

    @Override
    public void setSheet(Widget sheet) {
        this.mainContent.setWidget(2, 0, sheet);
    }
}

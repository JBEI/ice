package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.client.bulkimport.ImportType;
import org.jbei.ice.client.bulkimport.panel.SheetHeaderPanel;
import org.jbei.ice.client.bulkimport.sheet.Sheet;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Wrapper around the header container submit buttons etc
 * and the actual sheet
 * 
 * @author Hector Plahar
 */
public class NewBulkInput extends Composite {

    private final SheetHeaderPanel panel;
    private final VerticalPanel layout;
    private final Sheet sheet;
    private final ImportType type;

    public NewBulkInput(ImportType type, Sheet sheet) {
        layout = new VerticalPanel();
        initWidget(layout);
        this.sheet = sheet;
        this.type = type;

        panel = new SheetHeaderPanel();
        layout.add(panel);
        layout.add(sheet);
    }

    public SheetHeaderPanel getSheetHeaderPanel() {
        return this.panel;
    }

    public Sheet getSheet() {
        return this.sheet;
    }

    public ImportType getImportType() {
        return this.type;
    }
}

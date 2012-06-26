package org.jbei.ice.client.bulkimport.model;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jbei.ice.client.bulkimport.sheet.Sheet;
import org.jbei.ice.shared.EntryAddType;

/**
 * Wrapper around the header container submit buttons etc
 * and the actual sheet
 *
 * @author Hector Plahar
 */
public class NewBulkInput extends Composite {

    private long id;
    private final Sheet sheet;
    private final EntryAddType type;
    private String name;

    public NewBulkInput(EntryAddType type, Sheet sheet) {
        VerticalPanel layout = new VerticalPanel();
        initWidget(layout);
        this.sheet = sheet;
        this.type = type;

        layout.add(sheet);
    }

    public Sheet getSheet() {
        return this.sheet;
    }

    public EntryAddType getImportType() {
        return this.type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

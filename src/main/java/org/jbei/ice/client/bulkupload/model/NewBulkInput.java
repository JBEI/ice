package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.client.bulkupload.sheet.Sheet;
import org.jbei.ice.client.bulkupload.widget.SampleSelectionWidget;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

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
    private final SampleSelectionWidget sampleSelectionWidget;

    public NewBulkInput(EntryAddType type, Sheet sheet) {
        VerticalPanel layout = new VerticalPanel();
        initWidget(layout);
        this.sheet = sheet;
        this.type = type;
        this.sampleSelectionWidget = new SampleSelectionWidget();
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

    public void setSampleLocation(SampleLocation sampleLocation) {
        sampleSelectionWidget.setLocation(sampleLocation);
        if (sampleLocation != null)
            sheet.setSampleSelection(type, sampleSelectionWidget);
    }

    public SampleSelectionWidget getSampleSelectionWidget() {
        return sampleSelectionWidget;
    }
}

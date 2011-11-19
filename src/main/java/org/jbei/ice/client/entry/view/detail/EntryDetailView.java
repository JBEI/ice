package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.ParameterInfo;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Base view for the different types of entries
 * 
 * @author Hector Plahar
 * 
 * @param <T>
 *            Specific type of entry info to be shown by the view
 */
public abstract class EntryDetailView<T extends EntryInfo> extends Composite {

    private final FlexTable table;
    protected final T info;
    private int currentRow = 0;
    private int currentCol = 0;

    public EntryDetailView(T info) {

        this.info = info;
        table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(3);
        table.setCellSpacing(1);
        initWidget(table);

        addCommonShortFields();
        addShortFieldValues();

        addLongFields();
        addCommonLongFields();

        // parameters
        showParameters();

        // sequence
        createSequenceView();

        // notes
        createNotesView();
    }

    /**
     * Abstract class to be implemented by all specializations
     * to show their specific (non-generic) fields.
     */
    protected abstract void addShortFieldValues();

    protected abstract void addLongFields();

    public T getInfo() {
        return info;
    }

    protected void addCommonShortFields() {
        addShortField("Part ID", info.getPartId(), ValueType.SHORT_TEXT);
        addShortField("Owner", info.getOwner(), ValueType.SHORT_TEXT);

        addShortField("Name", info.getName(), ValueType.SHORT_TEXT);
        addShortField("Alias", info.getAlias(), ValueType.SHORT_TEXT);

        addShortField("Creator", info.getCreator(), ValueType.SHORT_TEXT);
        addShortField("Principal Investigator", info.getPrincipalInvestigator(),
            ValueType.SHORT_TEXT);

        addShortField("Created", DateUtilities.formatDate(info.getCreationTime()), ValueType.DATE);
        addShortField("Funding Source", info.getFundingSource(), ValueType.SHORT_TEXT);

        addShortField("Status", info.getStatus(), ValueType.SHORT_TEXT);
        addShortField("Bio Safety Level", Integer.toString(info.getBioSafetyLevel()),
            ValueType.SHORT_TEXT);

        addShortField("Modified", DateUtilities.formatDate(info.getModificationTime()),
            ValueType.SHORT_TEXT);
    }

    protected void addCommonLongFields() {
        addLongField("Links", info.getLinks());
        addLongField("Keywords", info.getKeywords());
        addLongField("Summary", info.getShortDescription());
        addLongField("References", info.getReferences());
        addLongField("Intellectual Property", info.getIntellectualProperty());
    }

    protected void showParameters() {
        FlexTable parameters = new FlexTable();
        parameters.setCellPadding(0);
        parameters.setCellSpacing(0);
        parameters.setWidth("100%");

        int row = 0;
        parameters.setWidget(row, 0, new Label("Parameters"));
        parameters.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");
        parameters.getFlexCellFormatter().setColSpan(row, 0, 6);

        row += 1;
        parameters.setWidget(row, 0, new Label(""));
        parameters.getFlexCellFormatter().setHeight(row, 0, "10px");
        parameters.getFlexCellFormatter().setColSpan(row, 0, 6);

        if (info.getParameters() != null) {
            int col = 6;
            for (ParameterInfo paramInfo : info.getParameters()) {

                if (col > 5) {
                    col = 0;
                    row += 1;
                }

                parameters.setWidget(row, col, new HTML("<b>" + paramInfo.getName() + "</b>"));
                parameters.getFlexCellFormatter().setWidth(row, col, "50px");
                col += 1;
                parameters.setWidget(row, col, new Label(paramInfo.getValue()));
                parameters.getFlexCellFormatter().setWidth(row, col, "220px");
                col += 1;
            }
        }

        table.setWidget(currentRow, 0, parameters);
        table.getFlexCellFormatter().setColSpan(currentRow, 0, 4);
        currentRow += 1;
    }

    protected void createSequenceView() {
        FlexTable sequence = new FlexTable();
        sequence.setCellPadding(0);
        sequence.setCellSpacing(0);
        sequence.setWidth("100%");

        int row = 0;
        sequence.setWidget(row, 0, new Label("Sequence"));
        sequence.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");
        sequence.getFlexCellFormatter().setColSpan(row, 0, 6);

        row += 1;
        sequence.setWidget(row, 0, new Label(""));
        sequence.getFlexCellFormatter().setHeight(row, 0, "10px");
        sequence.getFlexCellFormatter().setColSpan(row, 0, 6);

        row += 1;
        // check if there is a sequence 
        if (info.isHasSequence()) {
            Flash.Parameters param = new Flash.Parameters();
            param.setEntryId(info.getRecordId());
            param.setSessiondId(AppController.sessionId);
            param.setSwfPath("static/vv/VectorViewer.swf");
            sequence.setWidget(row, 0, new Flash(param));
            sequence.getFlexCellFormatter().setHeight(row, 0, "600px");
        } else {
            sequence.setWidget(row, 0, new HTML("links"));
            // TODO : add links to upload or create a new one in vector editor
            // TODO : the latter action opens vector editor in a new window
            /* 
             * WebComponent flashComponent = new WebComponent("flashComponent");

            String entryRecordId = parameters.getString("entryId");
            String accountSessionId = IceSession.get().getSessionKey();

            ResourceReference veResourceReference = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.VE_RESOURCE_LOCATION + "VectorEditor.swf?entryId=" + entryRecordId
                        + "&sessionId=" + accountSessionId);

            flashComponent.add(new SimpleAttributeModifier("src", urlFor(veResourceReference)));
            flashComponent.add(new SimpleAttributeModifier("quality", "high"));
            flashComponent.add(new SimpleAttributeModifier("bgcolor", "#869ca7"));
            flashComponent.add(new SimpleAttributeModifier("width", "100%"));
            flashComponent.add(new SimpleAttributeModifier("height", "100%"));
            flashComponent.add(new SimpleAttributeModifier("name", "VectorEditor"));
            flashComponent.add(new SimpleAttributeModifier("align", "middle"));
            flashComponent.add(new SimpleAttributeModifier("play", "true"));
            flashComponent.add(new SimpleAttributeModifier("loop", "false"));
            flashComponent.add(new SimpleAttributeModifier("type", "application/x-shockwave-flash"));
            flashComponent.add(new SimpleAttributeModifier("pluginspage",
                "http://www.adobe.com/go/getflashplayer"));
             */
        }

        table.setWidget(currentRow, 0, sequence);
        table.getFlexCellFormatter().setColSpan(currentRow, 0, 4);
        currentRow += 1;
    }

    protected void createNotesView() {
        FlexTable notes = new FlexTable();
        notes.setCellPadding(0);
        notes.setCellSpacing(0);
        notes.setWidth("100%");

        int row = 0;
        notes.setWidget(row, 0, new Label("Notes"));
        notes.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");

        row += 1;
        notes.setWidget(row, 0, new Label(""));
        notes.getFlexCellFormatter().setHeight(row, 0, "10px");

        row += 1;
        notes.setWidget(row, 0, new Label(info.getLongDescription()));

        table.setWidget(currentRow, 0, notes);
        table.getFlexCellFormatter().setColSpan(currentRow, 0, 4);
        currentRow += 1;
    }

    // TODO : this is currently a bit expensive
    public void switchToEditMode() {
        int rowCount = table.getRowCount();
        for (int row = 0; row < rowCount; row += 1) {
            int cellCount = table.getCellCount(row);
            for (int col = 0; col < cellCount; col += 1) {
                if (col % 2 == 0)
                    continue;

                String text = table.getText(row, col);
                TextBox textBox = new TextBox();
                textBox.setText(text);
                table.setWidget(row, col, textBox);

                //                ValueCell cell = (ValueCell) table.getWidget(row, col);
                //                switch (cell.getType()) {
                //                case DATE:
                //                    DatePicker datePicker = new DatePicker();
                //                    table.setWidget(row, col, datePicker);
                //                    break;
                //
                //                default:
                //                case SHORT_TEXT:
                //                    TextBox textBox = new TextBox();
                //                    textBox.setText(cell.getValue());
                //                    table.setWidget(row, col, textBox);
                //                    break;
            }
        }
    }

    // adds a field to the current table
    protected void addShortField(String labelString, String value, ValueType valueType) {
        if (currentCol >= 4) { // TODO : maybe add a parameter that determines whether to show on next row or not
            currentCol = 0;
            currentRow += 1;
        }

        HTML label = new HTML("<b>" + labelString + "</b>", true);
        table.setWidget(currentRow, currentCol, label);
        table.getFlexCellFormatter().setWidth(currentRow, currentCol, "170px");
        currentCol += 1;
        Label valueLabel = new Label(value);
        //        ValueCell cell = new ValueCell(value, valueType);
        table.setWidget(currentRow, currentCol, valueLabel); // TODO add a style to put space after this or something
        currentCol += 1;
    }

    protected void addLongField(String labelString, String value) {
        currentRow += 1;

        HTML label = new HTML("<b>" + labelString + "</b>", true);
        table.setWidget(currentRow, 0, label);
        table.getFlexCellFormatter().setWidth(currentRow, 0, "170px");

        table.setWidget(currentRow, 1, new Label(value));
        table.getFlexCellFormatter().setColSpan(currentRow, 1, 3);

        currentCol = 0;
        currentRow += 1;
    }

    // a field that spans 4 cols (both name and value)
    protected void addLongField() {
    }

    private Hyperlink createProfileLink(String email, String name) {
        Hyperlink link = new Hyperlink(name, Page.PROFILE.getLink() + ";id=" + email);
        return link;
    }

    //
    // inner classes
    //
    protected class ValueCell extends Label {
        private final String value;
        private final ValueType type;

        public ValueCell(String value, ValueType type) {
            super(value);
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return this.value;
        }

        public ValueType getType() {
            return this.type;
        }
    }

    /**
     * Represents the different type of field values in order
     * to accurately map them to their edit components. e.g. a DATE
     * ValueType will map to a DatePicker
     */
    protected enum ValueType {
        DATE, MARKER_AUTO_COMPLETE, SHORT_TEXT, LONG_TEXT, SELECTION
    }
}

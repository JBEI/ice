package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.ParameterInfo;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

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
        table.setCellSpacing(0);
        table.setStyleName("pad-6");
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

    /**
     * Adds, to the layout, fields that are short and common to all entry types
     */
    protected void addCommonShortFields() {
        addShortField("Part ID", info.getPartId(), ValueType.SHORT_TEXT);
        Widget ownerLink = this.createProfileLink(info.getOwnerEmail(), info.getOwner());
        addShortField("Owner", ownerLink, ValueType.SHORT_TEXT);

        addShortField("Name", info.getName(), ValueType.SHORT_TEXT);
        addShortField("Alias", info.getAlias(), ValueType.SHORT_TEXT);

        Widget link = this.createProfileLink(info.getCreatorEmail(), info.getCreator());
        addShortField("Creator", link, ValueType.SHORT_TEXT);
        addShortField("Principal Investigator", info.getPrincipalInvestigator(),
            ValueType.SHORT_TEXT);

        addShortField("Created", DateUtilities.formatDate(info.getCreationTime()), ValueType.DATE);
        addShortField("Funding Source", info.getFundingSource(), ValueType.SHORT_TEXT);

        String statusString = StatusType.displayValueOf(info.getStatus());
        if (statusString.equals(""))
            statusString = info.getStatus();
        addShortField("Status", statusString, ValueType.SHORT_TEXT);
        addShortField("Bio Safety Level", Integer.toString(info.getBioSafetyLevel()),
            ValueType.SHORT_TEXT);

        addShortField("Modified", DateUtilities.formatDate(info.getModificationTime()),
            ValueType.SHORT_TEXT);
    }

    /**
     * Adds fields that are long (span an entire row)
     */
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

                parameters.setWidget(row, col, new HTML("<b class=\"font-80em color_444\">"
                        + paramInfo.getName() + "</b>"));
                parameters.getFlexCellFormatter().setWidth(row, col, "170px");
                col += 1;
                parameters.setWidget(row, col, new Label(paramInfo.getValue()));
                //                parameters.getFlexCellFormatter().setWidth(row, col, "220px");
                col += 1;
            }
        }

        table.setWidget(currentRow, 0, parameters);
        table.getFlexCellFormatter().setColSpan(currentRow, 0, 4);
        currentRow += 1;
    }

    protected void showSamples() {
        FlexTable sampleLayout = new FlexTable();
        sampleLayout.setCellPadding(0);
        sampleLayout.setCellSpacing(0);
        sampleLayout.setWidth("100%");

        int row = 0;
        sampleLayout.setWidget(row, 0, new Label("Samples"));
        sampleLayout.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");
        sampleLayout.getFlexCellFormatter().setColSpan(row, 0, 6);

        row += 1;
        sampleLayout.setWidget(row, 0, new Label(""));
        sampleLayout.getFlexCellFormatter().setHeight(row, 0, "10px");
        sampleLayout.getFlexCellFormatter().setColSpan(row, 0, 6);

        // TODO : sample display
        if (info.getParameters() != null) {
            int col = 6;
            for (ParameterInfo paramInfo : info.getParameters()) {

                if (col > 5) {
                    col = 0;
                    row += 1;
                }

                sampleLayout.setWidget(row, col, new HTML("<b>" + paramInfo.getName() + "</b>"));
                sampleLayout.getFlexCellFormatter().setWidth(row, col, "50px");
                col += 1;
                sampleLayout.setWidget(row, col, new Label(paramInfo.getValue()));
                sampleLayout.getFlexCellFormatter().setWidth(row, col, "220px");
                col += 1;
            }
        }

        table.setWidget(currentRow, 0, sampleLayout);

        table.getFlexCellFormatter().setColSpan(currentRow, 0, 4);
        currentRow += 1;
    }

    protected void createSequenceView() {
        FlexTable sequence = new FlexTable();
        sequence.setCellPadding(0);
        sequence.setCellSpacing(0);
        sequence.setWidth("100%");

        int row = 0;
        sequence.setWidget(row, 0, createSequenceHeader());
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
            param.setSwfPath("vv/VectorViewer.swf");
            sequence.setWidget(row, 0, new Flash(param));
            sequence.getFlexCellFormatter().setHeight(row, 0, "600px");
        } else {
            sequence.setHTML(row, 0, "<span class=\"font-80em\">No sequence provided.</span>");
        }

        table.setWidget(currentRow, 0, sequence);
        table.getFlexCellFormatter().setColSpan(currentRow, 0, 4);
        currentRow += 1;
    }

    private Widget createSequenceHeader() {
        HTMLPanel panel = new HTMLPanel("<span style=\"color: #233559; "
                + "font-weight: bold; font-style: italic; font-size: 0.80em;\">"
                + "SEQUENCE</span><div style=\"float: right\"><span id=\"sequence_link\"></span>"
                + "<span style=\"color: #262626; font-size: 0.75em;\">|</span>"
                + " <span id=\"sequence_options\"></span></div>");

        panel.setStyleName("entry_sequence_sub_header");

        final VectorEditorDialog dialog = new VectorEditorDialog(info.getName());
        Flash.Parameters param = new Flash.Parameters();
        param.setEntryId(info.getRecordId());
        param.setSessiondId(AppController.sessionId);
        param.setSwfPath("ve/VectorEditor.swf");

        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setHeight("100%");
        table.setWidget(0, 0, new Flash(param));
        table.getFlexCellFormatter().setHeight(0, 0, "100%");
        dialog.setWidget(table);

        if (info.isHasSequence()) {
            // delete, open in vector editor, download
            Label label = dialog.getLabel("Open");
            label.setStyleName("open_sequence_sub_link");
            panel.add(label, "sequence_link");

            SequenceFileDownload download = new SequenceFileDownload(info.getId());
            Widget widget = download.asWidget();
            widget.addStyleName("display-inline");
            panel.add(download.asWidget(), "sequence_options");

            // TODO : delete
        } else {
            Label label = dialog.getLabel("Create New");
            label.setStyleName("open_sequence_sub_link");
            panel.add(label, "sequence_link");

            // upload sequence
            SequenceFileUpload upload = new SequenceFileUpload(info.getId());
            Widget widget = upload.asWidget();
            widget.addStyleName("display-inline");
            panel.add(upload.asWidget(), "sequence_options");
            //            upload.getSelectionModel().addSelectionChangeHandler(new Sele)
        }
        return panel;
    }

    protected void createNotesView() {
        FlexTable notes = new FlexTable();
        notes.setCellPadding(0);
        notes.setCellSpacing(0);
        notes.setWidth("100%");

        int row = 0;
        notes.setWidget(row, 0, new Label("Notes")); // TODO : parse this
        notes.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");

        row += 1;
        notes.setWidget(row, 0, new Label(""));
        notes.getFlexCellFormatter().setHeight(row, 0, "10px");

        row += 1;
        String description = (info.getLongDescription() == null) ? "" : info.getLongDescription();
        notes.setHTML(row, 0, "<span class=\"font-80em\">" + description + "</span>");

        table.setWidget(currentRow, 0, notes);
        table.getFlexCellFormatter().setColSpan(currentRow, 0, 4);
        currentRow += 1;
    }

    // adds a field to the current table
    protected void addShortField(String labelString, String value, ValueType valueType) {
        if (currentCol >= 4) { // TODO : maybe add a parameter that determines whether to show on next row or not
            currentCol = 0;
            currentRow += 1;
        }

        table.setHTML(currentRow, currentCol, "<b class=\"font-80em color_444\">" + labelString
                + "</b>");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, currentCol,
            HasAlignment.ALIGN_TOP);
        table.getFlexCellFormatter().setWidth(currentRow, currentCol, "170px");
        currentCol += 1;
        //        ValueCell cell = new ValueCell(value, valueType);
        if (value == null)
            value = "";
        table.setHTML(currentRow, currentCol, "<span class=\"font-80em\">" + value + "</span>");
        currentCol += 1;
    }

    // adds a field to the current table
    protected void addShortField(String labelString, Widget value, ValueType valueType) {
        if (currentCol >= 4) { // TODO : maybe add a parameter that determines whether to show on next row or not
            currentCol = 0;
            currentRow += 1;
        }

        table.setHTML(currentRow, currentCol, "<b class=\"font-80em color_444\">" + labelString
                + "</b>");
        table.getFlexCellFormatter().setWidth(currentRow, currentCol, "170px");
        currentCol += 1;
        //        ValueCell cell = new ValueCell(value, valueType);

        value.addStyleName("font-80em");
        table.setWidget(currentRow, currentCol, value); // TODO add a style to put space after this or something
        currentCol += 1;
    }

    protected void addLongField(String labelString, String value) {
        currentRow += 1;

        table.setHTML(currentRow, 0, "<b class=\"font-80em color_444\">" + labelString + "</b>");
        table.getFlexCellFormatter().setWidth(currentRow, 0, "170px");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, 0, HasAlignment.ALIGN_TOP);

        if (value == null)
            value = "";
        table.setHTML(currentRow, 1, "<span class=\"font-80em\">" + value + "</span>");
        table.getFlexCellFormatter().setColSpan(currentRow, 1, 3);

        currentCol = 0;
        currentRow += 1;
    }

    private Widget createProfileLink(String email, String name) {
        if (email == null || email.isEmpty())
            return new HTML("<i>" + name + "</i>");
        return new Hyperlink(name, Page.PROFILE.getLink() + ";id=" + email);
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

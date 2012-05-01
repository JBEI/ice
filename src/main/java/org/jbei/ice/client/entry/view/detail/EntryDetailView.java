package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.ParameterInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
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

    private SequenceViewPanel sequencePanel;

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

    public SequenceViewPanel getSequencePanel() {
        return this.sequencePanel;
    }

    /**
     * Abstract class to be implemented by all specializations
     * to show their specific (non-generic) fields.
     */
    protected abstract void addShortFieldValues();

    protected abstract void addLongFields();

    /**
     * Adds, to the layout, fields that are short and common to all entry types
     */
    protected void addCommonShortFields() {
        addShortField("Part ID", info.getPartId());
        Widget ownerLink = this.createProfileLink(info.getOwnerEmail(), info.getOwner());
        addShortField("Owner", ownerLink);

        addShortField("Name", info.getName());
        addShortField("Alias", info.getAlias());

        Widget link = this.createProfileLink(info.getCreatorEmail(), info.getCreator());
        addShortField("Creator", link);
        addShortField("Principal Investigator", info.getPrincipalInvestigator());

        addShortField("Created", DateUtilities.formatDate(info.getCreationTime()));
        addShortField("Funding Source", info.getFundingSource());

        String statusString = StatusType.displayValueOf(info.getStatus());
        if (statusString.equals(""))
            statusString = info.getStatus();
        addShortField("Status", statusString);
        addShortField("Bio Safety Level", Integer.toString(info.getBioSafetyLevel()));

        addShortField("Modified", DateUtilities.formatDate(info.getModificationTime()));
    }

    /**
     * Adds fields that are long (span an entire row)
     */
    protected void addCommonLongFields() {
        addLongField("Links", info.getLinkifiedLinks());
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
                parameters.setWidget(row, col,
                    new HTML("<span class=\"font-80em\">" + paramInfo.getValue() + "</span>"));
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

    public void createSequenceView() {
        sequencePanel = new SequenceViewPanel(this.info);
        table.setWidget(currentRow, 0, sequencePanel);
        table.getFlexCellFormatter().setColSpan(currentRow, 0, 4);
        currentRow += 1;
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
    protected void addShortField(String labelString, String value) {
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
        if (value == null)
            value = "";
        table.setHTML(currentRow, currentCol, "<span class=\"font-80em\">" + value + "</span>");
        table.getFlexCellFormatter().setStyleName(currentRow, currentCol, "min_width_170");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, currentCol,
            HasAlignment.ALIGN_TOP);

        currentCol += 1;
    }

    // adds a field to the current table
    protected void addShortField(String labelString, Widget value) {
        if (currentCol >= 4) {
            currentCol = 0;
            currentRow += 1;
        }

        table.setHTML(currentRow, currentCol, "<b class=\"font-80em color_444\">" + labelString
                + "</b>");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, currentCol,
            HasAlignment.ALIGN_TOP);
        table.getFlexCellFormatter().setWidth(currentRow, currentCol, "170px");

        currentCol += 1;

        value.addStyleName("font-80em");
        table.setWidget(currentRow, currentCol, value);
        table.getFlexCellFormatter().setStyleName(currentRow, currentCol, "min_width_170");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, currentCol,
            HasAlignment.ALIGN_TOP);

        currentCol += 1;
    }

    protected void addLongField(String labelString, String value) {
        currentRow += 1;

        table.setHTML(currentRow, 0, "<b class=\"font-80em color_444\">" + labelString + "</b>");
        table.getFlexCellFormatter().setWidth(currentRow, 0, "170px");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, 0, HasAlignment.ALIGN_TOP);

        if (value == null)
            value = "";

        int width = Window.getClientWidth() - 530;
        if (width <= 0)
            width = 100;

        table.setHTML(currentRow, 1,
            "<span class=\"font-80em\" style=\"display: block; word-wrap: break-word; width: "
                    + width + "px\">" + value + "</span>");
        table.getFlexCellFormatter().setColSpan(currentRow, 1, 3);

        currentCol = 0;
        currentRow += 1;
    }

    private Widget createProfileLink(String email, String name) {
        if (email == null || email.isEmpty())
            return new HTML("<i>" + name + "</i>");
        return new Hyperlink(name, Page.PROFILE.getLink() + ";id=" + email);
    }
}

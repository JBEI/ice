package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.ParameterInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
 * @param <T> Specific type of entry info to be shown by the view
 * @author Hector Plahar
 */
public abstract class EntryInfoView<T extends EntryInfo> extends Composite {

    private final FlexTable table;
    protected T info;
    private int currentRow = 0;
    private int currentCol = 0;

    private SequenceViewPanel sequencePanel;

    public EntryInfoView(T info) {
        this.info = info;

        table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(3);
        table.setCellSpacing(0);
        table.setStyleName("pad-6");
        initWidget(table);

        initView();
    }

    protected void initView() {
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

    public void setInfo(T info) {
        this.info = info;
        table.clear();
        currentCol = 0;
        currentRow = 0;
        initView();
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
        addShortField("Name", info.getName());
        addShortField("Alias", info.getAlias());

        Widget creator;
        if (info.getCreatorId() > 0)
            creator = this.createProfileLink(info.getCreatorId() + "", info.getCreator());
        else
            creator = new HTML("<i>" + info.getCreator() + "</i>");
        addShortField("Creator", creator);
        addShortField("Principal Investigator", info.getPrincipalInvestigator());
        addShortField("Funding Source", info.getFundingSource());

        String statusString = StatusType.displayValueOf(info.getStatus());
        if (statusString.equals(""))
            statusString = info.getStatus();
        addShortField("Status", statusString);

        String bioSafety = (info.getBioSafetyLevel() == null || info.getBioSafetyLevel() <= 0) ? "" :
                BioSafetyOption.enumValue(info.getBioSafetyLevel()).getValue();
        addShortField("Bio Safety Level", bioSafety);
        addShortField("Modified", DateUtilities.formatDate(info.getModificationTime()));
    }

    /**
     * Adds fields that are long (span an entire row)
     */
    protected void addCommonLongFields() {
        addLongField("Links", info.getLinkifiedLinks());
        addLongField("Keywords", info.getKeywords());
        addLongField("Summary", info.getLinkifiedShortDescription());
        addLongField("References", info.getReferences());
        addLongField("Intellectual Property", info.getIntellectualProperty());
    }

    protected void showParameters() {
        if (info.getParameters() == null || info.getParameters().isEmpty())
            return;

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
            parameters.setWidget(row, col, new HTML("<span class=\"font-80em\">" + paramInfo.getValue() + "</span>"));
            col += 1;
        }

        table.setWidget(currentRow, 0, parameters);
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
        notes.setStyleName("margin-top-10");
        notes.setWidth("100%");

        int row = 0;
        notes.setWidget(row, 0, new Label("Notes")); // TODO : parse this
        notes.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");

        row += 1;
        notes.setWidget(row, 0, new Label(""));
        notes.getFlexCellFormatter().setHeight(row, 0, "10px");

        row += 1;
        String description = (info.getLongDescription() == null) ? "" : info.getLongDescription();
        int width = Window.getClientWidth() - 680;
        if (width <= 0)
            width = 200;

        String notesHtml = description.replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp");
        notes.setHTML(row, 0,
                      "<span class=\"font-80em\" style=\"display: block; word-wrap: break-word; width: "
                              + width + "px\">" + SafeHtmlUtils.fromSafeConstant(notesHtml).asString() + "</span>");

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

        table.setHTML(currentRow, currentCol, labelString);
        table.getCellFormatter().setStyleName(currentRow, currentCol, "font-80em");
        table.getCellFormatter().addStyleName(currentRow, currentCol, "color_444");
        table.getCellFormatter().addStyleName(currentRow, currentCol, "font-bold");
        table.getCellFormatter().addStyleName(currentRow, currentCol, "line_height_18");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, currentCol, HasAlignment.ALIGN_TOP);
        table.getFlexCellFormatter().setWidth(currentRow, currentCol, "170px");
        currentCol += 1;
        if (value == null)
            value = "";
        table.setHTML(currentRow, currentCol, value);
        table.getCellFormatter().setStyleName(currentRow, currentCol, "font-80em");
        table.getCellFormatter().addStyleName(currentRow, currentCol, "line_height_18");
        table.getFlexCellFormatter().addStyleName(currentRow, currentCol, "min_width_150");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, currentCol, HasAlignment.ALIGN_TOP);

        currentCol += 1;
    }

    // adds a field to the current table
    protected void addShortField(String labelString, Widget value) {
        if (currentCol >= 4) {
            currentCol = 0;
            currentRow += 1;
        }

        table.setHTML(currentRow, currentCol, labelString);
        table.getCellFormatter().setStyleName(currentRow, currentCol, "font-80em");
        table.getCellFormatter().addStyleName(currentRow, currentCol, "color_444");
        table.getCellFormatter().addStyleName(currentRow, currentCol, "font-bold");
        table.getCellFormatter().addStyleName(currentRow, currentCol, "line_height_18");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, currentCol, HasAlignment.ALIGN_TOP);
        table.getFlexCellFormatter().setWidth(currentRow, currentCol, "170px");

        currentCol += 1;

        value.addStyleName("font-80em");
        table.setWidget(currentRow, currentCol, value);
        table.getFlexCellFormatter().setStyleName(currentRow, currentCol, "min_width_150");
        table.getCellFormatter().addStyleName(currentRow, currentCol, "line_height_18");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, currentCol, HasAlignment.ALIGN_TOP);

        currentCol += 1;
    }

    protected void addLongField(String labelString, String value) {
        currentRow += 1;

        table.setHTML(currentRow, 0, labelString);
        table.getCellFormatter().setStyleName(currentRow, 0, "font-80em");
        table.getCellFormatter().addStyleName(currentRow, 0, "color_444");
        table.getCellFormatter().addStyleName(currentRow, 0, "font-bold");
        table.getCellFormatter().addStyleName(currentRow, 0, "line_height_18");

        table.getFlexCellFormatter().setWidth(currentRow, 0, "170px");
        table.getFlexCellFormatter().setVerticalAlignment(currentRow, 0, HasAlignment.ALIGN_TOP);

        if (value == null)
            value = "";

        int width = Window.getClientWidth() - 680;
        if (width <= 0)
            width = 200;

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

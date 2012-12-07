package org.jbei.ice.client.common.header;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Page;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.dto.entry.EntryType;
import org.jbei.ice.shared.dto.search.BlastProgram;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Options widget for search
 *
 * @author Hector Plahar
 */
public class AdvancedSearchWidget extends Composite {

    private final EntryTypeFilterWidget entryTypes;     // entry type filter
    private final BooleanAssociationEntryWidget entryWidget;
    private final Button runSearch;
    private final HTML reset;
    private final FlexTable panel;
    private final SearchCompositeBox searchInput;
    private final ListBox bioSafetyOptions;
    private final TextArea blastSequence;
    private final ListBox blastProgram;
    private final Button mainSearchBtn;
    private HandlerRegistration mainBtnRegistration;

    public AdvancedSearchWidget(SearchCompositeBox searchInput, Button mainSearchBtn) {
        panel = new FlexTable();
        panel.setCellPadding(0);
        panel.setCellSpacing(0);
        initWidget(panel);
        panel.setStyleName("bg_white");
        this.searchInput = searchInput;
        this.mainSearchBtn = mainSearchBtn;

        // init components
        runSearch = new Button("Search");
        reset = new HTML("Reset");
        reset.setStyleName("footer_feedback_widget");
        reset.addStyleName("font-80em");
        entryTypes = new EntryTypeFilterWidget();
        entryWidget = new BooleanAssociationEntryWidget();

        int row = 0;
        panel.setWidget(row, 0, entryTypes);
        panel.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        panel.getFlexCellFormatter().setColSpan(row, 0, 2);
        panel.getFlexCellFormatter().setHeight(row, 0, "23px");

        // blast
        blastSequence = new TextArea();
        blastSequence.setWidth("370px");
        blastSequence.setVisibleLines(9);
        blastSequence.setStyleName("input_box");
        blastSequence.getElement().setAttribute("placeHolder", "Enter Sequence");
        blastProgram = new ListBox();
        blastProgram.setStyleName("pull_down");
        for (BlastProgram program : BlastProgram.values()) {
            blastProgram.addItem(program.getDetails(), program.name());
        }
        HTMLPanel folderOptionsPanel = new HTMLPanel("<div style=\"padding: 8px 8px 8px 5px; "
                                                             + "border-bottom: 0.10em solid #f3f3f3;"
                                                             + " padding-bottom: 15px\"><span class=\"font-80em\""
                                                             + "style=\"letter-spacing:-1px; color:#555;\">"
                                                             + "<b>BLAST</b></span> &nbsp; <span "
                                                             + "id=\"advanced_folders\"></span><br>"
                                                             + "<span id=\"blast_input\"></span>"
                                                             + "</div>");
        folderOptionsPanel.add(blastProgram, "advanced_folders");
        folderOptionsPanel.add(blastSequence, "blast_input");

        row += 1;
        panel.setWidget(row, 0, folderOptionsPanel);
        panel.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        panel.getFlexCellFormatter().setColSpan(row, 0, 2);

        row += 1;
        panel.setWidget(row, 0, entryWidget);
        panel.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        panel.getFlexCellFormatter().setColSpan(row, 0, 2);

        // biosafety
        bioSafetyOptions = new ListBox();
        bioSafetyOptions.setStyleName("pull_down");
        bioSafetyOptions.addItem("Select");
        for (BioSafetyOption option : BioSafetyOption.values())
            bioSafetyOptions.addItem(option.getDisplayName(), option.getValue());
        HTMLPanel htmlPanel = new HTMLPanel("<div style=\"padding: 10px; border-bottom: 0.10em solid #f3f3f3;"
                                                    + " padding-bottom: 15px\"><span class=\"font-80em\"" +
                                                    "style=\"letter-spacing: -1px; margin-right: 20px; color:#555;" +
                                                    "\"><b>"
                                                    + "BIOSAFETY OPTION</b></span><span " +
                                                    "id=\"advanced_biosafety\"></span>"
                                                    + "</div>");
        htmlPanel.add(bioSafetyOptions, "advanced_biosafety");

        row += 1;
        panel.setWidget(row, 0, htmlPanel);
        panel.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        panel.getFlexCellFormatter().setColSpan(row, 0, 2);

        initializeWidget();
        addResetHandler();
    }

    public void parseSearchOptions() {
        String url = Page.QUERY.getLink() + ";";
        url += URL.encodeQueryString(searchInput.getQueryString());

        //blast
        ClientController.blast = blastSequence.getText().trim();
        if (!ClientController.blast.isEmpty()) {
            url += ("&" + BlastProgram.values()[blastProgram.getSelectedIndex()].getName());
        }

        // search entry types
        EntryType[] types = getSearchEntryType();
        if (types != null && types.length != EntryType.values().length) {
            url += "&type=";
            for (EntryType type : types) {
                url += (type.getName() + ",");
            }
            url = url.substring(0, url.length() - 1);
        }

        // check has attachment ext
        String has = "";
        if (entryWidget.isHasAttachmentChecked())
            has += "&attachment";

        if (entryWidget.isHasSampleChecked()) {
            has += "&sample";
        }

        if (entryWidget.isHasSequenceChecked()) {
            has += "&sequence";
        }
        url += has;

        // biosafety level
        if (bioSafetyOptions.getSelectedIndex() != 0) {
            url += ("&biosafety=" + bioSafetyOptions.getSelectedIndex());
        }

        History.newItem(url);
    }

    public void addSearchHandler(final ClickHandler handler) {
        runSearch.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handler.onClick(event);
                parseSearchOptions();
            }
        });
        if (mainBtnRegistration != null)
            mainBtnRegistration = mainSearchBtn.addClickHandler(handler);
    }

    private void addResetHandler() {
        reset.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                reset();
            }
        });
    }

    public void reset() {
        initializeWidget();
        searchInput.reset();
    }

    // meant to be called only once to set the options available for searching
    private void initializeWidget() {
        String html = "<span id=\"run_advanced_search\"></span>&nbsp;<span id=\"reset_advanced_search\"></span>";
        HTMLPanel buttonPanel = new HTMLPanel(html);
        buttonPanel.setStyleName("pad-8");
        runSearch.addStyleName("display-inline");
        reset.addStyleName("display-inline");
        buttonPanel.add(runSearch, "run_advanced_search");
        buttonPanel.add(reset, "reset_advanced_search");
        panel.setWidget(4, 0, buttonPanel);
        panel.getFlexCellFormatter().setColSpan(4, 0, 2);
        panel.getFlexCellFormatter().setHorizontalAlignment(4, 0, HasAlignment.ALIGN_RIGHT);
    }

    public EntryType[] getSearchEntryType() {
        ArrayList<String> selected = entryTypes.getSelected();
        if (selected.size() == EntryType.values().length)
            return null;

        EntryType[] types = new EntryType[selected.size()];
        int i = 0;
        for (String select : selected) {
            EntryType type = EntryType.nameToType(select);
            if (type == null)
                continue;

            types[i] = type;
            i += 1;
        }

        return types;
    }

    //
    // inner classes
    //
    public class EntryTypeFilterWidget extends Composite {

        private final CheckBox allCheck;
        private final CheckBox[] typeChecks;

        public EntryTypeFilterWidget() {
            allCheck = new CheckBox();
            typeChecks = new CheckBox[EntryType.values().length];

            String html =
                    "<div style=\"border-bottom: 0.10em solid #f3f3f3; padding-bottom: 15px\">"
                            + "<span class=\"font-80em;\" style=\"letter-spacing:-1px; color:#555;\">"
                            + "<b>TYPE</b></span> <label "
                            + "style=\"padding-left:10px;\"><span style=\"position:relative; top: 2px; *overflow: "
                            + "hidden\" id=\"all_check\"></span>All</label>";

            for (int i = 0; i < EntryType.values().length; i += 1) {
                typeChecks[i] = new CheckBox();
                html += "<label style=\"padding-left:10px;\"><span style=\"position:relative; top: "
                        + "2px; *overflow: hidden\" id=\"" + EntryType.values()[i].getName()
                        + "_check\"></span>" + EntryType.values()[i].getDisplay() + "</label>";
            }

            html += "</div>";
            HTMLPanel htmlPanel = new HTMLPanel(html);
            htmlPanel.setStyleName("font-80em");
            htmlPanel.addStyleName("pad-3");
            initWidget(htmlPanel);

            htmlPanel.add(allCheck, "all_check");

            for (int i = 0; i < EntryType.values().length; i += 1) {
                htmlPanel.add(typeChecks[i], EntryType.values()[i].getName() + "_check");
            }

            addHandlers();

            // all is pre-selected
            allCheck.setValue(Boolean.TRUE, true);
        }

        protected void addHandlers() {
            allCheck.addValueChangeHandler(new CheckBoxHandler(true));
            CheckBoxHandler handler = new CheckBoxHandler(false);
            for (CheckBox box : typeChecks) {
                box.addValueChangeHandler(handler);
            }
        }

        public ArrayList<String> getSelected() {
            ArrayList<String> selected = new ArrayList<String>();

            if (allCheck.getValue().booleanValue()) {
                for (EntryType type : EntryType.values()) {
                    selected.add(type.getName());
                }
            } else {
                for (int i = 0; i < EntryType.values().length; i += 1) {
                    if (typeChecks[i].getValue().booleanValue()) {
                        selected.add(EntryType.values()[i].getName());
                    }
                }
            }

            return selected;
        }

        private class CheckBoxHandler implements ValueChangeHandler<Boolean> {

            private final boolean isAll;

            public CheckBoxHandler(boolean isAll) {
                this.isAll = isAll;
            }

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (isAll) {
                    for (CheckBox box : typeChecks) {
                        box.setValue(allCheck.getValue(), false);
                    }
                } else {
                    ArrayList<EntryType> selected = new ArrayList<EntryType>();
                    for (int i = 0; i < typeChecks.length; i += 1) {
                        if (typeChecks[i].getValue()) {
                            selected.add(EntryType.values()[i]);
                        }
                    }

                    allCheck.setValue((selected.size() == EntryType.values().length), false);
                }
            }
        }
    }

    // association with entries such as [has sample, has sequence, has attachment]
    private class BooleanAssociationEntryWidget extends Composite {

        private CheckBox hasAttachmentCheck;
        private CheckBox hasSampleCheck;
        private CheckBox hasSequenceCheck;

        public BooleanAssociationEntryWidget() {
            hasAttachmentCheck = new CheckBox();
            hasSampleCheck = new CheckBox();
            hasSequenceCheck = new CheckBox();

            String html = "<div style=\"border-bottom: 0.10em solid #f3f3f3; padding-bottom: 15px\">"
                    + "<label style=\"margin-left:5px;\"><span style=\"position:relative; top: " +
                    "2px; *overflow: hidden\" id=\"attachment_check\"></span> Has Attachment</label>";
            html += "<label style=\" margin-left:30px;\"><span style=\"position:relative; top: " +
                    "2px; *overflow: hidden\" id=\"sample_check\"></span> Has Sample</label>";
            html += "<label style=\" margin-left:30px;\"><span style=\"position:relative; top: " +
                    "2px; *overflow: hidden\" id=\"sequence_check\"></span> Has Sequence</label>";
            html += "</div>";
            HTMLPanel panel = new HTMLPanel(html);
            initWidget(panel);

            panel.add(hasAttachmentCheck, "attachment_check");
            panel.add(hasSampleCheck, "sample_check");
            panel.add(hasSequenceCheck, "sequence_check");
            setStyleName("font-80em");
            addStyleName("margin-top-10");
        }

        public boolean isHasAttachmentChecked() {
            return hasAttachmentCheck.getValue().booleanValue();
        }

        public boolean isHasSampleChecked() {
            return hasSampleCheck.getValue().booleanValue();
        }

        public boolean isHasSequenceChecked() {
            return hasSequenceCheck.getValue().booleanValue();
        }
    }
}

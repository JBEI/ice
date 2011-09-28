package org.jbei.ice.client.search;

import java.util.HashMap;

import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderView;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.component.ExportAsPanel;
import org.jbei.ice.client.component.table.EntryTablePager;
import org.jbei.ice.shared.BlastProgram;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class BlastView extends Composite implements BlastPresenter.Display {

    private final TextArea sequence;
    private final ListBox program;
    private final Button submit;
    private BlastResultsTable resultsTable;
    private EntryTablePager pager;
    private ExportAsPanel exportPanel;

    public BlastView() {

        // page layout
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        layout.setHeight("98%");
        initWidget(layout);

        // components
        sequence = new TextArea();
        sequence.setStyleName("inputbox");
        sequence.setWidth("640px");
        sequence.setHeight("100px");

        program = new ListBox();
        program.setVisibleItemCount(1);
        program.setStyleName("inputbox");
        submit = new Button("Blast");

        resultsTable = new BlastResultsTable();
        resultsTable.setWidth("100%", true);

        // layout contents, including headers and footer
        layout.setWidget(0, 0, new HeaderView());
        layout.setWidget(1, 0, new HeaderMenu());
        layout.setWidget(2, 0, createPageComponents());
        layout.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        layout.getCellFormatter().setHeight(2, 0, "100%");
        layout.getCellFormatter().addStyleName(2, 0, "pad_top");
        layout.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
        layout.setWidget(3, 0, Footer.getInstance());
    }

    @Override
    public String getSequence() {
        return this.sequence.getText();
    }

    @Override
    public BlastProgram getProgram() {
        String value = program.getValue(program.getSelectedIndex());
        return BlastProgram.valueOf(value);
    }

    @Override
    public BlastResultsTable getResultsTable() {
        return this.resultsTable;
    }

    public void setProgramOptions(HashMap<String, String> displayValueMap) {

        for (String key : displayValueMap.keySet()) {
            String value = displayValueMap.get(key);
            if (value != null)
                program.addItem(key, value);
        }
    }

    @Override
    public Button getSubmit() {
        return this.submit;
    }

    protected Widget createPageComponents() {
        FlexTable contents = new FlexTable();
        contents.setCellPadding(0);
        contents.setCellSpacing(0);
        contents.setWidth("100%");

        contents.setWidget(0, 0, createBlastSearchPanel());
        contents.getFlexCellFormatter().setHorizontalAlignment(0, 0,
            HasHorizontalAlignment.ALIGN_CENTER);

        // add results table
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.add(resultsTable);

        // add a break between filters and results
        contents.setHTML(1, 0, "<br />");

        // table pager
        pager = new EntryTablePager();
        pager.setDisplay(resultsTable);
        panel.add(pager);

        exportPanel = new ExportAsPanel();
        panel.add(exportPanel);
        contents.setWidget(2, 0, panel);

        return contents;
    }

    protected Widget createBlastSearchPanel() {

        // wrapper
        FlexTable wrapper = new FlexTable();
        wrapper.setWidth("100%");

        // right cell contents
        FlexTable table = new FlexTable();
        table.setStyleName("data_table");
        table.setWidth("800px");
        table.setCellPadding(3);
        table.setCellSpacing(1);

        table.setHTML(0, 0, "<span class=\"panel_header\">Enter sequence to BLAST</span>");
        table.getCellFormatter().addStyleName(0, 0, "title_row_header");
        table.setWidget(1, 0, blastContents());
        table.getCellFormatter().addStyleName(1, 0, "background_white");

        wrapper.setWidget(0, 0, table);

        // results
        //        wrapper.setWidget(0, 1, createPageComponents());
        return wrapper;
    }

    protected Widget blastContents() {

        FlexTable contents = new FlexTable();
        contents.setCellPadding(0);
        contents.setCellSpacing(2);
        contents.setHTML(0, 0, "Sequence: <span class=\"required\">*</span>");
        VerticalPanel panel = new VerticalPanel();
        panel.add(sequence);
        Label example = new Label("(e.g. agatacccagatcatatgaaacagcatgactttttcaagagtgccatgccc)");
        panel.add(example);
        contents.getCellFormatter().setWidth(0, 0, "150px");
        contents.setWidget(0, 1, panel);

        contents.setHTML(1, 0, "Program: <span class=\"required\">*</span>");
        HorizontalPanel horizontal = new HorizontalPanel();
        horizontal.setWidth("100%");
        horizontal.add(program);
        horizontal.add(submit);
        horizontal.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_RIGHT);

        contents.setWidget(1, 1, horizontal);

        return contents;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setResultsDisplayVisible(boolean visible) {
        this.resultsTable.setVisible(visible);
        this.exportPanel.setVisible(visible);
        this.pager.setVisible(visible);
    }
}

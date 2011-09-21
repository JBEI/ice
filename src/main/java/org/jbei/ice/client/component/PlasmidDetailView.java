package org.jbei.ice.client.component;

import org.jbei.ice.shared.PlasmidInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

public class PlasmidDetailView extends EntryDetailView<PlasmidInfo> {

    private final FlexTable table;
    private final PlasmidInfo info;

    public PlasmidDetailView(PlasmidInfo info) {

        super(info);

        table = new FlexTable();
        table.setCellPadding(3);
        table.setCellSpacing(2);
        table.setBorderWidth(0);
        table.setStyleName("bg_white");
        initWidget(table);
        this.info = info;

        bind();
    }

    public void bind() {

        // left column
        setLeftColumn(table);

        // second column
        table.setHTML(0, 2, "<b>Markers</b>");
        table.getFlexCellFormatter().setWidth(0, 2, "100px");
        table.setText(0, 3, "");
        table.getFlexCellFormatter().setWidth(0, 3, "300px");

        table.setHTML(1, 2, "<b>Backbone</b>");
        table.setText(1, 3, info.getBackbone());

        table.setHTML(2, 2, "<b>Origin of Replication</b>");
        table.getCellFormatter().setVerticalAlignment(2, 2, HasVerticalAlignment.ALIGN_TOP);
        table.setText(2, 3, info.getOriginOfReplication());
        table.getCellFormatter().setWordWrap(2, 3, true);
        table.getCellFormatter().setWidth(2, 3, "200");

        table.setHTML(3, 2, "<b>Promoters</b>");
        table.setText(3, 3, info.getPromoters());

        table.setHTML(4, 2, "<b>Strains</b>");
        table.setText(4, 3, "");

        table.setHTML(5, 2, "<b>Created</b>");
        String created = "";
        table.setText(5, 3, created);

        table.setHTML(6, 2, "<b>Modified</b>");
        String modified = "";
        table.setText(6, 3, modified);
    }

    private static void setLeftColumn(FlexTable table) {

        table.setHTML(0, 0, "<b>Part ID</b>");
        table.setText(0, 1, "");
        table.getFlexCellFormatter().setWidth(0, 1, "200px");

        table.setHTML(1, 0, "<b>Name</b>");
        table.setText(1, 1, "");

        table.setHTML(2, 0, "<b>Alias</b>");
        table.setText(2, 1, "");

        table.setHTML(3, 0, "<b>Creator</b>");
        table.setText(3, 1, "");

        table.setHTML(4, 0, "<b>Status</b>");
        table.setText(4, 1, "");

        table.setHTML(5, 0, "<b>Owner</b>");
        table.setText(5, 1, "");

        table.setHTML(6, 0, "<b>Links</b>");
        table.setText(6, 1, "");

        table.setHTML(7, 0, "<b>Principal Investigator</b>");
        table.setText(7, 1, "");
        table.getFlexCellFormatter().setColSpan(7, 1, 3);
        table.getFlexCellFormatter().setWidth(7, 0, "300px");

        table.setHTML(8, 0, "<b>Summary</b>");
        table.setText(8, 1, "");
        table.getFlexCellFormatter().setColSpan(8, 1, 3);

        table.setHTML(9, 0, "<b>Keywords</b>");
        table.setText(9, 1, "");
        table.getFlexCellFormatter().setColSpan(9, 1, 3);

        table.setHTML(10, 0, "<b>References</b>");
        table.setText(10, 1, "");
        table.getFlexCellFormatter().setColSpan(10, 1, 3);

        table.setHTML(11, 0, "<b>Bio Safety</b>");
        table.setText(11, 1, "");
        table.getFlexCellFormatter().setColSpan(11, 1, 3);

        table.setHTML(12, 0, "<b>IP Information</b>");
        table.setText(12, 1, "");
        table.getFlexCellFormatter().setColSpan(12, 1, 3);

        table.setHTML(13, 0, "<b>Samples</b>");
        table.setText(13, 1, "");
        table.getFlexCellFormatter().setColSpan(13, 1, 3);
    }

}

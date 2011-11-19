package org.jbei.ice.client.common;

import java.util.Date;

import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.PartData;
import org.jbei.ice.shared.PlasmidTipView;
import org.jbei.ice.shared.SeedTipView;
import org.jbei.ice.shared.StrainTipView;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

public class TipViewContentFactory {

    public static Widget getContents(EntryData entry) {

        FlexTable parent = new FlexTable();
        parent.setStyleName("entry_table");
        parent.setCellPadding(0);
        parent.setCellSpacing(1);
        parent.setBorderWidth(0);

        Widget header = getHeader(entry.isHasSequence(), entry.isHasAttachment(),
            entry.isHasSample());
        parent.setWidget(0, 0, header);

        String type = entry.getType();
        Widget widget = null;
        if ("strain".equals(type)) {
            StrainTipView view = (StrainTipView) entry;
            widget = getStrainContent(view);
        } else if ("plasmid".equals(type)) {
            PlasmidTipView view = (PlasmidTipView) entry;
            widget = getPlasmidContent(view);
        } else if ("arabidopsis".equals(type)) {
            SeedTipView view = (SeedTipView) entry;
            widget = getSeedContent(view);
        } else if ("part".equals(type)) {
            PartData view = (PartData) entry;
            widget = getPartContent(view);
        } else
            return null;

        parent.setWidget(1, 0, widget);
        return parent;
    }

    private static Widget getHeader(boolean hasSequence, boolean hasAttachment, boolean hasSample) {

        FlexTable header = new FlexTable();
        header.setWidth("100%");
        header.setStyleName("title_row_header");

        // content
        header.setText(0, 0, "General Information");
        //        header.setWidget(row, column, widget)

        return header;
    }

    private static Widget getPlasmidContent(PlasmidTipView view) {

        FlexTable table = new FlexTable();
        table.setCellPadding(3);
        table.setCellSpacing(2);
        table.setBorderWidth(0);
        table.setStyleName("background_white");

        // left column
        setLeftColumn(table, view);

        // second column
        table.setHTML(0, 2, "<b>Markers</b>");
        table.getFlexCellFormatter().setWidth(0, 2, "100px");
        table.setText(0, 3, view.getMarkers());
        table.getFlexCellFormatter().setWidth(0, 3, "300px");

        table.setHTML(1, 2, "<b>Backbone</b>");
        table.setText(1, 3, view.getBackbone());

        table.setHTML(2, 2, "<b>Origin of Replication</b>");
        table.getCellFormatter().setVerticalAlignment(2, 2, HasVerticalAlignment.ALIGN_TOP);
        table.setText(2, 3, view.getOrigin());
        table.getCellFormatter().setWordWrap(2, 3, true);
        table.getCellFormatter().setWidth(2, 3, "200");

        table.setHTML(3, 2, "<b>Promoters</b>");
        table.setText(3, 3, view.getPromoters());

        table.setHTML(4, 2, "<b>Strains</b>");
        table.setText(4, 3, view.getStrains());

        table.setHTML(5, 2, "<b>Created</b>");
        String created = generateDate(view.getCreated());
        table.setText(5, 3, created);

        table.setHTML(6, 2, "<b>Modified</b>");
        String modified = generateDate(view.getModified());
        table.setText(6, 3, modified);

        return table;
    }

    private static Widget getPartContent(PartData view) {

        FlexTable table = new FlexTable();
        table.setCellPadding(3);
        table.setCellSpacing(2);
        table.setBorderWidth(0);
        table.setStyleName("bg_white");

        // left column
        setLeftColumn(table, view);

        // second column
        table.setHTML(0, 2, "<b>Packaging Format</b>");
        table.getFlexCellFormatter().setWidth(0, 2, "100px");
        table.setText(0, 3, view.getPackagingFormat());
        table.getFlexCellFormatter().setWidth(0, 3, "300px");

        table.setHTML(1, 2, "");
        table.setText(1, 3, "");

        table.setHTML(2, 2, "");
        table.setText(2, 3, "");
        table.getCellFormatter().setWordWrap(2, 3, true);
        table.getCellFormatter().setWidth(2, 3, "200");

        table.setHTML(3, 2, "");
        table.setText(3, 3, "");

        table.setHTML(4, 2, "");
        table.setText(4, 3, "");

        table.setHTML(5, 2, "<b>Created</b>");
        String created = generateDate(view.getCreated());
        table.setText(5, 3, created);

        table.setHTML(6, 2, "<b>Modified</b>");
        String modified = generateDate(view.getModified());
        table.setText(6, 3, modified);

        return table;
    }

    private static Widget getSeedContent(SeedTipView view) {

        FlexTable table = new FlexTable();
        table.setCellPadding(3);
        table.setCellSpacing(2);
        table.setBorderWidth(0);
        table.setStyleName("bg_white");

        // left column
        setLeftColumn(table, view);

        // second column
        table.setHTML(0, 2, "<b>Plant Type</b>");
        table.getFlexCellFormatter().setWidth(0, 2, "100px");
        table.setText(0, 3, view.getPlantType());
        table.getFlexCellFormatter().setWidth(0, 3, "300px");

        table.setHTML(1, 2, "Generation");
        table.setText(1, 3, view.getGeneration());

        table.setHTML(2, 2, "Homozygosity");
        table.setText(2, 3, view.getHomozygosity());
        table.getCellFormatter().setWordWrap(2, 3, true);
        table.getCellFormatter().setWidth(2, 3, "200");

        table.setHTML(3, 2, "Ecotype");
        table.setText(3, 3, view.getEcotype());

        table.setHTML(4, 2, "Parents");
        table.setText(4, 3, view.getParents());

        table.setHTML(5, 2, "Harvested");
        table.setText(5, 3, view.getHarvested());

        table.setHTML(6, 2, "<b>Created</b>");
        String created = generateDate(view.getCreated());
        table.setText(6, 3, created);

        table.setHTML(7, 2, "<b>Modified</b>");
        String modified = generateDate(view.getModified());
        table.setText(7, 3, modified);

        return table;
    }

    private static Widget getStrainContent(StrainTipView view) {

        FlexTable table = new FlexTable();
        table.setCellPadding(3);
        table.setCellSpacing(2);
        table.setBorderWidth(0);
        table.setStyleName("bg_white");

        // left column
        setLeftColumn(table, view);

        // second column
        table.setHTML(0, 2, "<b>Markers</b>");
        table.getFlexCellFormatter().setWidth(0, 2, "100px");
        table.setText(0, 3, view.getMarkers());
        table.getFlexCellFormatter().setWidth(0, 3, "300px");

        table.setHTML(1, 2, "<b>Host</b>");
        table.setText(1, 3, view.getHost());

        table.setHTML(2, 2, "<b>Genotype/Phenotype</b>");
        table.getCellFormatter().setVerticalAlignment(2, 2, HasVerticalAlignment.ALIGN_TOP);
        table.setText(2, 3, view.getGenPhen());
        table.getCellFormatter().setWordWrap(2, 3, true);
        table.getCellFormatter().setWidth(2, 3, "200");

        table.setHTML(3, 2, "<b>Plasmids</b>");
        table.setText(3, 3, view.getPlasmids());

        table.setHTML(4, 2, "<b>Created</b>");
        String created = generateDate(view.getCreated());
        table.setText(4, 3, created);

        table.setHTML(5, 2, "<b>Modified</b>");
        String modified = generateDate(view.getModified());
        table.setText(5, 3, modified);

        //        table.setHTML(6, 2, "<b>Funding Source</b>");
        //        table.setText(6, 3, "");

        return table;
    }

    private static void setLeftColumn(FlexTable table, EntryData entry) {

        table.setHTML(0, 0, "<b>Part ID</b>");
        table.setText(0, 1, entry.getPartId());
        table.getFlexCellFormatter().setWidth(0, 1, "200px");

        table.setHTML(1, 0, "<b>Name</b>");
        table.setText(1, 1, entry.getName());

        table.setHTML(2, 0, "<b>Alias</b>");
        table.setText(2, 1, entry.getAlias());

        table.setHTML(3, 0, "<b>Creator</b>");
        table.setText(3, 1, entry.getCreator());

        table.setHTML(4, 0, "<b>Status</b>");
        table.setText(4, 1, entry.getStatus());

        table.setHTML(5, 0, "<b>Owner</b>");
        table.setText(5, 1, entry.getOwnerName());

        table.setHTML(6, 0, "<b>Links</b>");
        table.setText(6, 1, "");

        table.setHTML(7, 0, "<b>Principal Investigator</b>");
        table.setText(7, 1, entry.getpI());
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

    private static String generateDate(long time) {

        DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
        Date date = new Date(time);
        String value = format.format(date);
        return value;
    }
}

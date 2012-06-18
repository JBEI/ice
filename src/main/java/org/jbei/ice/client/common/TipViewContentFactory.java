package org.jbei.ice.client.common;

import java.util.Date;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.RegistryService;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PartInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.StrainInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

/**
 * Factory for generating a widget that is used as a tooltip for entrys
 * 
 * @author Hector Plahar
 */
// TODO : need better separation of concerns
public class TipViewContentFactory {

    private static final RegistryServiceAsync service = GWT.create(RegistryService.class);

    public static void getContents(EntryInfo entry, final Callback<Widget> callback) {

        try {
            service.retrieveEntryTipDetails(AppController.sessionId, entry.getId(),
                new AsyncCallback<EntryInfo>() {

                    @Override
                    public void onSuccess(EntryInfo result) {
                        if (result == null) {
                            callback.onFailure();
                            return;
                        }
                        Widget contents = getContents(result);
                        callback.onSucess(contents);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure();
                    }
                });
        } catch (org.jbei.ice.client.exception.AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private static Widget getContents(EntryInfo entry) {

        FlexTable parent = new FlexTable();
        parent.setWidth("650px");
        parent.setCellPadding(2);
        parent.setCellSpacing(0);
        parent.setStyleName("background_white");
        parent.addStyleName("pad-6");

        setHeader(parent, entry);

        switch (entry.getType()) {
        case STRAIN:
            StrainInfo view = (StrainInfo) entry;
            getStrainContent(parent, view);
            break;

        case PLASMID:
            PlasmidInfo plasmidInfo = (PlasmidInfo) entry;
            getPlasmidContent(parent, plasmidInfo);
            break;

        case ARABIDOPSIS:
            ArabidopsisSeedInfo seedInfo = (ArabidopsisSeedInfo) entry;
            getSeedContent(parent, seedInfo);
            break;

        case PART:
            PartInfo partInfo = (PartInfo) entry;
            getPartContent(parent, partInfo);
            break;

        default:
            return null;
        }

        return parent;
    }

    private static void setHeader(FlexTable layout, EntryInfo entry) {
        if (entry == null)
            return;
        boolean hasSample = entry.isHasSample();
        boolean hasAttachment = entry.isHasAttachment();
        boolean hasSequence = entry.isHasSequence();

        layout.setHTML(0, 0, "<span class=\"entry_tooltip_sub_header\">General Information</span>");
        layout.getFlexCellFormatter().setColSpan(0, 0, 3);

        HTMLPanel panel = new HTMLPanel(
                "<span id=\"has_attachment_image\"></span><span id=\"has_sample_image\"></span><span id=\"has_sequence_image\"></span>");

        if (hasAttachment)
            panel.add(ImageUtil.getAttachment(), "has_attachment_image");
        else
            panel.add(ImageUtil.getBlankIcon(), "has_attachment_image");

        if (hasSample)
            panel.add(ImageUtil.getSampleIcon(), "has_sample_image");
        else
            panel.add(ImageUtil.getBlankIcon(), "has_sample_image");

        if (hasSequence)
            panel.add(ImageUtil.getSequenceIcon(), "has_sequence_image");
        else
            panel.add(ImageUtil.getBlankIcon(), "has_sequence_image");

        layout.setWidget(0, 1, panel);
        layout.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_RIGHT);
    }

    private static void getPlasmidContent(FlexTable table, PlasmidInfo view) {

        // left column
        setLeftColumn(table, view);

        // second column
        addField(table, 1, 2, "Selection Markers", view.getSelectionMarkers());
        addField(table, 2, 2, "Backbone", view.getBackbone());
        addField(table, 3, 2, "Origin of Replication", view.getOriginOfReplication());
        table.getCellFormatter().setWordWrap(3, 3, true);

        String strains = "";
        for (Long id : view.getStrains().keySet())
            strains += (view.getStrains().get(id) + ", ");

        if (view.getStrains().keySet().size() > 0)
            strains = strains.substring(0, strains.lastIndexOf(", "));
        addField(table, 4, 2, "Strains", strains);
        addField(table, 5, 2, "Modified", generateDate(view.getModificationTime()));
        addField(table, 6, 2, "Funding Source", view.getFundingSource());
    }

    private static void getStrainContent(FlexTable table, StrainInfo view) {

        // left column
        setLeftColumn(table, view);

        // second column
        addField(table, 1, 2, "Selection Markers", view.getSelectionMarkers());
        addField(table, 2, 2, "Host", view.getHost());
        addField(table, 3, 2, "Genotype/Phenotype", view.getGenotypePhenotype());
        addField(table, 4, 2, "Plasmids", view.getLinkifiedPlasmids());
        addField(table, 5, 2, "Modified", generateDate(view.getModificationTime()));
        addField(table, 6, 2, "Funding Source", view.getFundingSource());
    }

    private static void getPartContent(FlexTable table, PartInfo view) {

        // left column
        setLeftColumn(table, view);

        // second column
        addField(table, 1, 2, "Package Format", view.getPackageFormat());
        addField(table, 2, 2, "Modified", generateDate(view.getModificationTime()));
        String fundingSource = view.getFundingSource() == null ? "" : view.getFundingSource();
        addField(table, 3, 2, "Funding Source", fundingSource);
    }

    private static void getSeedContent(FlexTable table, ArabidopsisSeedInfo view) {

        // left column
        setLeftColumn(table, view);

        // second column
        String plantType = (view.getPlantType() == null) ? "" : view.getPlantType().toString();
        addField(table, 1, 2, "Plant Type", plantType);
        String generation = (view.getGeneration() == null) ? "" : view.getGeneration().toString();
        addField(table, 2, 2, "Generation", generation);
        addField(table, 3, 2, "Homozygosity", view.getHomozygosity());
        addField(table, 4, 2, "Ecotype", view.getEcotype());
        addField(table, 5, 2, "Parents", view.getParents());
        addField(table, 6, 2, "Harvested", generateDate(view.getHarvestDate()));
        addField(table, 7, 2, "Modified", generateDate(view.getModificationTime()));
        addField(table, 8, 2, "Funding Source", view.getFundingSource());
    }

    private static void addField(FlexTable table, int row, int col, String header, String value) {
        table.setHTML(row, col, "<b class=\"font-80em\">" + header + "</b>");
        table.getFlexCellFormatter().setWidth(row, col, "150px");

        if (value == null)
            value = "";

        col += 1;
        table.setHTML(row, col, "<span class=\"font-80em\">" + value + "</span>");
    }

    private static void setLeftColumn(FlexTable table, EntryInfo entry) {
        addField(table, 1, 0, "Part ID", entry.getPartId());
        addField(table, 2, 0, "Alias", entry.getAlias());
        addField(table, 3, 0, "Creator", entry.getCreator());
        addField(table, 4, 0, "Owner", entry.getOwner());
        addField(table, 5, 0, "Links", entry.getLinkifiedLinks());
        addField(table, 6, 0, "Principal Investigator", entry.getPrincipalInvestigator());
        addField(table, 7, 0, "Keywords", entry.getKeywords());
        addField(table, 8, 0, "References", entry.getReferences());
        addField(table, 9, 0, "Bio Safety", entry.getBioSafetyLevel() + "");
        addField(table, 10, 0, "IP Information", entry.getIntellectualProperty());

        //        table.setHTML(11, 0, "<b class=\"entry_tooltip_sub_header\">Samples</b>");
        //        table.getFlexCellFormatter().setColSpan(11, 0, 4);

        //        Widget samplesWidget = createSamplesWidget(entry.getSampleStorage());
        //        table.setWidget(12, 0, samplesWidget);
        //        table.getFlexCellFormatter().setColSpan(12, 0, 4);
    }

    //    private static Widget createSamplesWidget(ArrayList<SampleStorage> data) {
    //        if (data == null || data.isEmpty())
    //            return new HTML("<span class=\"font-75em\">No samples</span>");
    //
    //        EntrySampleTable sampleTable = new EntrySampleTable();
    //        sampleTable.setData(data);
    //
    //        return sampleTable;
    //    }

    private static String generateDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
        String value = format.format(date);
        return value;
    }
}

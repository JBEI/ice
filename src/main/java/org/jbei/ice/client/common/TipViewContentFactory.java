package org.jbei.ice.client.common;

import java.util.Date;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryService;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.entry.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.PartInfo;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.shared.dto.entry.StrainInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

/**
 * Factory for generating a widget that is used as a tooltip for entrys
 *
 * @author Hector Plahar
 */
public class TipViewContentFactory {

    private static final RegistryServiceAsync service = GWT.create(RegistryService.class);

    public static void getContents(final EntryInfo entry, String url, final Callback<Widget> callback) {
        try {
            service.retrieveEntryTipDetails(ClientController.sessionId, entry.getRecordId(), url,
                                            new AsyncCallback<EntryInfo>() {

                                                @Override
                                                public void onSuccess(EntryInfo result) {
                                                    Widget contents = getContents(result);
                                                    callback.onSuccess(contents);
                                                }

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    callback.onFailure();
                                                }
                                            });
        } catch (AuthenticationException e) {
            History.newItem(Page.LOGIN.getLink());
        }
    }

    private static Widget getContents(EntryInfo entry) {
        FlexTable table = new FlexTable();
        table.setWidth("650px");
        table.setCellPadding(2);
        table.setCellSpacing(0);
        table.setStyleName("bg_white");
        table.addStyleName("pad-6");
        int r;

        switch (entry.getType()) {
            case STRAIN:
                StrainInfo view = (StrainInfo) entry;
                r = getStrainContent(table, view);
                break;

            case PLASMID:
                PlasmidInfo plasmidInfo = (PlasmidInfo) entry;
                r = getPlasmidContent(table, plasmidInfo);
                break;

            case ARABIDOPSIS:
                ArabidopsisSeedInfo seedInfo = (ArabidopsisSeedInfo) entry;
                r = getSeedContent(table, seedInfo);
                break;

            case PART:
                PartInfo partInfo = (PartInfo) entry;
                r = getPartContent(table, partInfo);
                break;

            default:
                return null;
        }

        addField(table, r, 2, "Principal Investigator", entry.getPrincipalInvestigator(), "135px", "230px");
        return table;
    }

    private static int getPlasmidContent(FlexTable table, PlasmidInfo view) {
        setLeftColumn(table, view);

        // second column
        addField(table, 1, 2, "Selection Markers", view.getSelectionMarkers(), "135px", "230px");
        addField(table, 2, 2, "Backbone", view.getBackbone(), "135px", "230px");
        addField(table, 3, 2, "Origin of Replication", view.getOriginOfReplication(), "135px", "230px");
        addField(table, 4, 2, "Promoters", view.getPromoters(), "135px", "230px");

        String strains = "";
        for (Long id : view.getStrains().keySet())
            strains += (view.getStrains().get(id) + ", ");

        if (view.getStrains().keySet().size() > 0)
            strains = strains.substring(0, strains.lastIndexOf(", "));
        addField(table, 5, 2, "Strains", strains, "135px", "230px");
        addField(table, 6, 2, "Funding Source", view.getFundingSource(), "135px", "230px");
        return 7;
    }

    private static int getStrainContent(FlexTable table, StrainInfo view) {
        setLeftColumn(table, view);

        // second column
        addField(table, 1, 2, "Selection Markers", view.getSelectionMarkers(), "135px", "230px");
        addField(table, 2, 2, "Host", view.getLinkifiedHost(), "125px", "200px");
        addField(table, 3, 2, "Genotype/Phenotype", view.getGenotypePhenotype(), "135px", "230px");
        addField(table, 4, 2, "Plasmids", view.getLinkifiedPlasmids(), "135px", "230px");
        addField(table, 5, 2, "Funding Source", view.getFundingSource(), "135px", "230px");
        return 6;
    }

    private static int getPartContent(FlexTable table, PartInfo view) {
        setLeftColumn(table, view);

        // second column
        addField(table, 1, 2, "Package Format", view.getPackageFormat(), "135px", "230px");
        String fundingSource = view.getFundingSource() == null ? "" : view.getFundingSource();
        addField(table, 2, 2, "Funding Source", fundingSource, "135px", "230px");
        return 3;
    }

    private static int getSeedContent(FlexTable table, ArabidopsisSeedInfo view) {
        setLeftColumn(table, view);

        // second column
        String plantType = (view.getPlantType() == null) ? "" : view.getPlantType().toString();
        addField(table, 1, 2, "Plant Type", plantType, "135px", "230px");
        String generation = (view.getGeneration() == null) ? "" : view.getGeneration().toString();
        addField(table, 2, 2, "Generation", generation, "135px", "230px");
        addField(table, 3, 2, "Homozygosity", view.getHomozygosity(), "135px", "230px");
        addField(table, 4, 2, "Ecotype", view.getEcotype(), "135px", "230px");
        addField(table, 5, 2, "Parents", view.getParents(), "135px", "230px");
        addField(table, 6, 2, "Harvested", generateDate(view.getHarvestDate()), "135px", "230px");
        addField(table, 7, 2, "Funding Source", view.getFundingSource(), "135px", "230px");
        return 8;
    }

    private static void addField(FlexTable table, int row, int col, String header,
            String value, String headerWidth, String valueWidth) {
        table.setHTML(row, col, "<b class=\"font-75em\" style=\"color: #222\">" + header + "</b>");
        table.getFlexCellFormatter().setWidth(row, col, headerWidth);
        table.getFlexCellFormatter().setVerticalAlignment(row, col, HasAlignment.ALIGN_TOP);

        if (value == null)
            value = "";

        col += 1;
        table.setHTML(row, col, "<span class=\"font-75em\">" + value + "</span>");
        table.getFlexCellFormatter().setWidth(row, col, valueWidth);
        table.getFlexCellFormatter().setVerticalAlignment(row, col, HasAlignment.ALIGN_TOP);
    }

    private static void setLeftColumn(FlexTable table, EntryInfo entry) {
        addField(table, 1, 0, "Part ID", entry.getPartId(), "115px", "170px");
        addField(table, 2, 0, "Alias", entry.getAlias(), "115px", "170px");
        addField(table, 3, 0, "Creator", entry.getCreator(), "115px", "170px");
        addField(table, 4, 0, "Owner", entry.getOwner(), "115px", "170px");
        addField(table, 5, 0, "Links", entry.getLinkifiedLinks(), "115px", "170px");
        addField(table, 6, 0, "Modified", generateDate(entry.getModificationTime()), "115px", "170px");
        addField(table, 7, 0, "Keywords", entry.getKeywords(), "115px", "170px");
        addField(table, 8, 0, "References", entry.getReferences(), "115px", "170px");
        addField(table, 9, 0, "Bio Safety", "Level " + entry.getBioSafetyLevel(), "115px", "170px");
        addField(table, 10, 0, "IP Information", entry.getIntellectualProperty(), "115px", "170px");
    }

    private static String generateDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
        return format.format(date);
    }
}

package org.jbei.ice.web.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.AuthenticatedSampleManager;

public class EntriesAllFieldsExcelExportPage extends ExcelExportPage {
    private ArrayList<Entry> entries;

    public EntriesAllFieldsExcelExportPage(ArrayList<Entry> entries) {
        super();

        this.entries = entries;
    }

    @Override
    public String getFileName() {
        return "data.xls";
    }

    @Override
    public String getContent() {
        int index = 1;

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("#").append("\t");
        stringBuilder.append("Type").append("\t");
        stringBuilder.append("Part ID").append("\t");
        stringBuilder.append("Name").append("\t");
        stringBuilder.append("Owner").append("\t");
        stringBuilder.append("Creator").append("\t");
        stringBuilder.append("Alias").append("\t");
        stringBuilder.append("Keywords").append("\t");
        stringBuilder.append("Links").append("\t");
        stringBuilder.append("Status").append("\t");
        stringBuilder.append("Summary").append("\t");
        stringBuilder.append("Notes").append("\t");
        stringBuilder.append("References").append("\t");
        stringBuilder.append("Markers (Plasmid and Strain)").append("\t");
        stringBuilder.append("Backbone (Plasmid)").append("\t");
        stringBuilder.append("Origin of Replication(Plasmid)").append("\t");
        stringBuilder.append("Promoters (Plasmid)").append("\t");
        stringBuilder.append("Circular (Plasmid)").append("\t");
        stringBuilder.append("Host (Strain)").append("\t");
        stringBuilder.append("Genotype/Phenotype (Strain)").append("\t");
        stringBuilder.append("Plasmids (Strain)").append("\t");
        stringBuilder.append("Package Format (Part)").append("\t");
        stringBuilder.append("Has Attachment").append("\t");
        stringBuilder.append("Has Samples").append("\t");
        stringBuilder.append("Has Sequence").append("\t");
        stringBuilder.append("Bio Safety").append("\t");
        stringBuilder.append("IP Information").append("\t");
        stringBuilder.append("Principal Investigator").append("\t");
        stringBuilder.append("Funding Source").append("\t");
        stringBuilder.append("Created").append("\t");
        stringBuilder.append("Updated").append("\n");

        for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext();) {
            Entry entry = iterator.next();

            stringBuilder.append(index).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getRecordType())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getPartNumbersAsString())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getNamesAsString())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getOwner())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getCreator())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getAlias())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getKeywords())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getLinksAsString())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getStatus())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getShortDescription())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getLongDescription())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getReferences())).append("\t");

            if (entry instanceof Plasmid) {
                Plasmid plasmid = (Plasmid) entry;

                stringBuilder.append(escapeCSVValue(plasmid.getSelectionMarkersAsString())).append(
                        "\t");
                stringBuilder.append(escapeCSVValue(plasmid.getBackbone())).append("\t");
                stringBuilder.append(escapeCSVValue(plasmid.getOriginOfReplication())).append("\t");
                stringBuilder.append(escapeCSVValue(plasmid.getPromoters())).append("\t");
                stringBuilder.append(plasmid.getCircular() ? "Yes" : "No").append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
            } else if (entry instanceof Strain) {
                Strain strain = (Strain) entry;

                stringBuilder.append(escapeCSVValue(strain.getSelectionMarkersAsString())).append(
                        "\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append(escapeCSVValue(strain.getHost())).append("\t");
                stringBuilder.append(escapeCSVValue(strain.getGenotypePhenotype())).append("\t");
                stringBuilder.append(escapeCSVValue(strain.getPlasmids())).append("\t");
                stringBuilder.append("\t");
            } else if (entry instanceof Part) {
                Part part = (Part) entry;

                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append("\t");
                stringBuilder.append(escapeCSVValue(part.getPackageFormat())).append("\t");
            }

            stringBuilder.append((AttachmentManager.hasAttachment(entry)) ? "Yes" : "No").append(
                    "\t");
            stringBuilder.append((AuthenticatedSampleManager.hasSample(entry)) ? "Yes" : "No")
                    .append("\t");
            stringBuilder.append((SequenceManager.hasSequence(entry)) ? "Yes" : "No").append("\t");
            stringBuilder.append(escapeCSVValue(entry.getBioSafetyLevel())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getIntellectualProperty())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.principalInvestigatorToString()))
                    .append("\t");
            stringBuilder.append(escapeCSVValue(entry.fundingSourceToString())).append("\t");

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");

            stringBuilder.append(
                    (entry.getCreationTime() == null) ? "" : dateFormat.format(entry
                            .getCreationTime())).append("\t");
            stringBuilder.append(
                    (entry.getModificationTime() == null) ? "" : dateFormat.format(entry
                            .getModificationTime())).append("\t");
            stringBuilder.append("\n");

            index++;
        }

        return stringBuilder.toString();
    }
}

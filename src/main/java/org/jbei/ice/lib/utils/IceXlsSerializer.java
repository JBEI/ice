package org.jbei.ice.lib.utils;

import org.apache.commons.lang.StringUtils;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.web.common.ViewException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class IceXlsSerializer {

    protected static Object escapeCSVValue(Object value) {
        if (value != null) {
            String stringValue = StringUtils.trim(value.toString());
            if (!StringUtils.containsNone(stringValue, new char[]{'\n', ',', '\t'})) {
                return "\"" + StringUtils.replace(stringValue, "\"", "\\\"") + "\"";
            }

            return stringValue;
        }

        return "";
    }

    public static String serialize(EntryController entryController, ArrayList<Entry> entries) {
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

        for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext(); ) {
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

            try {
                AccountController controller = new AccountController();
                SampleController sampleController = new SampleController();
                SequenceController sequenceController = new SequenceController();
                stringBuilder.append((entryController.hasAttachments(controller.getSystemAccount(),
                                                                     entry)) ? "Yes" : "No")
                             .append("\t");
                stringBuilder.append((sampleController.hasSample(entry)) ? "Yes" : "No").append(
                        "\t");
                stringBuilder.append((sequenceController.hasSequence(entry)) ? "Yes" : "No").append(
                        "\t");
            } catch (ControllerException e) {
                throw new ViewException(e);
            }

            stringBuilder.append(escapeCSVValue(entry.getBioSafetyLevel())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getIntellectualProperty())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.principalInvestigatorToString()))
                         .append("\t");
            stringBuilder.append(escapeCSVValue(entry.fundingSourceToString())).append("\t");

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");

            stringBuilder
                    .append(
                            (entry.getCreationTime() == null) ? "" : dateFormat.format(entry
                                                                                               .getCreationTime()))
                    .append("\t");
            stringBuilder.append(
                    (entry.getModificationTime() == null) ? "" : dateFormat.format(entry
                                                                                           .getModificationTime()))
                         .append("\t");
            stringBuilder.append("\n");

            index++;
        }

        return stringBuilder.toString();
    }

}

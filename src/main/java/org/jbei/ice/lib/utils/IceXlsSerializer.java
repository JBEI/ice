package org.jbei.ice.lib.utils;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sequence.SequenceController;

import org.apache.commons.lang.StringUtils;

/**
 * Serializer for entry -> xls conversion
 *
 * @author Hector Plahar, Tim Ham
 */
public class IceXlsSerializer {

    private static final String PLASMID_TYPE = "PLASMID";
    private static final String STRAIN_TYPE = "STRAIN";
    private static final String ARABIDOPSIS_TYPE = "ARABIDOPSIS";

    protected static Object escapeCSVValue(Object value) {
        if (value != null) {
            String stringValue = StringUtils.trim(value.toString());
            if (!StringUtils.containsNone(stringValue, new char[]{'\n', ',', ','})) {
                return "\"" + StringUtils.replace(stringValue, "\"", "\\\"") + "\"";
            }
            return stringValue;
        }
        return "";
    }

    private static HashSet<String> getHeaders(String type) {
        HashSet<String> headers = new HashSet<>();
        switch (type.toUpperCase().trim()) {
            case PLASMID_TYPE:
                headers.add("Circular");
                headers.add("Backbone");
                headers.add("Promoters");
                headers.add("Replicates In");
                headers.add("Origin of Replication");
                break;

            case STRAIN_TYPE:
                headers.add("Parental Strain");
                headers.add("Genotype or Phenotype");
                headers.add("Plasmids");
                break;

            case ARABIDOPSIS_TYPE:
                headers.add("Homozygosity");
                headers.add("Harvest Date");
                headers.add("Ecotype");
                headers.add("Parents");
                headers.add("Generation");
                headers.add("Plant Type");
                headers.add("Sent to ABRC?");
                break;

            default:
                return headers;
        }
        return headers;
    }

    public static String serialize(List<Entry> entries, Set<String> types) throws ControllerException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Type").append(",");
        stringBuilder.append("Part ID").append(",");
        stringBuilder.append("Name").append(",");
        stringBuilder.append("Owner").append(",");
        stringBuilder.append("Creator").append(",");
        stringBuilder.append("Alias").append(",");
        stringBuilder.append("Keywords").append(",");
        stringBuilder.append("Markers").append(",");
        stringBuilder.append("Links").append(",");
        stringBuilder.append("Status").append(",");
        stringBuilder.append("Summary").append(",");
        stringBuilder.append("Notes").append(",");
        stringBuilder.append("References").append(",");
        stringBuilder.append("BioSafety Level").append(",");
        stringBuilder.append("IP Information").append(",");
        stringBuilder.append("Principal Investigator").append(",");
        stringBuilder.append("Funding Source").append(",");
        stringBuilder.append("Created").append(",");
        stringBuilder.append("Updated").append(",");

        for (String type : types) {
            for (String header : getHeaders(type)) {
                stringBuilder.append(header).append(",");
            }
        }

        stringBuilder.append("Has Attachment").append(",");
        stringBuilder.append("Has Samples").append(",");
        stringBuilder.append("Has Sequence").append("\n");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        SampleController sampleController = ControllerFactory.getSampleController();
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        AttachmentController attachmentController = ControllerFactory.getAttachmentController();

        for (Entry entry : entries) {
            stringBuilder.append(escapeCSVValue(entry.getRecordType())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getPartNumber())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getName())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getOwner())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getCreator())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getAlias())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getKeywords())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getSelectionMarkersAsString())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getLinksAsString())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getStatus())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getShortDescription())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getLongDescription())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getReferences())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getBioSafetyLevel())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getIntellectualProperty())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getPrincipalInvestigator())).append(",");
            stringBuilder.append(escapeCSVValue(entry.getFundingSource())).append(",");
            String time = (entry.getCreationTime() == null) ? "" : dateFormat.format(entry.getCreationTime());
            stringBuilder.append(escapeCSVValue(time)).append(",");
            time = (entry.getModificationTime() == null) ? "" : dateFormat.format(entry.getModificationTime());
            stringBuilder.append(escapeCSVValue(time)).append(",");
            final String type = entry.getRecordType().toUpperCase();

            switch (type) {
                case ARABIDOPSIS_TYPE:
                    ArabidopsisSeed seed = (ArabidopsisSeed) entry;
                    for (String header : getHeaders(type)) {
                        if (header.equalsIgnoreCase("Homozygosity"))
                            stringBuilder.append(escapeCSVValue(seed.getHomozygosity())).append(",");
                        if (header.equalsIgnoreCase("Harvest Date")) {
                            time = (entry.getCreationTime() == null) ? "" : dateFormat.format(seed.getHarvestDate());
                            stringBuilder.append(escapeCSVValue(time)).append(",");
                        }
                        if (header.equalsIgnoreCase("Ecotype"))
                            stringBuilder.append(escapeCSVValue(seed.getEcotype())).append(",");
                        if (header.equalsIgnoreCase("Parents"))
                            stringBuilder.append(escapeCSVValue(seed.getParents())).append(",");
                        if (header.equalsIgnoreCase("Generation"))
                            stringBuilder.append(escapeCSVValue(seed.getGeneration())).append(",");
                        if (header.equalsIgnoreCase("Plant Type"))
                            stringBuilder.append(escapeCSVValue(seed.getPlantType())).append(",");
                        if (header.equalsIgnoreCase("Sent to ABRC?"))
                            stringBuilder.append(seed.isSentToABRC() ? "Yes" : "No").append(",");
                    }
                    if (types.contains(PLASMID_TYPE)) {
                        for (int i = 0; i < 4; i += 1)
                            stringBuilder.append(",");
                    }
                    if (types.contains(STRAIN_TYPE)) {
                        for (int i = 0; i < 3; i += 1)
                            stringBuilder.append(",");
                    }
                    break;

                case PLASMID_TYPE:
                    Plasmid plasmid = (Plasmid) entry;
                    if (types.contains(ARABIDOPSIS_TYPE)) {
                        for (int i = 0; i < 7; i += 1)
                            stringBuilder.append(",");
                    }
                    for (String header : getHeaders(type)) {
                        if (header.equalsIgnoreCase("Circular"))
                            stringBuilder.append(plasmid.getCircular() ? "Yes" : "No").append(",");
                        if (header.equalsIgnoreCase("Backbone"))
                            stringBuilder.append(escapeCSVValue(plasmid.getBackbone())).append(",");
                        if (header.equalsIgnoreCase("Promoters"))
                            stringBuilder.append(escapeCSVValue(plasmid.getPromoters())).append(",");
                        if (header.equalsIgnoreCase("Replicates In"))
                            stringBuilder.append(escapeCSVValue(plasmid.getReplicatesIn())).append(",");
                        if (header.equalsIgnoreCase("Origin of Replication"))
                            stringBuilder.append(escapeCSVValue(plasmid.getOriginOfReplication())).append(",");
                    }
                    if (types.contains(STRAIN_TYPE)) {
                        for (int i = 0; i < 3; i += 1)
                            stringBuilder.append(",");
                    }
                    break;


                case STRAIN_TYPE:
                    Strain strain = (Strain) entry;
                    if (types.contains(ARABIDOPSIS_TYPE)) {
                        for (int i = 0; i < 7; i += 1)
                            stringBuilder.append(",");
                    }
                    if (types.contains(PLASMID_TYPE)) {
                        for (int i = 0; i < 4; i += 1)
                            stringBuilder.append(",");
                    }
                    for (String header : getHeaders(type)) {
                        if (header.equalsIgnoreCase("Parental Strain"))
                            stringBuilder.append(escapeCSVValue(strain.getHost())).append(",");
                        if (header.equalsIgnoreCase("Genotype or Phenotype"))
                            stringBuilder.append(escapeCSVValue(strain.getGenotypePhenotype())).append(",");
                        if (header.equalsIgnoreCase("Plasmids"))
                            stringBuilder.append(escapeCSVValue(strain.getPlasmids())).append(",");
                    }
                    break;
            }

            stringBuilder.append(attachmentController.hasAttachment(entry) ? "Yes" : "No").append(",");
            stringBuilder.append((sampleController.hasSample(entry)) ? "Yes" : "No").append(",");
            stringBuilder.append((sequenceController.hasSequence(entry.getId())) ? "Yes" : "No").append("\n");
        }

        return stringBuilder.toString();
    }
}

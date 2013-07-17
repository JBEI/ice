package org.jbei.ice.lib.utils;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.entry.EntryUtil;
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
            if (!StringUtils.containsNone(stringValue, new char[]{'\n', ',', '\t'})) {
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
        stringBuilder.append("Type").append("\t");
        stringBuilder.append("Part ID").append("\t");
        stringBuilder.append("Name").append("\t");
        stringBuilder.append("Owner").append("\t");
        stringBuilder.append("Creator").append("\t");
        stringBuilder.append("Alias").append("\t");
        stringBuilder.append("Keywords").append("\t");
        stringBuilder.append("Markers").append("\t");
        stringBuilder.append("Links").append("\t");
        stringBuilder.append("Status").append("\t");
        stringBuilder.append("Summary").append("\t");
        stringBuilder.append("Notes").append("\t");
        stringBuilder.append("References").append("\t");
        stringBuilder.append("BioSafety Level").append("\t");
        stringBuilder.append("IP Information").append("\t");
        stringBuilder.append("Principal Investigator").append("\t");
        stringBuilder.append("Funding Source").append("\t");
        stringBuilder.append("Created").append("\t");
        stringBuilder.append("Updated").append("\t");

        for (String type : types) {
            for (String header : getHeaders(type)) {
                stringBuilder.append(header).append("\t");
            }
        }

        stringBuilder.append("Has Attachment").append("\t");
        stringBuilder.append("Has Samples").append("\t");
        stringBuilder.append("Has Sequence").append("\n");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        SampleController sampleController = ControllerFactory.getSampleController();
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        AttachmentController attachmentController = ControllerFactory.getAttachmentController();

        for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext(); ) {
            Entry entry = iterator.next();
            stringBuilder.append(escapeCSVValue(entry.getRecordType())).append("\t");
            stringBuilder.append(escapeCSVValue(EntryUtil.getPartNumbersAsString(entry))).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getNamesAsString())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getOwner())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getCreator())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getAlias())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getKeywords())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getSelectionMarkersAsString())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getLinksAsString())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getStatus())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getShortDescription())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getLongDescription())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getReferences())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getBioSafetyLevel())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getIntellectualProperty())).append("\t");
            String pis = EntryUtil.principalInvestigatorToString(entry.getEntryFundingSources());
            stringBuilder.append(escapeCSVValue(pis)).append("\t");
            String fundingSources = EntryUtil.fundingSourceToString(entry.getEntryFundingSources());
            stringBuilder.append(escapeCSVValue(fundingSources)).append("\t");
            String time = (entry.getCreationTime() == null) ? "" : dateFormat.format(entry.getCreationTime());
            stringBuilder.append(escapeCSVValue(time)).append("\t");
            time = (entry.getModificationTime() == null) ? "" : dateFormat.format(entry.getModificationTime());
            stringBuilder.append(escapeCSVValue(time)).append("\t");
            final String type = entry.getRecordType().toUpperCase();

            switch (type) {
                case ARABIDOPSIS_TYPE:
                    ArabidopsisSeed seed = (ArabidopsisSeed) entry;
                    for (String header : getHeaders(type)) {
                        if (header.equalsIgnoreCase("Homozygosity"))
                            stringBuilder.append(escapeCSVValue(seed.getHomozygosity())).append("\t");
                        if (header.equalsIgnoreCase("Harvest Date")) {
                            time = (entry.getCreationTime() == null) ? "" : dateFormat.format(seed.getHarvestDate());
                            stringBuilder.append(escapeCSVValue(time)).append("\t");
                        }
                        if (header.equalsIgnoreCase("Ecotype"))
                            stringBuilder.append(escapeCSVValue(seed.getEcotype())).append("\t");
                        if (header.equalsIgnoreCase("Parents"))
                            stringBuilder.append(escapeCSVValue(seed.getParents())).append("\t");
                        if (header.equalsIgnoreCase("Generation"))
                            stringBuilder.append(escapeCSVValue(seed.getGeneration())).append("\t");
                        if (header.equalsIgnoreCase("Plant Type"))
                            stringBuilder.append(escapeCSVValue(seed.getPlantType())).append("\t");
                        if (header.equalsIgnoreCase("Sent to ABRC?"))
                            stringBuilder.append(seed.isSentToABRC() ? "Yes" : "No").append("\t");
                    }
                    if (types.contains(PLASMID_TYPE)) {
                        for (int i = 0; i < 4; i += 1)
                            stringBuilder.append("\t");
                    }
                    if (types.contains(STRAIN_TYPE)) {
                        for (int i = 0; i < 3; i += 1)
                            stringBuilder.append("\t");
                    }
                    break;

                case PLASMID_TYPE:
                    Plasmid plasmid = (Plasmid) entry;
                    if (types.contains(ARABIDOPSIS_TYPE)) {
                        for (int i = 0; i < 7; i += 1)
                            stringBuilder.append("\t");
                    }
                    for (String header : getHeaders(type)) {
                        if (header.equalsIgnoreCase("Circular"))
                            stringBuilder.append(plasmid.getCircular() ? "Yes" : "No").append("\t");
                        if (header.equalsIgnoreCase("Backbone"))
                            stringBuilder.append(escapeCSVValue(plasmid.getBackbone())).append("\t");
                        if (header.equalsIgnoreCase("Promoters"))
                            stringBuilder.append(escapeCSVValue(plasmid.getPromoters())).append("\t");
                        if (header.equalsIgnoreCase("Replicates In"))
                            stringBuilder.append(escapeCSVValue(plasmid.getReplicatesIn())).append("\t");
                        if (header.equalsIgnoreCase("Origin of Replication"))
                            stringBuilder.append(escapeCSVValue(plasmid.getOriginOfReplication())).append("\t");
                    }
                    if (types.contains(STRAIN_TYPE)) {
                        for (int i = 0; i < 3; i += 1)
                            stringBuilder.append("\t");
                    }
                    break;


                case STRAIN_TYPE:
                    Strain strain = (Strain) entry;
                    if (types.contains(ARABIDOPSIS_TYPE)) {
                        for (int i = 0; i < 7; i += 1)
                            stringBuilder.append("\t");
                    }
                    if (types.contains(PLASMID_TYPE)) {
                        for (int i = 0; i < 4; i += 1)
                            stringBuilder.append("\t");
                    }
                    for (String header : getHeaders(type)) {
                        if (header.equalsIgnoreCase("Parental Strain"))
                            stringBuilder.append(escapeCSVValue(strain.getHost())).append("\t");
                        if (header.equalsIgnoreCase("Genotype or Phenotype"))
                            stringBuilder.append(escapeCSVValue(strain.getGenotypePhenotype())).append("\t");
                        if (header.equalsIgnoreCase("Plasmids"))
                            stringBuilder.append(escapeCSVValue(strain.getPlasmids())).append("\t");
                    }
                    break;
            }

            stringBuilder.append(attachmentController.hasAttachment(entry) ? "Yes" : "No").append("\t");
            stringBuilder.append((sampleController.hasSample(entry)) ? "Yes" : "No").append("\t");
            stringBuilder.append((sequenceController.hasSequence(entry.getId())) ? "Yes" : "No").append("\n");
        }

        return stringBuilder.toString();
    }
}

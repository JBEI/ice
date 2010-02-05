package org.jbei.ice.web.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.AuthenticatedSampleManager;

public class EntriesXMLExportPage extends XMLExportPage {
    private ArrayList<Entry> entries;

    public EntriesXMLExportPage(ArrayList<Entry> entries) {
        super();

        this.entries = entries;
    }

    @Override
    public String getContent() {
        int index = 1;

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<entries>");

        for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext();) {
            stringBuilder.append("<entry>");

            Entry entry = iterator.next();

            stringBuilder.append("<index>").append(index).append("</index>");
            stringBuilder.append("<type>").append(escapeXMLValue(entry.getRecordType())).append(
                    "</type>");

            stringBuilder.append("<partIds>");
            for (PartNumber partNumber : entry.getPartNumbers()) {
                stringBuilder.append("<partId>").append(escapeXMLValue(partNumber.getPartNumber()))
                        .append("</partId>");
            }
            stringBuilder.append("</partIds>");

            stringBuilder.append("<names>");
            for (PartNumber partNumber : entry.getPartNumbers()) {
                stringBuilder.append("<name>").append(escapeXMLValue(partNumber.getPartNumber()))
                        .append("</name>");
            }
            stringBuilder.append("</names>");
            stringBuilder.append("<owner>");
            stringBuilder.append("<name>").append(escapeXMLValue(entry.getOwner())).append(
                    "</name>");
            stringBuilder.append("<email>").append(escapeXMLValue(entry.getOwnerEmail())).append(
                    "</email>");
            stringBuilder.append("</owner>");
            stringBuilder.append("<creator>");
            stringBuilder.append("<name>").append(escapeXMLValue(entry.getCreator())).append(
                    "</name>");
            stringBuilder.append("<email>").append(escapeXMLValue(entry.getCreatorEmail())).append(
                    "</email>");
            stringBuilder.append("</creator>");

            stringBuilder.append("<alias>").append(escapeXMLValue(entry.getAlias())).append(
                    "</alias>");
            stringBuilder.append("<keywords>").append(escapeXMLValue(entry.getKeywords())).append(
                    "</keywords>");

            stringBuilder.append("<links>");
            for (Link link : entry.getLinks()) {
                stringBuilder.append("<link>").append(escapeXMLValue(link.getLink())).append(
                        "</link>");
                stringBuilder.append("<url>").append(escapeXMLValue(link.getUrl()))
                        .append("</url>");
            }
            stringBuilder.append("</links>");

            stringBuilder.append("<status>").append(escapeXMLValue(entry.getStatus())).append(
                    "</status>");
            stringBuilder.append("<summary>").append(escapeXMLValue(entry.getShortDescription()))
                    .append("</summary>");
            stringBuilder.append("<notes>").append(escapeXMLValue(entry.getLongDescription()))
                    .append("</notes>");
            stringBuilder.append("<references>").append(escapeXMLValue(entry.getReferences()))
                    .append("</references>");

            if (entry instanceof Plasmid) {
                Plasmid plasmid = (Plasmid) entry;

                stringBuilder.append("<selectionMarkers>").append(
                        escapeXMLValue(plasmid.getSelectionMarkersAsString())).append(
                        "</selectionMarkers>");
                stringBuilder.append("<backbone>").append(escapeXMLValue(plasmid.getBackbone()))
                        .append("</backbone>");
                stringBuilder.append("<originOfReplication>").append(
                        escapeXMLValue(plasmid.getOriginOfReplication())).append(
                        "</originOfReplication>");
                stringBuilder.append("<promoters>").append(escapeXMLValue(plasmid.getPromoters()))
                        .append("</promoters>");
                stringBuilder.append("<isCircular>").append(plasmid.getCircular() ? "Yes" : "No")
                        .append("</isCircular>");
            } else if (entry instanceof Strain) {
                Strain strain = (Strain) entry;

                stringBuilder.append("<selectionMarkers>").append(
                        escapeXMLValue(strain.getSelectionMarkersAsString())).append(
                        "</selectionMarkers>");
                stringBuilder.append("<host>").append(escapeXMLValue(strain.getHost())).append(
                        "</host>");
                stringBuilder.append("<genotypePhenotype>").append(
                        escapeXMLValue(strain.getGenotypePhenotype())).append(
                        "</genotypePhenotype>");
                stringBuilder.append("<plasmids>").append(escapeXMLValue(strain.getPlasmids()))
                        .append("</plasmids>");
            } else if (entry instanceof Part) {
                Part part = (Part) entry;

                stringBuilder.append("<packageFormat>").append(
                        escapeXMLValue(part.getPackageFormat())).append("</packageFormat>");
            }

            stringBuilder.append("<hasAttachments>").append(
                    (AttachmentManager.hasAttachment(entry)) ? "Yes" : "No").append(
                    "</hasAttachments>");
            stringBuilder.append("<hasSamples>").append(
                    (AuthenticatedSampleManager.hasSample(entry)) ? "Yes" : "No").append(
                    "</hasSamples>");
            stringBuilder.append("<hasSequence>").append(
                    (SequenceManager.hasSequence(entry)) ? "Yes" : "No").append("</hasSequence>");

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");

            stringBuilder.append("<created>").append(
                    (entry.getCreationTime() == null) ? "" : dateFormat.format(entry
                            .getCreationTime())).append("</created>");
            stringBuilder.append("<updated>").append(
                    (entry.getModificationTime() == null) ? "" : dateFormat.format(entry
                            .getModificationTime())).append("</updated>");

            stringBuilder.append("</entry>");
            index++;
        }
        stringBuilder.append("</entries>");

        return stringBuilder.toString();
    }
}

package org.jbei.ice.web.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;

public class EntryXMLExportPage extends XMLExportPage {
    private ArrayList<Entry> entries;

    public EntryXMLExportPage(ArrayList<Entry> entries) {
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
            stringBuilder.append("<type>").append(escapeValue(entry.getRecordType())).append(
                    "</type>");
            stringBuilder.append("<partId>").append(
                    escapeValue(entry.getOnePartNumber().getPartNumber())).append("</partId>");
            stringBuilder.append("<name>").append(escapeValue(entry.getOneName().getName()))
                    .append("</name>");
            stringBuilder.append("<owner>").append(escapeValue(entry.getOwner()))
                    .append("</owner>");
            stringBuilder.append("<creator>").append(escapeValue(entry.getCreator())).append(
                    "</creator>");
            stringBuilder.append("<alias>").append(escapeValue(entry.getAlias()))
                    .append("</alias>");
            stringBuilder.append("<keywords>").append(escapeValue(entry.getKeywords())).append(
                    "</keywords>");
            stringBuilder.append("<links>").append(escapeValue(entry.getLinks()))
                    .append("</links>");
            stringBuilder.append("<status>").append(escapeValue(entry.getStatus())).append(
                    "</status>");
            stringBuilder.append("<summary>").append(escapeValue(entry.getShortDescription()))
                    .append("</summary>");
            stringBuilder.append("<notes>").append(escapeValue(entry.getLongDescription())).append(
                    "</notes>");
            stringBuilder.append("<references>").append(escapeValue(entry.getReferences())).append(
                    "</references>");

            if (entry instanceof Plasmid) {
                Plasmid plasmid = (Plasmid) entry;

                stringBuilder.append("<selectionMarkers>").append(
                        escapeValue(plasmid.getSelectionMarkersAsString())).append(
                        "</selectionMarkers>");
                stringBuilder.append("<backbone>").append(escapeValue(plasmid.getBackbone()))
                        .append("</backbone>");
                stringBuilder.append("<originOfReplication>").append(
                        escapeValue(plasmid.getOriginOfReplication())).append(
                        "</originOfReplication>");
                stringBuilder.append("<promoters>").append(escapeValue(plasmid.getPromoters()))
                        .append("</promoters>");
                stringBuilder.append("<isCircular>").append(plasmid.getCircular() ? "Yes" : "No")
                        .append("</isCircular>");
            } else if (entry instanceof Strain) {
                Strain strain = (Strain) entry;

                stringBuilder.append("<selectionMarkers>").append(
                        escapeValue(strain.getSelectionMarkersAsString())).append(
                        "</selectionMarkers>");
                stringBuilder.append("<host>").append(escapeValue(strain.getHost())).append(
                        "</host>");
                stringBuilder.append("<genotypePhenotype>").append(
                        escapeValue(strain.getGenotypePhenotype())).append("</genotypePhenotype>");
                stringBuilder.append("<plasmids>").append(escapeValue(strain.getPlasmids()))
                        .append("</plasmids>");
            } else if (entry instanceof Part) {
                Part part = (Part) entry;

                stringBuilder.append("<packageFormat>")
                        .append(escapeValue(part.getPackageFormat())).append("</packageFormat>");
            }

            stringBuilder.append("<hasAttachments>").append(
                    (AttachmentManager.hasAttachment(entry)) ? "Yes" : "No").append(
                    "</hasAttachments>");
            stringBuilder.append("<hasSamples>").append(
                    (SampleManager.hasSample(entry)) ? "Yes" : "No").append("</hasSamples>");
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

package org.jbei.ice.web.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.AuthenticatedSampleManager;
import org.jbei.ice.lib.utils.JbeiConstants;

public class EntriesCurrentFieldsExcelExportPage extends ExcelExportPage {
    private ArrayList<Entry> entries;

    public EntriesCurrentFieldsExcelExportPage(ArrayList<Entry> entries) {
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

        stringBuilder
                .append("#\tType\tPart ID\tName\tSummary\tOwner\tStatus\tHas Attachment\tHas Samples\tHas Sequence\tCreated\n");

        for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext();) {
            Entry entry = iterator.next();

            stringBuilder.append(index).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getRecordType())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getOnePartNumber().getPartNumber())).append(
                    "\t");
            stringBuilder.append(escapeCSVValue(entry.getOneName().getName())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getShortDescription())).append("\t");
            stringBuilder.append(escapeCSVValue(entry.getOwner())).append("\t");
            stringBuilder.append(JbeiConstants.getStatus(entry.getStatus())).append("\t");
            stringBuilder.append((AttachmentManager.hasAttachment(entry)) ? "Yes" : "No").append(
                    "\t");
            stringBuilder.append((AuthenticatedSampleManager.hasSample(entry)) ? "Yes" : "No")
                    .append("\t");
            stringBuilder.append((SequenceManager.hasSequence(entry)) ? "Yes" : "No").append("\t");
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
            String dateString = dateFormat.format(entry.getCreationTime());
            stringBuilder.append(escapeCSVValue(dateString)).append("\t");
            stringBuilder.append("\n");

            index++;
        }

        return stringBuilder.toString();
    }
}

package org.jbei.ice.web.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

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

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("#\tType\tPart ID\tName\tSummary\tOwner\tStatus\tHas Attachment\tHas Samples\tHas Sequence\tCreated\n");

        try {
            for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext();) {
                Entry entry = iterator.next();

                stringBuilder.append(index).append("\t");
                stringBuilder.append(escapeCSVValue(entry.getRecordType())).append("\t");
                stringBuilder.append(escapeCSVValue(entry.getOnePartNumber().getPartNumber()))
                        .append("\t");
                stringBuilder.append(escapeCSVValue(entry.getOneName().getName())).append("\t");
                stringBuilder.append(escapeCSVValue(entry.getShortDescription())).append("\t");
                stringBuilder.append(escapeCSVValue(entry.getOwner())).append("\t");
                stringBuilder.append(JbeiConstants.getStatus(entry.getStatus())).append("\t");
                stringBuilder.append((entryController.hasAttachments(entry)) ? "Yes" : "No")
                        .append("\t");
                stringBuilder.append((entryController.hasSamples(entry)) ? "Yes" : "No").append(
                        "\t");
                stringBuilder.append((entryController.hasSequence(entry)) ? "Yes" : "No").append(
                        "\t");
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String dateString = dateFormat.format(entry.getCreationTime());
                stringBuilder.append(escapeCSVValue(dateString)).append("\t");
                stringBuilder.append("\n");

                index++;
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return stringBuilder.toString();
    }
}

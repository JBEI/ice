package org.jbei.ice.lib.utils;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;

import org.jbei.ice.lib.bulkupload.BulkCSVUploadHeaders;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.SampleController;

import org.apache.commons.lang.StringUtils;

/**
 * ICE to CSV format
 *
 * @author Hector Plahar
 */
public class IceCSVSerializer {

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

    private static HashSet<String> getHeaders(EntryType type) {
        HashSet<String> headers = new HashSet<>();
        List<EntryField> fields = BulkCSVUploadHeaders.getHeadersForType(type);
        for (EntryField field : fields) {
            headers.add(field.getLabel());
        }

        return headers;
    }

    public static String serialize(Entry entry) {
        final EntryType type = EntryType.nameToType(entry.getRecordType());
        List<EntryField> fields = BulkCSVUploadHeaders.getHeadersForType(type);

        StringBuilder stringBuilder = new StringBuilder();

        for (EntryField field : fields) {
            stringBuilder.append(field.getLabel()).append(",");
        }
        stringBuilder.append("Has Attachment").append(",");
        stringBuilder.append("Has Samples").append(",");
        stringBuilder.append("Has Sequence").append("\n");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        SampleController sampleController = new SampleController();
        AttachmentController attachmentController = new AttachmentController();

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

        for (EntryField field : fields) {
            String value = EntryUtil.entryFieldToValue(entry, field);
            stringBuilder.append(escapeCSVValue(value)).append(",");
        }


//                    if (types.contains(PLASMID_TYPE)) {
//                        for (int i = 0; i < 4; i += 1)
//                            stringBuilder.append(",");
//                    }
//                    if (types.contains(STRAIN_TYPE)) {
//                        for (int i = 0; i < 3; i += 1)
//                            stringBuilder.append(",");
//                    }

        stringBuilder.append(attachmentController.hasAttachment(entry) ? "Yes" : "No").append(",");
        stringBuilder.append((sampleController.hasSample(entry)) ? "Yes" : "No").append(",");
        stringBuilder.append((DAOFactory.getSequenceDAO().hasSequence(entry.getId())) ? "Yes" : "No").append("\n");

        // todo : add option for samples

        return stringBuilder.toString();
    }
}

package org.jbei.ice.lib.utils;

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

        SampleController sampleController = new SampleController();
        AttachmentController attachmentController = new AttachmentController();

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

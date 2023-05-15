package org.jbei.ice.storage;

import org.jbei.ice.dto.entry.*;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Attachment;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.EntryFieldValueModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for converting {@link Entry}s to a {@link PartData}
 * data transfer objects
 *
 * @author Hector Plahar
 */
public class ModelToInfoFactory {

    public static PartData getInfo(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            throw new IllegalArgumentException("Invalid entry type: " + entry.getRecordType());

        return new PartData(type);
    }

    public static ArrayList<AttachmentInfo> getAttachments(List<Attachment> attachments, boolean canEdit) {
        ArrayList<AttachmentInfo> infos = new ArrayList<>();
        if (attachments == null)
            return infos;

        for (Attachment attachment : attachments) {
            AttachmentInfo info = new AttachmentInfo();
            info.setDescription(attachment.getDescription());
            info.setFilename(attachment.getFileName());
            info.setId(attachment.getId());
            info.setCanEdit(canEdit);
            info.setFileId(attachment.getFileId());
            infos.add(info);
        }

        return infos;
    }

    private static PartData getTipViewCommon(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        PartData view = new PartData(type);
        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(entry.getPartNumber());
        return view;
    }


    public static PartData createTableView(long entryId) {
        // minimum set of values
        Entry entry = DAOFactory.getEntryDAO().get(entryId);
        EntryType type = EntryType.nameToType(entry.getRecordType());
        PartData view = new PartData(type);
        view.setId(entry.getId());
        view.setRecordId(entry.getRecordId());
        view.setPartId(entry.getPartNumber());
        view.setCreationTime(entry.getCreationTime().getTime());

        // get entry details
        List<EntryFieldValueModel> values = DAOFactory.getEntryFieldValueModelDAO().getFieldsForEntry(entryId, EntryFieldLabel.getTableViewFields());
        for (EntryFieldValueModel valueModel : values) {
            view.getFields().add(valueModel.toDataTransferObject());
        }

        // has sample
        view.setHasSample(DAOFactory.getSampleDAO().hasSample(entry));

        // has sequence
        Visibility visibility = Visibility.valueToEnum(entry.getVisibility());
        view.setVisibility(visibility);
        if (visibility == Visibility.REMOTE) {
        } else {
            SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();
            view.setHasSequence(sequenceDAO.hasSequence(entry.getId()));
            view.setHasOriginalSequence(sequenceDAO.hasOriginalSequence(entry.getId()));
        }

        return view;
    }

    public static PartData createTipView(Entry entry) {
        EntryType type = EntryType.nameToType(entry.getRecordType());
        if (type == null)
            throw new IllegalArgumentException("Invalid entry type " + entry.getRecordType());

        return getTipViewCommon(entry);
    }
}

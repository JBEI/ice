package org.jbei.ice.storage;

import org.jbei.ice.dto.entry.*;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Attachment;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.EntryFieldValueModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


    public static PartData createTableView(long entryId, List<String> fields) {
        Set<String> fieldsToProcess;
        if (fields == null)
            fieldsToProcess = new HashSet<>();
        else
            fieldsToProcess = new HashSet<>(fields);

        fieldsToProcess.add("name");
        fieldsToProcess.add("status");
        fieldsToProcess.add("recordType");
        fieldsToProcess.add("creation_time");
        fieldsToProcess.add("short_description");

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

//        view.setName(entry.getName());
//        view.setShortDescription(entry.getShortDescription());
//        view.setCreationTime(entry.getCreationTime().getTime());
//        view.setStatus(entry.getStatus());
//        view.setShortDescription(entry.getShortDescription());

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

        // optional values
        if (fieldsToProcess.contains("alias")) {
        }

        if (fieldsToProcess.contains("links")) {
            for (Entry linkedEntry : entry.getLinkedEntries()) {
                PartData linkedPartData = new PartData(EntryType.nameToType(linkedEntry.getRecordType()));
                linkedPartData.setId(linkedEntry.getId());
                view.getLinkedParts().add(linkedPartData);
            }

            List<Entry> parents = DAOFactory.getEntryDAO().getParents(entry.getId());
            if (parents != null) {
                for (Entry parentEntry : parents) {
                    PartData partData = new PartData(EntryType.nameToType(parentEntry.getRecordType()));
                    partData.setId(parentEntry.getId());
                    view.getParents().add(partData);
                }
            }
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

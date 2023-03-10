package org.jbei.ice.entry;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.entry.*;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldDAO;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldValueDAO;
import org.jbei.ice.storage.hibernate.dao.EntryFieldValueModelDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.EntryFieldValueModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Field values for specified entry types. Maps the fields to built in types or creates new custom fields as needed
 *
 * @author Hector Plahar
 */
public class EntryFieldValues {

    private final String userId;
    private final Entry entry;
    private final CustomEntryFieldValueDAO entryFieldValueDAO;
    private final CustomEntryFieldDAO customFieldDAO;
    private final EntryFieldValueModelDAO dao;
    private final List<EntryField> fields;
    private final EntryAuthorization authorization;
    private boolean validated;

    public EntryFieldValues(String userId, String entryId, List<EntryField> fields) {
        this.userId = userId;
        this.entry = new HasEntry().getEntry(entryId);
        if (this.entry == null)
            throw new IllegalArgumentException("Invalid entry id: " + entryId);

        this.entryFieldValueDAO = DAOFactory.getCustomEntryFieldValueDAO();
        this.customFieldDAO = DAOFactory.getCustomEntryFieldDAO();
        this.dao = DAOFactory.getEntryFieldValueModelDAO();
        this.fields = fields;
        this.authorization = new EntryAuthorization();
    }

    /**
     * @return list of field labels that do not validate
     */
    public List<String> validates() {
        List<String> invalidLabels = new ArrayList<>();
        if (this.fields == null)
            return invalidLabels;

        for (EntryField field : this.fields) {

            // validate standard fields
            // TODO : if existing field is not null, else use label instead
            if (field.isRequired() && StringUtils.isEmpty(field.getValue())) {
                invalidLabels.add(field.getLabel());
            }

            // check
        }

        this.validated = true;
        return invalidLabels;
    }

    public PartData set() {
        if (!this.validated)
            throw new IllegalStateException("Validation required");

        authorization.expectWrite(userId, this.entry);

        PartData partData = new PartData(EntryType.nameToType(entry.getRecordType()));
        partData.setId(this.entry.getId());

        if (fields == null || fields.isEmpty())
            return partData;

        for (EntryField field : fields) {
            // empty value but not required
            if (StringUtils.isEmpty(field.getValue()))
                continue;

            update(entry, field);
        }

        // update visibility if necessary
        if (entry.getVisibility() != Visibility.OK.getValue()) {
            entry.setVisibility(Visibility.OK.getValue());
            DAOFactory.getEntryDAO().update(entry);
        }

        return partData;

    }

    private void update(Entry entry, EntryField field) {
        EntryFieldValueModel model = this.dao.get(field.getId());
        if (model == null) {
            model = new EntryFieldValueModel();
            model.setEntry(entry);
            model.setValue(field.getValue());
            model.setLabel(EntryFieldLabel.fromLabel(null, field.getLabel()));
            this.dao.create(model);
        } else {

        }
        // todo: if custom, update custom below
//        CustomEntryFieldModel customEntryFieldModel = customFieldDAO.get(field.getId());
//        if (customEntryFieldModel == null) {
//            // get details about custom field (note: this is different from value)
//            EntryType type = EntryType.nameToType(entry.getRecordType());
//
//            // try again with label and type
//            Optional<CustomEntryFieldModel> optional = customFieldDAO.getLabelForType(type, field.getLabel());
//
//            // existing fields will not have custom fields created
//            if (field.isCustom() && optional.isEmpty()) {
//                Logger.error("Could not retrieve custom field with id " + field.getId());
//                return;
//            }
//            customEntryFieldModel = optional.orElse(null);
//        }
//
//        CustomEntryFieldValueModel model = null;
//        if (customEntryFieldModel != null)
//            model = entryFieldValueDAO.getByFieldAndEntry(entry, customEntryFieldModel);
//
//        if (model == null) {
//            // create new
//            model = new CustomEntryFieldValueModel();
//            model.setEntry(entry);
//            model.setField(customEntryFieldModel);
//            model.setValue(field.getValue());
//            entryFieldValueDAO.create(model);
//        } else {
//            model.setValue(field.getValue());
//            entryFieldValueDAO.update(model);
//        }
    }
}

package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldDAO;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldValueDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.ParameterDAO;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author Hector Plahar
 */
public class CustomFields {

    private final ParameterDAO dao;
    private final EntryDAO entryDAO;
    private final CustomEntryFieldDAO customEntryFieldDAO;
    private EntryAuthorization authorization;

    public CustomFields() {
        this.authorization = new EntryAuthorization();
        this.dao = DAOFactory.getParameterDAO();
        this.entryDAO = DAOFactory.getEntryDAO();
        this.customEntryFieldDAO = DAOFactory.getCustomEntryFieldDAO();
    }

    public CustomField createField(String userId, long partId, CustomField field) {
        Entry entry = entryDAO.get(partId);
        authorization.expectWrite(userId, entry);

        Parameter parameter = new Parameter();
        parameter.setEntry(entry);
        parameter.setKey(field.getName());
        parameter.setValue(field.getValue());
        entry.getParameters().add(parameter);
        return this.dao.create(parameter).toDataTransferObject();
    }

    public CustomField updateField(String userId, long id, CustomField customField) {
        Parameter parameter = dao.get(id);
        if (parameter == null)
            return null;

        Entry entry = parameter.getEntry();
        authorization.expectWrite(userId, entry);

        parameter.setValue(customField.getValue());
        parameter.setKey(customField.getName());
        return dao.update(parameter).toDataTransferObject();
    }

    public CustomField getField(String userId, long id) {
        Parameter parameter = dao.get(id);
        if (parameter == null)
            return null;

        Entry entry = parameter.getEntry();
        authorization.expectRead(userId, entry);
        return parameter.toDataTransferObject();
    }

    public List<CustomField> getFieldsForPart(String userId, long partId) {
        Entry entry = entryDAO.get(partId);
        authorization.expectRead(userId, entry);

        List<CustomField> result = new ArrayList<>();
        if (entry.getParameters() == null)
            return result;

        for (Parameter parameter : entry.getParameters()) {
            result.add(parameter.toDataTransferObject());
        }
        return result;
    }

    public List<PartData> getPartsByFields(String userId, List<CustomField> fields) {
        // todo : performance
        List<Entry> entries = dao.filter(fields);
        List<PartData> parts = new ArrayList<>();
        for (Entry entry : entries) {
            if (!authorization.canRead(userId, entry))
                continue;

            parts.add(entry.toDataTransferObject());
        }

        return parts;
    }

    /**
     * Deletes the custom field specified by the unique identifier in the parameter.
     * The user must have write privileges on field associated with entry
     *
     * @param userId account identifier for user performing the action
     * @param id     unique identifier for custom field
     * @return true if field is found and deleted successfully, false otherwise (including when the field is not found)
     */
    public boolean deleteField(String userId, long id) {
        Parameter parameter = dao.get(id);
        if (parameter == null)
            return false;

        Entry entry = parameter.getEntry();
        authorization.expectWrite(userId, entry);
        entry.getParameters().remove(parameter);
        dao.delete(parameter);
        return true;
    }

    public CustomEntryField create(String userId, CustomEntryField customField) {
        authorization.expectAdmin(userId);
        Optional<CustomEntryFieldModel> result = customEntryFieldDAO.getLabelForType(customField.getEntryType(), customField.getLabel());
        if (result.isPresent() && !result.get().getDisabled())
            throw new IllegalArgumentException("Field with label \"" + customField.getLabel() + "\" already exists for entry type \"" + customField.getEntryType() + "\"");

        CustomEntryFieldModel model = new CustomEntryFieldModel();
        model.setLabel(customField.getLabel());
        model.setRequired(customField.isRequired());
        model.setFieldType(customField.getFieldType()); // todo : validate field type
        model.setEntryType(customField.getEntryType()); // todo : validate
        model.setExistingField(customField.getExistingField());

        for (CustomField field : customField.getOptions()) {
            model.getCustomFieldLabels().add(new CustomEntryFieldOptionModel(field.getValue()));
        }

        model = customEntryFieldDAO.create(model);
        return model.toDataTransferObject();
    }

    public Results<CustomEntryField> get(EntryType entryType) {
        Results<CustomEntryField> fields = new Results<>();

        List<CustomEntryFieldModel> results = customEntryFieldDAO.getFieldsForType(entryType, false);
        for (CustomEntryFieldModel model : results) {
            fields.getData().add(model.toDataTransferObject());
        }
        return fields;
    }

    public List<CustomEntryField> getCustomFieldValuesForPart(long partId) {
        Entry entry = entryDAO.get(partId);
        List<CustomEntryField> fields = new ArrayList<>();
        if (entry == null)
            return fields;

        // get fields for entry type
        EntryType type = EntryType.nameToType(entry.getRecordType());
        List<CustomEntryFieldModel> typeFields = customEntryFieldDAO.getFieldsForType(type, false);

        // get custom fields values
        CustomEntryFieldValueDAO dao = DAOFactory.getCustomEntryFieldValueDAO();
        List<CustomEntryFieldValueModel> results = dao.getByEntry(entry);
        for (CustomEntryFieldValueModel valueModel : results) {
            fields.add(valueModel.toDataTransferObject());
        }

        // add entry field types that are not in custom field values
        Iterator<CustomEntryFieldModel> iterator = typeFields.iterator();
        while (iterator.hasNext()) {
            CustomEntryFieldModel next = iterator.next();
            for (CustomEntryFieldValueModel valueModel : results) {
                if (next.getLabel().equals(valueModel.getField().getLabel())) {
                    iterator.remove();
                    break;
                }
            }
        }

        for (CustomEntryFieldModel model : typeFields) {
            fields.add(model.toDataTransferObject());
        }

        return fields;
    }

    public void deleteCustomField(String userId, EntryType type, long fieldId) {
        authorization.expectAdmin(userId);
        CustomEntryFieldDAO dao = customEntryFieldDAO;

        CustomEntryFieldModel value = dao.get(fieldId);
        if (value == null || value.getEntryType() != type)
            throw new IllegalArgumentException("Could not retrieve custom field using specified parameters [" + fieldId + ", " + type + "]");

        value.setDisabled(true);
        dao.update(value);
    }
}

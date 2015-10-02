package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.ParameterDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class CustomFields {

    private EntryAuthorization authorization;
    private final ParameterDAO dao;

    public CustomFields() {
        this.authorization = new EntryAuthorization();
        this.dao = DAOFactory.getParameterDAO();
    }

    public CustomField createField(String userId, long partId, CustomField field) {
        EntryDAO entryDAO = DAOFactory.getEntryDAO();

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
        EntryDAO entryDAO = DAOFactory.getEntryDAO();
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
        Set<Entry> entries = dao.filter(fields);
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
}

package org.jbei.ice.entry;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.entry.*;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryFieldValueModelDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.EntryFieldValueModel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Entry fields for a specific entry
 *
 * @author Hector Plahar
 */
public class EntryFields {

    private final String userId;
    private final EntryFieldValueModelDAO dao;

    /**
     * @param userId unique identifier of user making request
     */
    public EntryFields(String userId) {
        this.userId = userId;
        this.dao = DAOFactory.getEntryFieldValueModelDAO();
    }

    public EntryField update(String entryId, long fieldId, EntryField field) {
        Entry entry = new HasEntry().getEntry(entryId);
        if (entry == null)
            throw new IllegalArgumentException("Cannot locate entry with id: " + entryId);

        EntryFieldValueModel model = dao.get(fieldId);
        if (model.getEntry().getId() != entry.getId())
            throw new IllegalArgumentException("Invalid field");

        if (model.getLabel() != field.getFieldType())
            throw new IllegalArgumentException("Invalid field");

        model.setValue(field.getValue());
        dao.update(model);
        return model.toDataTransferObject();
    }

    /**
     * Determines which entry type fields to return
     *
     * @return List of {@link EntryField}
     */
    public List<EntryField> get(EntryType type) {
        if (type == null)
            throw new IllegalArgumentException("Illegal null entry type");

        List<EntryFieldLabel> labels = switch (type) {
            default -> EntryFieldLabel.getPartLabels();
            case PLASMID -> EntryFieldLabel.getPlasmidLabels();
            case STRAIN -> EntryFieldLabel.getStrainLabels();
            case SEED -> EntryFieldLabel.getSeedLabels();
            case PROTEIN -> EntryFieldLabel.getProteinFields();
        };

        // retrieves the default labels for the specific entry types

        // retrieve the entry fields for the retrieved labels
        return getFieldsForLabels(type, labels);
    }

    /**
     * Retrieve associated {@link EntryField}s for the referenced labels
     *
     * @param labels list of {@link EntryFieldLabel}s that are to be used for retrieval
     * @return list of fields
     */
    private List<EntryField> getFieldsForLabels(EntryType type, List<EntryFieldLabel> labels) {
        List<EntryField> fields = new LinkedList<>();
        Set<EntryFieldLabel> existingCustomFields = new HashSet<>();

        // retrieve custom fields created on this instance of ICE
        CustomFields customFields = new CustomFields();
        List<CustomEntryField> customEntryFields = customFields.get(type).getData();

        // iterate through the list of custom entries
        for (CustomEntryField customEntryField : customEntryFields) {
            EntryField field = new EntryField();
            switch (customEntryField.getFieldType()) {

                // keep track of existing field customizations to avoid duplicating it when retrieving
                // regular fields
                case EXISTING -> {
                    existingCustomFields.add(customEntryField.getExistingField());
                    field.setFieldInputType(customEntryField.getExistingField().getFieldType());
                }

                case MULTI_CHOICE, MULTI_CHOICE_PLUS -> field.setFieldInputType(FieldInputType.SELECT);
            }

            // create and add entry field
            field.setRequired(customEntryField.isRequired());
            field.setCustom(true);
            field.setId(customEntryField.getId());
            field.setValue(customEntryField.getValue());
            field.setLabel(customEntryField.getLabel());
            field.getOptions().addAll(customEntryField.getOptions());
            fields.add(field);
        }

        // default values for entries
        PartDefaults partDefaults = new PartDefaults(this.userId);

        // get regular fields using list of labels
        for (EntryFieldLabel label : labels) {
            // skip any that have been modified via custom fields since it is already accounted for
            if (existingCustomFields.contains(label))
                continue;

            // create and add entry field
            EntryField field = new EntryField();
            field.setLabel(label.getDisplay());
            field.setFieldType(label);
            field.setFieldInputType(label.getFieldType());

            // get the user set default value for field label
            String defaultValue = partDefaults.getForLabel(label);
            if (!StringUtils.isEmpty(defaultValue))
                field.setValue(defaultValue);

            field.setRequired(label.isRequired());

            // retrieve options (should be restricted to only type SELECT)
            field.getOptions().addAll(EntryFieldLabel.getDefaultOptions(label));
            fields.add(field);
        }

        return fields;
    }
}

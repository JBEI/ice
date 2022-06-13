package org.jbei.ice.entry;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.entry.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Entry fields for a specific entry
 *
 * @author Hector Plahar
 */
public class PartFields {

    private final EntryType type;
    private final String userId;

    /**
     * @param userId unique identifier of user making request
     * @param type   type of entry
     */
    public PartFields(String userId, EntryType type) {
        if (type == null)
            throw new IllegalArgumentException("Illegal null entry type");

        this.type = type;
        this.userId = userId;
    }

    /**
     * Determines which entry type fields to return
     *
     * @return List of {@link EntryField}
     */
    public List<EntryField> get() {
        List<EntryFieldLabel> labels;

        // retrieves the default labels for the specific entry types
        switch (this.type) {
            case PART:
            default:
                labels = EntryFieldLabel.getPartLabels();
                break;

            case PLASMID:
                labels = EntryFieldLabel.getPlasmidLabels();
                break;

            case STRAIN:
                labels = EntryFieldLabel.getStrainLabels();
                break;

            case SEED:
                labels = EntryFieldLabel.getSeedLabels();
                break;

            case PROTEIN:
                labels = EntryFieldLabel.getProteinFields();
                break;
        }

        // retrieve the entry fields for the retrieved labels
        return getFieldsForLabels(labels);
    }

    /**
     * Retrieve associated {@link EntryField}s for the referenced labels
     *
     * @param labels list of {@link EntryFieldLabel}s that are to be used for retrieval
     * @return list of fields
     */
    private List<EntryField> getFieldsForLabels(List<EntryFieldLabel> labels) {
        List<EntryField> fields = new LinkedList<>();
        Set<EntryFieldLabel> existingCustomFields = new HashSet<>();

        // retrieve custom fields created on this instance of ICE
        CustomFields customFields = new CustomFields();
        List<CustomEntryField> customEntryFields = customFields.get(this.type).getData();

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
            field.setEntryType(this.type);
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
            // skip any that have been modified via custom fields since it is already account for
            if (existingCustomFields.contains(label))
                continue;

            // create and add entry field
            EntryField field = new EntryField();
            field.setLabel(label.getDisplay());
            field.setEntryType(this.type);
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

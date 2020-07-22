package org.jbei.ice.lib.entry;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.entry.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entry fields for a specific entry
 */
public class PartFields {

    private final EntryType type;
    private final String userId;

    public PartFields(String userId, EntryType type) {
        if (type == null)
            throw new IllegalArgumentException("Illegal null entry type");

        this.type = type;
        this.userId = userId;
    }

    /**
     * Determines which entry type fields to return and retrieves the appropriate fields
     *
     * @return List of {@link EntryField}
     */
    public List<EntryField> get() {
        List<EntryFieldLabel> labels;
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

        return getFieldsForLabels(labels);
    }

    private List<EntryField> getFieldsForLabels(List<EntryFieldLabel> labels) {
        List<EntryField> fields = new ArrayList<>();

        //
        Set<EntryFieldLabel> existingCustomFields = new HashSet<>();
        PartDefaults partDefaults = new PartDefaults(this.userId);

        // get custom fields
        CustomFields customFields = new CustomFields();
        List<CustomEntryField> customEntryFields = customFields.get(this.type).getData();
        for (CustomEntryField customEntryField : customEntryFields) {

            // keep track of existing field customizations to avoid duplicating it when retrieving
            // regular fields
            if (customEntryField.getFieldType() == FieldType.EXISTING) {
                existingCustomFields.add(customEntryField.getExistingField());
            }

            EntryField field = new EntryField();
            field.setRequired(customEntryField.isRequired());
            field.setEntryType(this.type);
            field.setCustom(true);

            field.setId(customEntryField.getId());
            field.setValue(customEntryField.getValue());
            field.setLabel(customEntryField.getLabel());
            field.getOptions().addAll(customEntryField.getOptions());
            fields.add(field);
        }

        // get regular fields
        for (EntryFieldLabel label : labels) {
            // skip any that have been modified via custom fields
            if (existingCustomFields.contains(label))
                continue;

            EntryField field = new EntryField();
            field.setLabel(label.getLabel());
            field.setEntryType(this.type);
            String defaultValue = partDefaults.getForLabel(label);
            if (!StringUtils.isEmpty(defaultValue))
                field.setValue(defaultValue);

            field.setRequired(label.isRequired());
            field.getOptions().addAll(EntryFieldLabel.getDefaultOptions(label));

            fields.add(field);
        }

        return fields;
    }
}

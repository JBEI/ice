package org.jbei.ice.storage.model;

import org.jbei.ice.lib.dto.entry.CustomField;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

/**
 * Option value for custom entry fields
 *
 * @author Hector Plahar
 */

@Entity
@Table(name = "custom_entry_field_option_model")
@SequenceGenerator(name = "custom_entry_field_option_model_id", sequenceName = "custom_entry_field_option_model_id_seq",
        allocationSize = 1)
public class CustomEntryFieldOptionModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "custom_entry_field_option_model_id")
    private long id;

    @Column(name = "label")
    private String label;

    @Override
    public long getId() {
        return id;
    }

    public CustomEntryFieldOptionModel() {
    }

    public CustomEntryFieldOptionModel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public CustomField toDataTransferObject() {
        return new CustomField(label, "");
    }
}

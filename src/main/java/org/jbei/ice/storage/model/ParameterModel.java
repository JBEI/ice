package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.jbei.ice.dto.entry.CustomField;
import org.jbei.ice.storage.DataModel;

/**
 * Stores key-value information for {@link org.jbei.ice.storage.model.Entry}.
 * <p/>
 * Parameter should be used sparingly. If a parameter seems universal to entry or to part type, that
 * is, many entries have the same parameters, then they should become a field in the appropriate
 * object.
 * <p/>
 * Parameters should be serialized for full text search as "parameter name=parameter value", so they
 * can be searched for as key-value pairs (e.g. "key=value").
 *
 * @author Timothy Ham
 */
@Entity
@Table(name = "parameters")
@SequenceGenerator(name = "parameters_id", sequenceName = "parameters_id_seq", allocationSize = 1)
public class ParameterModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "parameters_id")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false, unique = false)
    private org.jbei.ice.storage.model.Entry entry;

    @Column(name = "parameter_type", nullable = false)
    private String parameterType;

    @Column(name = "\"key\"", length = 255, nullable = false)
    private String key;

    @Column(name = "\"value\"", length = 4095, nullable = false)
    private String value;

    public ParameterModel() {
        parameterType = "";
    }

    public org.jbei.ice.storage.model.Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public CustomField toDataTransferObject() {
        return new CustomField(this.id, entry.getId(), key, value);
    }
}

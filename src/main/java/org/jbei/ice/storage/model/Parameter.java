package org.jbei.ice.storage.model;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.jbei.ice.lib.dto.entry.CustomField;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

/**
 * Stores key-value information for {@link Entry}.
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
@SequenceGenerator(name = "sequence", sequenceName = "parameters_id_seq", allocationSize = 1)
public class Parameter implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @ContainedIn
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false, unique = false)
    private Entry entry;

    @Column(name = "parameter_type", nullable = false)
    private String parameterType;

    @Column(name = "key", length = 255, nullable = false)
    private String key;

    @Column(name = "value", length = 4095, nullable = false)
    @Field
    private String value;

    public Parameter() {
        parameterType = "";
    }

    public Entry getEntry() {
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

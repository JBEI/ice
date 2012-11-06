package org.jbei.ice.lib.entry.model;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.*;

import org.jbei.ice.lib.dao.IModel;

// TODO Make advanced search filter for parameters

/**
 * Stores key-value information for {@link org.jbei.ice.lib.entry.model.Entry}.
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
public class Parameter implements IModel {

    private static final long serialVersionUID = 1L;

    public enum ParameterType {
        NUMBER, BOOLEAN, TEXT;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false, unique = false)
    private Entry entry;

    @Column(name = "parameter_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ParameterType parameterType;

    @Column(name = "key", length = 255, nullable = false)
    private String key;

    @Column(name = "value", length = 4095, nullable = false)
    private String value;

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
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

    public static Map<String, String> getParameterTypeFormatOptionsMap() {
        Map<String, String> result = new LinkedHashMap<String, String>();

        result.put(ParameterType.NUMBER.toString(), "Number");
        result.put(ParameterType.BOOLEAN.toString(), "Boolean");
        result.put(ParameterType.TEXT.toString(), "Text");

        return result;
    }
}

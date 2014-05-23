package org.jbei.ice.lib.entry.model;

import javax.persistence.*;

import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dao.IDataTransferModel;

import org.hibernate.search.annotations.ContainedIn;

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
public class Parameter implements IDataModel {

    private static final long serialVersionUID = 1L;

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
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dto.Setting;

/**
 * Place to store installation specific values. For example, uuids for default site install groups.
 *
 * @author Hector Plahar, Timothy Ham
 */
@Entity
@Table(name = "configuration")
@SequenceGenerator(name = "sequence", sequenceName = "configuration_id_seq", allocationSize = 1)
public class Configuration implements IDataModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "key", length = 255, nullable = false, unique = true)
    private String key;

    @Column(name = "value", length = 1024, nullable = false)
    private String value;

    public Configuration() {
        super();
    }

    public Configuration(String key, String value) {
        setKey(key);
        setValue(value);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    @Override
    public Setting toDataTransferObject() {
        return new Setting(this.key, this.value);
    }
}

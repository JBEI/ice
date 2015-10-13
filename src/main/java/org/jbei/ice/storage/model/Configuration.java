package org.jbei.ice.storage.model;

import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

/**
 * Site wide configuration values. See {@link org.jbei.ice.lib.dto.ConfigurationKey} for
 * the built in configuration values
 *
 * @author Hector Plahar, Timothy Ham
 */
@Entity
@Table(name = "configuration")
@SequenceGenerator(name = "sequence", sequenceName = "configuration_id_seq", allocationSize = 1)
public class Configuration implements DataModel {

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

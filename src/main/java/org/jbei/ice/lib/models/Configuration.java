package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;

/**
 * Place to store site specific values. For example, uuids for default site install groups.
 * 
 * @author tham
 * 
 */
@Entity
@Table(name = "configuration")
@SequenceGenerator(name = "sequence", sequenceName = "configuration_id_seq", allocationSize = 1)
public class Configuration implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "key", length = 255, nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private ConfigurationKey key;

    @Column(name = "value", length = 1024, nullable = false)
    private String value;

    public enum ConfigurationKey {
        PLASMID_STORAGE_ROOT, STRAIN_STORAGE_ROOT, PART_STORAGE_ROOT, ARABIDOPSIS_STORAGE_ROOT, PLASMID_STORAGE_DEFAULT, STRAIN_STORAGE_DEFAULT, PART_STORAGE_DEFAULT, ARABIDOPSIS_STORAGE_DEFAULT
    }

    public Configuration() {
        super();
    }

    public Configuration(ConfigurationKey key, String value) {
        setKey(key);
        setValue(value);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ConfigurationKey getKey() {
        return key;
    }

    public void setKey(ConfigurationKey key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

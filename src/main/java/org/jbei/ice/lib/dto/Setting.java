package org.jbei.ice.lib.dto;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * POJO for site-wide Configuration setting
 *
 * @author Hector Plahar
 */
public class Setting implements IDataTransferModel {

    private String key;
    private String value;

    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
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
}

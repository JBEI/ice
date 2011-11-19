package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ParameterInfo implements IsSerializable {

    public enum Type implements IsSerializable {
        BOOLEAN, NUMBER, TEXT;
    }

    private String name;
    private String value;
    private Type type;

    public ParameterInfo() {
    }

    public ParameterInfo(String name, String value) { // TODO : type
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

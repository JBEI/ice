package org.jbei.ice.shared.dto;

public class ParameterInfo implements IDTOModel {

    private String name;
    private String value;
    private ParameterType parameterType;

    public ParameterInfo() {
    }

    public ParameterInfo(String name, String value) {
        this.name = name;
        this.value = value;
        this.parameterType = getValueType(value);
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

    public ParameterType getType() {
        return parameterType;
    }

    public void setType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public static ParameterType getValueType(String value) {
        value = value.toLowerCase().trim();
        if ("yes".equals(value) || "no".equals(value) || "true".equals(value)
                || "false".equals(value))
            return ParameterType.BOOLEAN;

        try {
            Integer.decode(value);
            return ParameterType.NUMBER;
        } catch (NumberFormatException nfe) {
            return ParameterType.TEXT;
        }
    }
}

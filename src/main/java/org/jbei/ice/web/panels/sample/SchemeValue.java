package org.jbei.ice.web.panels.sample;

import java.io.Serializable;

public class SchemeValue implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String index;

    public SchemeValue(String name, String value) {
        setName(name);
        setIndex(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String label) {
        name = label;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String value) {
        index = value;
    }

}
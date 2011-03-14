package org.jbei.ice.lib.vo;

import java.io.Serializable;

/**
 * This is called a Qualifier in genbank reference parlance.
 */
public class DNAFeatureNote implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name = "";
    private String value = "";
    private boolean quoted = true;

    public DNAFeatureNote() {
    }

    public DNAFeatureNote(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setQuoted(boolean quoted) {
        this.quoted = quoted;
    }

    public boolean isQuoted() {
        return quoted;
    }
}

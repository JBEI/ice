package org.jbei.ice.lib.vo;

import java.io.Serializable;

/**
 * Value object for storing sequence assembly item.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class AssemblyItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private FeaturedDNASequence sequence;
    private String original;
    private String meta;

    public AssemblyItem() {
        super();
    }

    public AssemblyItem(String name, FeaturedDNASequence sequence, String original, String meta) {
        super();

        this.name = name;
        this.sequence = sequence;
        this.original = original;
        this.meta = meta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FeaturedDNASequence getSequence() {
        return sequence;
    }

    public void setSequence(FeaturedDNASequence sequence) {
        this.sequence = sequence;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
}
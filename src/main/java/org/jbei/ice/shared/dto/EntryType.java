package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum EntryType implements IsSerializable {

    STRAIN("Strain", "strain"),
    PLASMID("Plasmid", "plasmid"),
    PART("Part", "part"),
    ARABIDOPSIS("Arabidopsis", "arabidopsis");

    private String name;
    private String display;

    EntryType() {
    }

    EntryType(String display, String name) {
        this.display = display;
        this.name = name;
    }

    public static EntryType nameToType(String name) {
        for (EntryType type : EntryType.values()) {
            if (name.equalsIgnoreCase(type.getName()))
                return type;
        }

        return null;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplay() {
        return this.display;
    }

    @Override
    public String toString() {
        return this.display;
    }
}
package org.jbei.ice.dto.entry;

public enum EntryType {

    STRAIN("Strain", "strain"),
    PLASMID("Plasmid", "plasmid"),
    PART("Part", "part"),
    SEED("Seed", "seed"),
    PROTEIN("Protein", "protein");

    private final String name;
    private final String display;

    EntryType(String display, String name) {
        this.display = display;
        this.name = name;
    }

    public static EntryType nameToType(String name) {
        if (name == null)
            return null;

        name = name.trim();

        if ("arabidopsis".equalsIgnoreCase(name)) // ice-150: workaround for existing records
            return SEED;

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

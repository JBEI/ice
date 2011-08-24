package org.jbei.ice.shared;

public enum EntryType {

    PLASMID("Plasmid"), STRAIN("Strain"), PART("Part"), STRAIN_WITH_PLASMID(
            "Strain with One Plasmid"), ARABIDOPSIS("Arabidopsis Seed");

    private String display;

    EntryType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return this.display;
    }
}

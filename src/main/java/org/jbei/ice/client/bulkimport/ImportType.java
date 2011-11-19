package org.jbei.ice.client.bulkimport;

public enum ImportType {

    STRAIN("Strain"), PLASMID("Plasmid"), STRAIN_WITH_PLASMID("Strain with Plasmid"), ARABIDOPSIS(
            "Arabidopsis Seed"), PART("Part");

    private String display;

    ImportType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return this.display;
    }
}

package org.jbei.ice.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum EntryAddType implements IsSerializable {

    PLASMID("Plasmid"), STRAIN("Strain"), PART("Part"), STRAIN_WITH_PLASMID(
            "Strain with One Plasmid"), ARABIDOPSIS("Arabidopsis Seed");

    private String display;

    EntryAddType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return this.display;
    }
}

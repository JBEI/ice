package org.jbei.ice.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum ColumnField implements IsSerializable {

    SELECTION("Select"), TYPE("Type"), PART_ID("Part ID"), CREATED("Created"), NAME("Name"), SUMMARY(
            "Summary"), OWNER("Owner"), STATUS("Status"), BIT_SCORE("Bit Score"), E_VALUE("E-Value"), ALIGNED_BP(
            "Aligned (BP)"), ALIGNED_IDENTITY("Aligned % Identity"), LAST_ADDED("Last Added"), LAST_VISITED(
            "Last Visited"), LABEL("Label"), NOTES("Notes"), LOCATION("Location"), DESCRIPTION(
            "Description"), COUNT("Member Count");

    private String name;

    ColumnField(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

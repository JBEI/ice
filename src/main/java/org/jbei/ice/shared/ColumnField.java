package org.jbei.ice.shared;

import java.io.Serializable;

public enum ColumnField implements Serializable {

    SELECTION("Select"), TYPE("Type"), PART_ID("Part ID"), CREATED("Created"), NAME("Name"), SUMMARY(
            "Summary"), OWNER("Owner"), STATUS("Status");

    private String name;

    ColumnField(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

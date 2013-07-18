package org.jbei.ice.lib.shared;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.IDTOModel;

public enum StatusType implements IDTOModel {

    COMPLETE("Complete"),
    IN_PROGRESS("In Progress"),
    PLANNED("Planned");

    private String displayName;

    private StatusType() {
    }

    StatusType(String name) {
        this.displayName = name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    public static String displayValueOf(String s) {
        for (StatusType type : StatusType.values()) {
            if (type.toString().equalsIgnoreCase(s))
                return type.toString();
        }
        return "";
    }

    public static StatusType displayToEnum(String displayName) {
        for (StatusType type : StatusType.values()) {
            if (type.toString().equalsIgnoreCase(displayName))
                return type;
        }

        return null;
    }

    public static ArrayList<String> getDisplayList() {
        ArrayList<String> displayList = new ArrayList<String>();
        for (StatusType type : StatusType.values()) {
            displayList.add(type.toString());
        }
        return displayList;
    }
}

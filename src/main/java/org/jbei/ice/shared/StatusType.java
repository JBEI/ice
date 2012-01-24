package org.jbei.ice.shared;

public enum StatusType {

    COMPLETE("Complete"), IN_PROGRESS("In Progress"), PLANNED("Planned");

    private String displayName;

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
}

package org.jbei.ice.client.profile;

public enum MenuType {
    ABOUT("About"), ENTRIES("Entries"), SAMPLES("Samples");

    private String display;

    MenuType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return this.display;
    }
}

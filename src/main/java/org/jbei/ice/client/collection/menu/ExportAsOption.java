package org.jbei.ice.client.collection.menu;

/**
 * Options for the "Export As" menu typically found at the top of
 * a set/collection of entries
 * 
 * @author Hector Plahar
 */
public enum ExportAsOption {

    PRINTABLE("Printable"), EXCEL("Excel"), XML("XML");

    private String display;

    ExportAsOption(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return this.display;
    }
}

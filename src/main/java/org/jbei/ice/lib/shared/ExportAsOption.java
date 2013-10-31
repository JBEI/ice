package org.jbei.ice.lib.shared;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Options for the "Export As" menu typically found at the top of
 * a set/collection of entries
 *
 * @author Hector Plahar
 */
public enum ExportAsOption implements IDTOModel {

    CSV("CSV"), XML("XML");

    private String display;

    ExportAsOption(String display) {
        this.display = display;
    }

    private ExportAsOption() {
    }

    @Override
    public String toString() {
        return this.display;
    }
}

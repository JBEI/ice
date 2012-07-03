package org.jbei.ice.client.bulkimport.sheet;

import org.jbei.ice.shared.BioSafetyOptions;

/**
 * @author Hector Plahar
 */
public class BioSafetySheetCell extends MultiSuggestSheetCell {

    public BioSafetySheetCell() {
        super();
        this.addOracleData(BioSafetyOptions.getDisplayList());
    }
}

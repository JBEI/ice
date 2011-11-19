package org.jbei.ice.client.bulkimport;

import org.jbei.ice.client.bulkimport.sheet.Sheet;
import org.jbei.ice.client.bulkimport.sheet.StrainSheet;

public class SheetFactory {

    public static Sheet getSheetForType(ImportType type) {

        switch (type) {
        case STRAIN:
            return new StrainSheet();

            //        case PLASMID:
            //            return new PlasmidSheet();

        default:
            return null;
        }
    }

}

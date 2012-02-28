package org.jbei.ice.client.bulkimport;

import org.jbei.ice.client.bulkimport.sheet.PlasmidSheet;
import org.jbei.ice.client.bulkimport.sheet.Sheet;
import org.jbei.ice.client.bulkimport.sheet.StrainSheet;
import org.jbei.ice.shared.EntryAddType;

public class SheetFactory {

    public static Sheet getSheetForType(EntryAddType type) {

        switch (type) {
        case STRAIN:
            return new StrainSheet();

        case PLASMID:
            return new PlasmidSheet();

        default:
            return null;
        }
    }

}

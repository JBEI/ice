package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.shared.EntryAddType;

public class ModelFactory {

    public static SheetModel getModelForType(EntryAddType type) {
        switch (type) {
        case STRAIN:
            return new StrainSheetModel();

        default:
            return null;
        }
    }
}

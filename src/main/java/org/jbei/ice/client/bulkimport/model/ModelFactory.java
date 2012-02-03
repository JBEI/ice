package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.client.bulkimport.ImportType;

public class ModelFactory {

    public static SheetModel getModelForType(ImportType type) {
        switch (type) {
        case STRAIN:
            return new StrainSheetModel();

        default:
            return null;
        }
    }

}

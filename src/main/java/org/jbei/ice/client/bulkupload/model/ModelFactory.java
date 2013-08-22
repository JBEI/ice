package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.entry.PartData;

/**
 * Factory for creating sheet models based on entry types
 *
 * @author Hector Plahar
 */
public class ModelFactory {

    public static SheetModel<? extends PartData> getModelForType(EntryAddType type) {
        switch (type) {
            case STRAIN:
                return new StrainSheetModel();

            case PLASMID:
                return new PlasmidSheetModel();

            case PART:
                return new PartSheetModel();

            case STRAIN_WITH_PLASMID:
                return new StrainWithPlasmidModel();

            case ARABIDOPSIS:
                return new ArabidopsisSheetModel();

            default:
                return null;
        }
    }
}

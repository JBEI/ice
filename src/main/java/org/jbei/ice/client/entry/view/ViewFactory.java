package org.jbei.ice.client.entry.view;

import org.jbei.ice.client.entry.view.detail.ArabidopsisDataView;
import org.jbei.ice.client.entry.view.detail.EntryDataView;
import org.jbei.ice.client.entry.view.detail.PartDataView;
import org.jbei.ice.client.entry.view.detail.PlasmidDataView;
import org.jbei.ice.client.entry.view.detail.StrainDataView;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;
import org.jbei.ice.lib.shared.dto.entry.StrainData;

public class ViewFactory {

    public static EntryDataView createDetailView(PartData info) {

        switch (info.getType()) {

            case PLASMID:
                PlasmidData plasmidData = (PlasmidData) info;
                return new PlasmidDataView(plasmidData);

            case PART:
                return new PartDataView(info);

            case ARABIDOPSIS:
                ArabidopsisSeedData seedData = (ArabidopsisSeedData) info;
                return new ArabidopsisDataView(seedData);

            case STRAIN:
                StrainData strainData = (StrainData) info;
                return new StrainDataView(strainData);

            default:
                return null;
            // TODO : proper handling of unknown types
        }
    }
}

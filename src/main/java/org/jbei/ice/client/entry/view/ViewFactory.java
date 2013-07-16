package org.jbei.ice.client.entry.view;

import org.jbei.ice.client.entry.view.detail.ArabidopsisInfoView;
import org.jbei.ice.client.entry.view.detail.EntryInfoView;
import org.jbei.ice.client.entry.view.detail.PartInfoView;
import org.jbei.ice.client.entry.view.detail.PlasmidInfoView;
import org.jbei.ice.client.entry.view.detail.StrainInfoView;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;
import org.jbei.ice.lib.shared.dto.entry.StrainData;

public class ViewFactory {

    public static EntryInfoView createDetailView(PartData info) {

        switch (info.getType()) {

            case PLASMID:
                PlasmidData plasmidData = (PlasmidData) info;
                return new PlasmidInfoView(plasmidData);

            case PART:
                return new PartInfoView(info);

            case ARABIDOPSIS:
                ArabidopsisSeedData seedData = (ArabidopsisSeedData) info;
                return new ArabidopsisInfoView(seedData);

            case STRAIN:
                StrainData strainData = (StrainData) info;
                return new StrainInfoView(strainData);

            default:
                return null;
            // TODO : proper handling of unknown types
        }
    }
}

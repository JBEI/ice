package org.jbei.ice.client.entry.view;

import org.jbei.ice.client.entry.view.detail.ArabidopsisInfoView;
import org.jbei.ice.client.entry.view.detail.EntryInfoView;
import org.jbei.ice.client.entry.view.detail.PartInfoView;
import org.jbei.ice.client.entry.view.detail.PlasmidInfoView;
import org.jbei.ice.client.entry.view.detail.StrainInfoView;
import org.jbei.ice.shared.dto.entry.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.PartInfo;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.shared.dto.entry.StrainInfo;

public class ViewFactory {

    public static EntryInfoView createDetailView(EntryInfo info) {

        switch (info.getType()) {

            case PLASMID:
                PlasmidInfo plasmidInfo = (PlasmidInfo) info;
                return new PlasmidInfoView(plasmidInfo);

            case PART:
                PartInfo partInfo = (PartInfo) info;
                return new PartInfoView(partInfo);

            case ARABIDOPSIS:
                ArabidopsisSeedInfo seedInfo = (ArabidopsisSeedInfo) info;
                return new ArabidopsisInfoView(seedInfo);

            case STRAIN:
                StrainInfo strainInfo = (StrainInfo) info;
                return new StrainInfoView(strainInfo);

            default:
                return null;
            // TODO : proper handling of unknown types
        }
    }
}

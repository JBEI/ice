package org.jbei.ice.client.entry.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.entry.view.detail.ArabidopsisDetailView;
import org.jbei.ice.client.entry.view.detail.EntryDetailView;
import org.jbei.ice.client.entry.view.detail.PartDetailView;
import org.jbei.ice.client.entry.view.detail.PlasmidDetailView;
import org.jbei.ice.client.entry.view.detail.StrainDetailView;
import org.jbei.ice.client.entry.view.update.ArabidopsisUpdateForm;
import org.jbei.ice.client.entry.view.update.UpdateEntryForm;
import org.jbei.ice.client.entry.view.update.UpdatePartForm;
import org.jbei.ice.client.entry.view.update.UpdatePlasmidForm;
import org.jbei.ice.client.entry.view.update.UpdateStrainForm;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PartInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.StrainInfo;

public class ViewFactory {

    public static EntryDetailView<? extends EntryInfo> createDetailView(EntryInfo info) {

        switch (info.getType()) {

        case PLASMID:
            PlasmidInfo plasmidInfo = (PlasmidInfo) info;
            return new PlasmidDetailView(plasmidInfo);

        case PART:
            PartInfo partInfo = (PartInfo) info;
            return new PartDetailView(partInfo);

        case ARABIDOPSIS:
            ArabidopsisSeedInfo seedInfo = (ArabidopsisSeedInfo) info;
            return new ArabidopsisDetailView(seedInfo);

        case STRAIN:
            StrainInfo strainInfo = (StrainInfo) info;
            return new StrainDetailView(strainInfo);

        default:
            return null;
            // TODO : proper handling of unknown types
        }
    }

    public static UpdateEntryForm<? extends EntryInfo> getUpdateForm(EntryInfo info,
            HashMap<AutoCompleteField, ArrayList<String>> autoCompleteData) {
        switch (info.getType()) {
        case PLASMID:
            PlasmidInfo plasmidInfo = (PlasmidInfo) info;
            return new UpdatePlasmidForm(autoCompleteData, plasmidInfo);

        case PART:
            PartInfo partInfo = (PartInfo) info;
            return new UpdatePartForm(autoCompleteData, partInfo);

        case ARABIDOPSIS:
            ArabidopsisSeedInfo seedInfo = (ArabidopsisSeedInfo) info;
            return new ArabidopsisUpdateForm(autoCompleteData, seedInfo);

        case STRAIN:
            StrainInfo strainInfo = (StrainInfo) info;
            return new UpdateStrainForm(autoCompleteData, strainInfo);

        default:
            return null;
        }
    }
}

package org.jbei.ice.client.collection.add;

import org.jbei.ice.client.collection.add.form.ArabidopsisForm;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.add.form.NewStrainWithPlasmidForm;
import org.jbei.ice.client.collection.add.form.PartForm;
import org.jbei.ice.client.collection.add.form.PlasmidForm;
import org.jbei.ice.client.collection.add.form.StrainForm;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.entry.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.EntryType;
import org.jbei.ice.shared.dto.entry.PartInfo;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.shared.dto.entry.StrainInfo;

public class EntryFormFactory {
    /**
     * creates a new form based on specific types of entries.
     * To create a new entry/form, add the type to {@link EntryType} and create a new form here
     *
     * @param type EntryType
     * @return form specific to type
     */
    public static IEntryFormSubmit entryForm(EntryAddType type, String creatorName, String creatorEmail) {
        switch (type) {
            case PLASMID:
                PlasmidInfo plasmidInfo = new PlasmidInfo();
                plasmidInfo.setCircular(true);
                plasmidInfo.setCreator(creatorName);
                plasmidInfo.setCreatorEmail(creatorEmail);
                return new PlasmidForm(plasmidInfo);

            case STRAIN:
                StrainInfo strainInfo = new StrainInfo();
                strainInfo.setCreator(creatorName);
                strainInfo.setCreatorEmail(creatorEmail);
                return new StrainForm(strainInfo);

            case PART:
                PartInfo partInfo = new PartInfo();
                partInfo.setCreator(creatorName);
                partInfo.setCreatorEmail(creatorEmail);
                return new PartForm(partInfo);

            case STRAIN_WITH_PLASMID:
                StrainInfo strain = new StrainInfo();
                strain.setCreator(creatorName);
                strain.setCreatorEmail(creatorEmail);
                strain.setInfo(new PlasmidInfo());
                return new NewStrainWithPlasmidForm(strain);

            case ARABIDOPSIS:
                ArabidopsisSeedInfo seedInfo = new ArabidopsisSeedInfo();
                seedInfo.setCreator(creatorName);
                seedInfo.setCreatorEmail(creatorEmail);
                return new ArabidopsisForm(seedInfo);

            default:
                return null;
        }
    }

    public static IEntryFormSubmit updateForm(EntryInfo info) {
        switch (info.getType()) {
            case PLASMID:
                PlasmidInfo plasmidInfo = (PlasmidInfo) info;
                return new PlasmidForm(plasmidInfo);

            case PART:
                PartInfo partInfo = (PartInfo) info;
                return new PartForm(partInfo);

            case ARABIDOPSIS:
                ArabidopsisSeedInfo seedInfo = (ArabidopsisSeedInfo) info;
                return new ArabidopsisForm(seedInfo);

            case STRAIN:
                StrainInfo strainInfo = (StrainInfo) info;
                return new StrainForm(strainInfo);

            default:
                return null;
        }
    }
}

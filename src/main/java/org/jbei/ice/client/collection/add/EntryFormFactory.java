package org.jbei.ice.client.collection.add;

import org.jbei.ice.client.collection.add.form.ArabidopsisForm;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.add.form.NewStrainWithPlasmidForm;
import org.jbei.ice.client.collection.add.form.PartForm;
import org.jbei.ice.client.collection.add.form.PlasmidForm;
import org.jbei.ice.client.collection.add.form.StrainForm;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;
import org.jbei.ice.lib.shared.dto.entry.StrainData;

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
                PlasmidData plasmidData = new PlasmidData();
                plasmidData.setCircular(true);
                plasmidData.setCreator(creatorName);
                plasmidData.setCreatorEmail(creatorEmail);
                return new PlasmidForm(plasmidData);

            case STRAIN:
                StrainData strainData = new StrainData();
                strainData.setCreator(creatorName);
                strainData.setCreatorEmail(creatorEmail);
                return new StrainForm(strainData);

            case PART:
                PartData partInfo = new PartData();
                partInfo.setCreator(creatorName);
                partInfo.setCreatorEmail(creatorEmail);
                return new PartForm(partInfo);

            case STRAIN_WITH_PLASMID:
                StrainData strain = new StrainData();
                strain.setCreator(creatorName);
                strain.setCreatorEmail(creatorEmail);
                strain.setInfo(new PlasmidData());
                return new NewStrainWithPlasmidForm(strain);

            case ARABIDOPSIS:
                ArabidopsisSeedData seedData = new ArabidopsisSeedData();
                seedData.setCreator(creatorName);
                seedData.setCreatorEmail(creatorEmail);
                return new ArabidopsisForm(seedData);

            default:
                return null;
        }
    }

    public static IEntryFormSubmit updateForm(PartData info) {
        switch (info.getType()) {
            case PLASMID:
                PlasmidData plasmidData = (PlasmidData) info;
                return new PlasmidForm(plasmidData);

            case PART:
                return new PartForm(info);

            case ARABIDOPSIS:
                ArabidopsisSeedData seedData = (ArabidopsisSeedData) info;
                return new ArabidopsisForm(seedData);

            case STRAIN:
                StrainData strainData = (StrainData) info;
                return new StrainForm(strainData);

            default:
                return null;
        }
    }
}

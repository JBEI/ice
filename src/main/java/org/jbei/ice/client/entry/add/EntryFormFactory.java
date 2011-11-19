package org.jbei.ice.client.entry.add;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.entry.add.form.NewArabidopsisForm;
import org.jbei.ice.client.entry.add.form.NewEntryForm;
import org.jbei.ice.client.entry.add.form.NewPartForm;
import org.jbei.ice.client.entry.add.form.NewPlasmidForm;
import org.jbei.ice.client.entry.add.form.NewStrainForm;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.EntryInfo.EntryType;

public class EntryFormFactory {
    /**
     * creates a new form based on specific types of entries.
     * To create a new entry/form, add the type to {@link EntryType} and create a new form here
     * 
     * @param type
     *            EntryType
     * @return form specific to type
     */
    public static NewEntryForm entryForm(EntryAddType type,
            HashMap<AutoCompleteField, ArrayList<String>> autoCompleteData, String creatorName,
            String creatorEmail) {

        switch (type) {

        case PLASMID:
            return new NewPlasmidForm(autoCompleteData, creatorName, creatorEmail);

        case STRAIN:
            return new NewStrainForm(autoCompleteData, creatorName, creatorEmail);

        case PART:
            return new NewPartForm(autoCompleteData, creatorName, creatorEmail);

            //        case STRAIN_WITH_PLASMID:
            //            return new NewStrainWithPlasmidForm(autoCompleteData, saveButton);

        case ARABIDOPSIS:
            return new NewArabidopsisForm(autoCompleteData, creatorName, creatorEmail);

        default:
            //            layout.setHTML(1, 0, "Part not recognized");
            return null;
        }
    }
}

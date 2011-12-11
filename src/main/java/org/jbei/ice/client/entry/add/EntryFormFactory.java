package org.jbei.ice.client.entry.add;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.entry.add.form.EntryCreateWidget;
import org.jbei.ice.client.entry.add.form.NewArabidopsisForm;
import org.jbei.ice.client.entry.add.form.NewPartForm;
import org.jbei.ice.client.entry.add.form.NewPlasmidForm;
import org.jbei.ice.client.entry.add.form.NewStrainForm;
import org.jbei.ice.client.entry.add.form.NewStrainWithPlasmidForm;
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
    public static EntryCreateWidget entryForm(EntryAddType type,
            HashMap<AutoCompleteField, ArrayList<String>> autoCompleteData, String creatorName,
            String creatorEmail) {

        EntryCreateWidget widget;

        switch (type) {

        case PLASMID:
            NewPlasmidForm plasmidForm = new NewPlasmidForm(autoCompleteData, creatorName,
                    creatorEmail);
            widget = new EntryCreateWidget(plasmidForm);
            return widget;

        case STRAIN:
            widget = new EntryCreateWidget(new NewStrainForm(autoCompleteData, creatorName,
                    creatorEmail));
            return widget;

        case PART:
            widget = new EntryCreateWidget(new NewPartForm(autoCompleteData, creatorName,
                    creatorEmail));
            return widget;

        case STRAIN_WITH_PLASMID:
            widget = new EntryCreateWidget(new NewStrainWithPlasmidForm(autoCompleteData,
                    creatorName, creatorEmail));
            return widget;

        case ARABIDOPSIS:
            widget = new EntryCreateWidget(new NewArabidopsisForm(autoCompleteData, creatorName,
                    creatorEmail));
            return widget;

        default:
            return null;
        }
    }
}

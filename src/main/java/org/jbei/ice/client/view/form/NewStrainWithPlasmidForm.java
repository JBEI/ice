package org.jbei.ice.client.view.form;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.ui.Button;

public class NewStrainWithPlasmidForm extends NewEntryForm {

    public NewStrainWithPlasmidForm(HashMap<AutoCompleteField, ArrayList<String>> data, Button saveButton) {

        super(data);
        initWidget(layout);
    }

    protected void init(Button saveButton) {

    }

    @Override
    public EntryInfo getEntry() {
        // TODO Auto-generated method stub
        return null;
    }
}

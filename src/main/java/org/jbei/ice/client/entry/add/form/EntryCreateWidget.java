package org.jbei.ice.client.entry.add.form;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class EntryCreateWidget implements IsWidget {

    private final IEntryFormSubmit submitForm;

    public EntryCreateWidget(IEntryFormSubmit submitForm) {
        this.submitForm = submitForm;
    }

    public IEntryFormSubmit getEntrySubmitForm() {
        return this.submitForm;
    }

    @Override
    public Widget asWidget() {
        return submitForm.asWidget();
    }
}

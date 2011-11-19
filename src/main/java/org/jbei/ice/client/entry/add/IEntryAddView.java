package org.jbei.ice.client.entry.add;

import org.jbei.ice.client.entry.add.form.NewEntryForm;
import org.jbei.ice.client.entry.add.menu.NewEntryMenu;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.ui.Widget;

public interface IEntryAddView {

    NewEntryMenu getMenu();

    NewEntryForm<EntryInfo> getCurrentForm();

    Widget asWidget();

    void setCurrentForm(NewEntryForm<EntryInfo> form, String title);
}

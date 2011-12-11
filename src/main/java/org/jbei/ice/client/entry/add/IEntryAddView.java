package org.jbei.ice.client.entry.add;

import org.jbei.ice.client.entry.add.form.EntryCreateWidget;
import org.jbei.ice.client.entry.add.menu.NewEntryMenu;

import com.google.gwt.user.client.ui.Widget;

public interface IEntryAddView {

    NewEntryMenu getMenu();

    EntryCreateWidget getCurrentForm();

    Widget asWidget();

    void setCurrentForm(EntryCreateWidget form, String title);
}

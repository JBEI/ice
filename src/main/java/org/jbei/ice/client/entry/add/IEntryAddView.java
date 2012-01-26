package org.jbei.ice.client.entry.add;

import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.client.entry.add.form.EntryCreateWidget;
import org.jbei.ice.client.entry.add.menu.EntryAddMenu;

import com.google.gwt.user.client.ui.Widget;

public interface IEntryAddView {

    EntryAddMenu getMenu();

    EntryCreateWidget getCurrentForm();

    Widget asWidget();

    void setCurrentForm(EntryCreateWidget form, String title);

    void setFeedbackPanel(FeedbackPanel panel);
}

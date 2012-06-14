package org.jbei.ice.client.entry.view.update;

import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for entry form update submissions
 * 
 * @author Hector Plahar
 */

public interface IEntryFormUpdateSubmit {

    void addSubmitHandler(ClickHandler handler);

    void addCancelHandler(ClickHandler handler);

    FocusWidget validateForm();

    Widget asWidget();

    void populateEntry();

    EntryInfo getEntry();
}

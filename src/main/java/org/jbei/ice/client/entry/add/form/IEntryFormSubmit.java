package org.jbei.ice.client.entry.add.form;

import java.util.Set;

import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for entry form submissions
 * TODO : nothing here suggests that it is for entry
 * 
 * @author Hector Plahar
 */

public interface IEntryFormSubmit {

    Button getSubmit();

    Button getCancel();

    FocusWidget validateForm();

    Widget asWidget();

    void populateEntries();

    Set<EntryInfo> getEntries();

    void setSampleLocation(SampleLocationWidget sampleLocation);
}

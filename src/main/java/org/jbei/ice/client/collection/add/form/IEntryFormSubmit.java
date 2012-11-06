package org.jbei.ice.client.collection.add.form;

import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for entry form submissions
 *
 * @author Hector Plahar
 */

public interface IEntryFormSubmit {

    Button getSubmit();

    Button getCancel();

    FocusWidget validateForm();

    Widget asWidget();

    void populateEntries();

    EntryInfo getEntry();

    void setSampleLocation(SampleLocation sampleLocation);
}

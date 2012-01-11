package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.detail.EntryDetailView;
import org.jbei.ice.client.entry.view.table.EntrySampleTable;
import org.jbei.ice.client.entry.view.table.SequenceTable;
import org.jbei.ice.client.entry.view.update.UpdateEntryForm;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public interface IEntryView {
    Widget asWidget();

    void setEntryName(String name);

    CellList<AttachmentInfo> getAttachmentList();

    CellList<MenuItem> getMenu();

    Button showSequenceView(SequenceTable table, Flash flash);

    Button showEntryDetailView(EntryDetailView<? extends EntryInfo> view);

    void showUpdateForm(UpdateEntryForm<? extends EntryInfo> form);

    void showPermissionsWidget(Widget permissionWidget);

    Button showSampleView(EntrySampleTable table);

    Label getPermissionLink();
}

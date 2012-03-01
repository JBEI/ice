package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;

import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.detail.EntryDetailView;
import org.jbei.ice.client.entry.view.table.EntrySampleTable;
import org.jbei.ice.client.entry.view.table.SequenceTable;
import org.jbei.ice.client.entry.view.update.UpdateEntryForm;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public interface IEntryView {
    Widget asWidget();

    void setEntryName(String name);

    CellList<AttachmentInfo> getAttachmentList();

    void setMenuItems(ArrayList<MenuItem> items);

    Button showSequenceView(SequenceTable table, Flash flash);

    Button showEntryDetailView(EntryDetailView<? extends EntryInfo> view);

    void showUpdateForm(UpdateEntryForm<? extends EntryInfo> form);

    PermissionsWidget getPermissionsWidget();

    void showPermissionsWidget();

    Button showSampleView(EntrySampleTable table);

    EntryDetailViewMenu getDetailMenu();

    CreateSampleForm getSampleForm();

    void showContextNav(boolean show);

    void addNextHandler(ClickHandler handler);

    void addGoBackHandler(ClickHandler handler);

    void addPrevHandler(ClickHandler handler);

    void enablePrev(boolean enable);

    void enableNext(boolean enable);
}

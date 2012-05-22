package org.jbei.ice.client.profile;

import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

public interface IProfileView {

    Widget asWidget();

    void setContents(Widget widget);

    ProfileViewMenu getMenu();

    void setHeaderText(String text, ClickHandler editHandler, ClickHandler changePasswordHandler);

    HasEntryDataTable<SampleInfo> getSamplesTable();

    void setSampleView();
}

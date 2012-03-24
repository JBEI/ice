package org.jbei.ice.client.profile;

import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.user.client.ui.Widget;

public interface IProfileView {

    Widget asWidget();

    void setContents(Widget widget);

    ProfileViewMenu getMenu();

    void setHeaderText(String text);

    HasEntryDataTable<SampleInfo> getSamplesTable();
}

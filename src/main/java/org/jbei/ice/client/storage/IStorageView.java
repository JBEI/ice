package org.jbei.ice.client.storage;

import com.google.gwt.user.client.ui.Widget;

public interface IStorageView {

    void setContent(Widget widget);

    Widget asWidget();
}

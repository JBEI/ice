package org.jbei.ice.client.profile;

import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public interface IProfileView {

    Widget asWidget();

    SingleSelectionModel<UserOption> getUserSelectionModel();

    void setMenuSelection(UserOption option);

    void setMenuOptions(UserOption... menuOptions);

    void setAccountInfo(User info);

    void show(UserOption selected, Widget widget);
}

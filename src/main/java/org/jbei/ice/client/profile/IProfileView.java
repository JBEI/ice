package org.jbei.ice.client.profile;

import org.jbei.ice.lib.shared.dto.AccountInfo;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public interface IProfileView {

    Widget asWidget();

    SingleSelectionModel<UserOption> getUserSelectionModel();

    void setMenuSelection(UserOption option);

    void setMenuOptions(UserOption... menuOptions);

    void setAccountInfo(AccountInfo info);

    void show(UserOption selected, Widget widget);
}

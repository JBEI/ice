package org.jbei.ice.client.entry.view;

import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;
import org.jbei.ice.shared.dto.permission.PermissionSuggestion;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public abstract class ReadBoxSelectionHandler implements SelectionHandler<SuggestOracle.Suggestion> {

    @Override
    public void onSelection(SelectionEvent<Suggestion> event) {
        PermissionSuggestion suggest = (PermissionSuggestion) event.getSelectedItem();
        if (suggest == null)
            return;

        PermissionInfo info = new PermissionInfo(suggest.getType(), suggest.getId(),
                suggest.getReplacementString());
        this.updatePermission(info, suggest.getType());
    }

    abstract void updatePermission(PermissionInfo info, PermissionType permissionType);
}

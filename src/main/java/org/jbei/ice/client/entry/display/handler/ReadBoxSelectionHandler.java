package org.jbei.ice.client.entry.display.handler;

import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
import org.jbei.ice.lib.shared.dto.permission.PermissionSuggestion;

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

        this.updatePermission(suggest.getAccess());
    }

    public abstract void updatePermission(AccessPermission access);
}

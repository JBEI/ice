package org.jbei.ice.client.entry.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class PermissionsPresenter {

    public interface IPermissionsView {

        void setAccountData(LinkedHashMap<Long, String> result);

        void setGroupData(LinkedHashMap<Long, String> result);

        HandlerRegistration addUpdatePermissionsHandler(ClickHandler handler);

        void setExistingPermissions(ArrayList<PermissionInfo> result);

        HashMap<PermissionType, HashSet<String>> getReadSelected();

        HashMap<PermissionType, HashSet<String>> getWriteSelected();
    }

    private final IPermissionsView view;
    private HashMap<Long, String> accountInfo;
    private HashMap<Long, String> groupInfo;
    private HandlerRegistration updateHandlerRegistration;

    public PermissionsPresenter(IPermissionsView view) {
        this.view = view;
        this.accountInfo = new HashMap<Long, String>();
        this.groupInfo = new HashMap<Long, String>();
    }

    public void setAccountData(LinkedHashMap<Long, String> result) {
        view.setAccountData(result);
        accountInfo.clear();
        accountInfo.putAll(result);
    }

    public HashMap<PermissionType, HashSet<String>> getReadSelected() {
        return view.getReadSelected();
    }

    public HashMap<PermissionType, HashSet<String>> getWriteSelected() {
        return view.getWriteSelected();
    }

    public void setGroupData(LinkedHashMap<Long, String> result) {
        view.setGroupData(result);
        groupInfo.clear();
        groupInfo.putAll(result);
    }

    public void addUpdatePermissionsHandler(ClickHandler clickHandler) {
        if (updateHandlerRegistration != null) {
            updateHandlerRegistration.removeHandler();
        }
        updateHandlerRegistration = view.addUpdatePermissionsHandler(clickHandler);
    }

    public void setPermissionData(ArrayList<PermissionInfo> result) {
        view.setExistingPermissions(result);
    }
}

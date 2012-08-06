package org.jbei.ice.client.bulkupload;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.NewBulkInput;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public interface IBulkUploadView {

    void setHeader(String header);

    Widget asWidget();

    void showFeedback(String msg, boolean isError);

    void setSavedDraftsData(ArrayList<BulkUploadMenuItem> data, IDeleteMenuHandler handler);

    void setPendingDraftsData(ArrayList<BulkUploadMenuItem> data, IDeleteMenuHandler handler);

    SingleSelectionModel<BulkUploadMenuItem> getDraftMenuModel();

    SingleSelectionModel<EntryAddType> getImportCreateModel();

    void setDraftMenuVisibility(boolean visible, boolean isToggleClick);

    void addToggleMenuHandler(ClickHandler handler);

    boolean getMenuVisibility();

    void setToggleMenuVisibility(boolean visible);

    void setSheet(NewBulkInput input, boolean isNew, boolean isValidation);

    void setDraftUpdateHandler(ClickHandler handler);

    String getDraftName();

    void setSubmitHandler(ClickHandler submitHandler);

    void setResetHandler(ClickHandler resetHandler);

    void setDraftSaveHandler(ClickHandler draftSaveHandler);

    void updateSavedDraftsMenu(BulkUploadMenuItem item);

    SingleSelectionModel<BulkUploadMenuItem> getPendingMenuModel();

    void setApproveHandler(ClickHandler handler);

    void setGroupPermissions(ArrayList<GroupInfo> result);

    void setSelectedGroupPermission(GroupInfo groupInfo);

    String getPermissionSelection();
}

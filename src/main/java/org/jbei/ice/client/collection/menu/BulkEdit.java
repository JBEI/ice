package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget that enables users to bulk edit a set of
 * parts of the same type
 *
 * @author Hector Plahar
 */
public class BulkEdit implements IsWidget {

    interface Style extends CellList.Style {

        String subMenuBulkEdit();
    }

    interface BulkEditResource extends CellList.Resources {

        static BulkEditResource INSTANCE = GWT.create(BulkEditResource.class);

        @Source("org/jbei/ice/client/resource/css/BulkEdit.css")
        Style cellListStyle();
    }

    private static final String LABEL = "<i class=\"" + FAIconType.TABLE.getStyleName()
            + "\" style=\"opacity:0.85; color: #0082C0\"></i> Bulk Edit";

    private final Button bulkEdit;
    private HandlerRegistration handlerRegistration;

    public BulkEdit() {
        BulkEditResource.INSTANCE.cellListStyle().ensureInjected();
        bulkEdit = new Button(LABEL);
        bulkEdit.setStyleName(BulkEditResource.INSTANCE.cellListStyle().subMenuBulkEdit());
        bulkEdit.setEnabled(false);
    }

    public void setEnabled(boolean enable) {
        this.bulkEdit.setEnabled(enable);
    }

    @Override
    public Widget asWidget() {
        return bulkEdit;
    }

    public void setClickHandler(ClickHandler handler) {
        if (handlerRegistration != null)
            handlerRegistration.removeHandler();
        handlerRegistration = bulkEdit.addClickHandler(handler);
    }
}

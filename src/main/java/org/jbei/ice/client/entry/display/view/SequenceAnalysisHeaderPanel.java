package org.jbei.ice.client.entry.display.view;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * Header for the Entry Sequence table on the Entry detail view page
 *
 * @author Hector Plahar
 */
public class SequenceAnalysisHeaderPanel extends Composite {

    private final MultiSelectionModel<SequenceAnalysisInfo> selection;
    private final Button delete;
    private final Button upload;
    private HandlerRegistration registration;
    private HandlerRegistration uploadRegistration;

    public SequenceAnalysisHeaderPanel(MultiSelectionModel<SequenceAnalysisInfo> selection) {
        HTMLPanel panel = new HTMLPanel("<span id=\"trace_file_delete\"></span> "
                                                + "&nbsp; <span id=\"trace_file_upload\"></span>");
        initWidget(panel);

        this.selection = selection;
        addSelectionHandler();

        // delete button
        delete = new Button("<i class=\"" + FAIconType.TRASH.getStyleName() + "\"></i> Delete Selected");
        delete.setEnabled(false);
        delete.setVisible(false);
        panel.add(delete, "trace_file_delete");

        // upload button
        upload = new Button("<i class=\"" + FAIconType.UPLOAD_ALT.getStyleName() + "\"></i> Upload File(s)");
        panel.add(upload, "trace_file_upload");
        upload.setVisible(false);
        this.setStyleName("pad_top");
    }

    private void addSelectionHandler() {
        this.selection.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                int count = selection.getSelectedSet().size();
                delete.setEnabled(count > 0);
            }
        });
    }

    public void setTraceUploadHandler(final ClickHandler handler) {
        if (uploadRegistration != null)
            uploadRegistration.removeHandler();
        uploadRegistration = upload.addClickHandler(handler);
    }

    public void setUserCanEdit(boolean canEdit) {
        delete.setVisible(canEdit);
        upload.setVisible(canEdit);
    }

    /**
     * Sets the trace sequence delete handler. This is expected to be called if the user is allowed to edit
     * the part the trace sequences are associated with.
     *
     * @param handler handler for deleting trace sequences
     */
    public void setDeleteHandler(final ClickHandler handler) {
        if (registration != null)
            registration.removeHandler();

        registration = delete.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm("Delete selected trace files? This action cannot be undone")) {
                    handler.onClick(event);
                    selection.clear();
                    delete.setEnabled(false);
                }
            }
        });
    }
}

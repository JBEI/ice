package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * Header for the Entry Sequence table on the Entry detail view page
 *
 * @author Hector Plahar
 */

public class SequenceAnalysisHeaderPanel extends Composite {

    private final HTMLPanel panel;
    private final Label traceCount;
    private final MultiSelectionModel<SequenceAnalysisInfo> selection;
    private final Button delete;
    private HandlerRegistration registration;

    public SequenceAnalysisHeaderPanel(MultiSelectionModel<SequenceAnalysisInfo> selection) {

        panel = new HTMLPanel(
                "<span id=\"selection_trace_file_count\"></span><span id=\"trace_file_delete\"></span>");
        initWidget(panel);

        traceCount = new Label("0 selected");
        traceCount.setStyleName("open_sequence_sub_link");
        traceCount.addStyleName("display-inline");
        panel.add(traceCount, "selection_trace_file_count");
        this.selection = selection;
        addSelectionHandler();

        // delete button
        delete = new Button("Delete");
        delete.setEnabled(false);
        delete.setVisible(false);
        panel.add(delete, "trace_file_delete");
    }

    private void addSelectionHandler() {
        this.selection.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                int count = selection.getSelectedSet().size();
                delete.setEnabled(count > 0);
                delete.setVisible(registration != null);
                traceCount.setText(count + " selected");
            }
        });
    }

    public void setDeleteHandler(final ClickHandler handler) {
        if (registration != null)
            registration.removeHandler();

        registration = delete.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm("Delete selected trace files? This action cannot be undone")) {
                    handler.onClick(event);
                    selection.clear();
                }
            }
        });
        delete.setVisible(true);
    }
}

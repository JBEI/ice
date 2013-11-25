package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.entry.display.handler.DeleteSequenceHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DeleteSequenceData {

    private final SequenceViewPanelPresenter sequencePanelPresenter;
    private final Label label;

    public DeleteSequenceData(SequenceViewPanelPresenter presenter, DeleteSequenceHandler deleteHandler) {
        this.sequencePanelPresenter = presenter;
        label = new Label("Delete");
        label.setStyleName("open_sequence_sub_link");
        label.addClickHandler(new DeleteHandler(deleteHandler));
    }

    public Widget getLabelWidget() {
        HTMLPanel panel = new HTMLPanel("<span id=\"delete_sequence_data_label\"></span>"
                                                + "<span style=\"color: #262626; font-size: 0.75em;\">| </span>");
        panel.setStyleName("display-inline");
        panel.add(label, "delete_sequence_data_label");
        return panel;
    }

    private class DeleteHandler implements ClickHandler {

        private final DeleteSequenceHandler deleteHandler;

        public DeleteHandler(DeleteSequenceHandler deleteHandler) {
            this.deleteHandler = deleteHandler;
            this.deleteHandler.setCallback(new DeleteCallback());
        }

        @Override
        public void onClick(ClickEvent event) {
            if (Window.confirm("Confirm sequence deletion.\nPlease note that this action cannot be undone.")) {
                deleteHandler.onClick(event);
            }
        }
    }

    private class DeleteCallback extends Callback<Boolean> {

        @Override
        public void onSuccess(Boolean success) {
            if (success) {
                sequencePanelPresenter.getPartData().setHasSequence(false);
                sequencePanelPresenter.getPartData().setHasOriginalSequence(false);
                sequencePanelPresenter.updateSequenceView();
            } else {
                Window.alert("There was a problem deleting the sequence");
            }
        }

        @Override
        public void onFailure() {
            Window.alert("There was a problem deleting the sequence");
        }
    }
}

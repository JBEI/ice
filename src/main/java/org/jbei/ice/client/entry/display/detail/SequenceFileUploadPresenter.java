package org.jbei.ice.client.entry.display.detail;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class SequenceFileUploadPresenter {

    public interface IView {

        SingleSelectionModel<UploadOption> getUploadOptionSelectionModel();

        void showPasteSequenceDialog();

        void showUploadFileDialog();

        void addSubmitSequencePasteHandler(ClickHandler handler);

        String getPastedSequence();
    }

    private final IView view;

    public SequenceFileUploadPresenter(final IView view) {
        this.view = view;
        final SingleSelectionModel<UploadOption> uploadModel = view.getUploadOptionSelectionModel();
        UploadSelectionHandler handler = new UploadSelectionHandler(uploadModel);
        uploadModel.addSelectionChangeHandler(handler);
    }

    // selection change handler for upload options
    private class UploadSelectionHandler implements Handler {

        private final SingleSelectionModel<UploadOption> model;

        public UploadSelectionHandler(SingleSelectionModel<UploadOption> model) {
            this.model = model;
        }

        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
            UploadOption selected = model.getSelectedObject();
            if (selected == null)
                return;

            switch (selected) {
                case FILE:
                    view.showUploadFileDialog();
                    break;

                case PASTE:
                default:
                    view.showPasteSequenceDialog();
                    break;
            }

            model.setSelected(selected, false);
        }
    }
}

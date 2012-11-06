package org.jbei.ice.client.entry.view.detail;

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

    public String getPastedSequence() {
        return this.view.getPastedSequence();
    }

    public void addSubmitSequenceHandler(ClickHandler handler) {
        view.addSubmitSequencePasteHandler(handler);
    }

    // upload options for sequence files
    public enum UploadOption {
        FILE("File Upload", "file"), PASTE("Paste Sequence", "paste");

        private String display;
        private String type;

        private UploadOption(String display, String type) {
            this.display = display;
            this.type = type;
        }

        @Override
        public String toString() {
            return this.display;
        }

        public String getType() {
            return this.type;
        }
    }
}

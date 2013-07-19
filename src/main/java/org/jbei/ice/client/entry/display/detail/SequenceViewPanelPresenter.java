package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.client.entry.display.view.DeleteSequenceHandler;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.event.dom.client.ClickHandler;
import gwtupload.client.IUploader.OnFinishUploaderHandler;

public class SequenceViewPanelPresenter {

    public interface ISequenceView {
        void addSubmitSequencePasteHandler(ClickHandler handler);

        void updateSequenceContents();

        void updateSequenceHeaders();

        String getSequence();

        PartData getInfo();

        void setHasSequence(boolean hasSequence);

        void hideDialog();

        void showSequenceDeleteLink(DeleteSequenceHandler deleteHandler);

        void setFinishHandler(OnFinishUploaderHandler handler);
    }

    private final ISequenceView view;
    private boolean canEdit;

    public SequenceViewPanelPresenter(ISequenceView view) {
        this.view = view;
    }

    public void addSequencePasteHandler(ClickHandler handler) {
        this.view.addSubmitSequencePasteHandler(handler);
    }

    public void addSequenceFileUploadHandler(OnFinishUploaderHandler handler) {
        this.view.setFinishHandler(handler);
    }

    public void updateSequenceView() {
        this.view.hideDialog();
        this.view.updateSequenceHeaders();
        this.view.updateSequenceContents();
    }

    public String getSequence() {
        return view.getSequence();
    }

    public PartData getEntry() {
        return view.getInfo();
    }

    public void setHasSequence(boolean result) {
        view.setHasSequence(result);
    }

    public void setIsCanEdit(boolean canEdit, DeleteSequenceHandler deleteHandler) {
        this.canEdit = canEdit;
        if (canEdit && view.getInfo().isHasSequence())
            view.showSequenceDeleteLink(deleteHandler);
    }

    public boolean isCanEdit() {
        return this.canEdit;
    }
}

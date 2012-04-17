package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickHandler;

public class SequenceViewPanelPresenter {

    public interface ISequenceView {
        void addSubmitSequencePasteHandler(ClickHandler handler);

        void updateSequenceContents();

        void updateSequenceHeaders();

        String getSequence();

        EntryInfo getInfo();

        void setHasSequence(boolean hasSequence);

        void hideDialog();
    }

    private final ISequenceView view;

    public SequenceViewPanelPresenter(ISequenceView view) {
        this.view = view;
    }

    public void addFileUploadHandler(ClickHandler handler) {
        this.view.addSubmitSequencePasteHandler(handler);
    }

    public void updateSequenceView() {
        this.view.hideDialog();
        this.view.updateSequenceHeaders();
        this.view.updateSequenceContents();
    }

    public String getSequence() {
        return view.getSequence();
    }

    public EntryInfo getEntry() {
        return view.getInfo();
    }

    public void setHasSequence(boolean result) {
        view.setHasSequence(result);
    }
}

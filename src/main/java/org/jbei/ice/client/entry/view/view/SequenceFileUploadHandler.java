package org.jbei.ice.client.entry.view.view;

import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;

import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;

public class SequenceFileUploadHandler implements OnFinishUploaderHandler {

    private final SequenceViewPanelPresenter presenter;

    public SequenceFileUploadHandler(SequenceViewPanelPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onFinish(IUploader uploader) {
        presenter.getEntry().setHasSequence(true);
        presenter.updateSequenceView();
    }
}

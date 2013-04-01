package org.jbei.ice.client.bulkupload.sheet;

import gwtupload.client.BaseUploadStatus;

public class FileUploadStatus extends BaseUploadStatus {

    @Override
    protected void addElementsToPanel() {
        panel.add(statusLabel);
        panel.add(cancelLabel);
    }

    @Override
    public void setFileName(String name) {
        if (name.length() > 25) {
            name.lastIndexOf('.');
            name = name.substring(0, 22) + "...";
        }
        fileNameLabel.setText(name);
    }
}

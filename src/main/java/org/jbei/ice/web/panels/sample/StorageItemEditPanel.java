package org.jbei.ice.web.panels.sample;

import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.StorageScheme;

public class StorageItemEditPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private StorageScheme scheme = null;
    private String[] values;
    private StorageItemSchemeEditPanel storageSchemeEditPanel = null;

    public StorageItemEditPanel(String id) {
        super(id);
    }

    public void setScheme(StorageScheme scheme) {
        this.scheme = scheme;
    }

    public StorageScheme getScheme() {
        return scheme;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public String[] getValues() {
        return values;
    }

    public void setStorageSchemeEditPanel(StorageItemSchemeEditPanel storageSchemeEditPanel) {
        this.storageSchemeEditPanel = storageSchemeEditPanel;
    }

    public StorageItemSchemeEditPanel getStorageSchemeEditPanel() {
        return storageSchemeEditPanel;
    }

}

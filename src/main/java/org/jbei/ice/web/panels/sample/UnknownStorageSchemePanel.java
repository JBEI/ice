package org.jbei.ice.web.panels.sample;

import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.web.panels.EmptyMessagePanel;

public class UnknownStorageSchemePanel extends Panel {
    private static final long serialVersionUID = 1L;

    public UnknownStorageSchemePanel(String id, Storage storage) {
        super(id);

        add(new EmptyMessagePanel("messagePanel", "Mismatched location found:"));
        add(new StorageLineViewPanel("storageLineViewPanel", storage));
        add(new EmptyMessagePanel("messagePanel2", "Enter new location or leave empty to reset."));

    }
}

package org.jbei.ice.client.collection.presenter;

import org.jbei.ice.lib.shared.dto.folder.FolderDetails;

import com.google.gwt.view.client.ProvidesKey;

public class FolderDetailsKeyProvider implements ProvidesKey<FolderDetails> {

    @Override
    public Long getKey(FolderDetails item) {
        return item.getId();
    }
}

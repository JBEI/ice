package org.jbei.ice.lib.dto.folder;

import java.util.ArrayList;
import java.util.Collections;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class FolderWrapper implements IDataTransferModel {
    private ArrayList<FolderDetails> folders;

    public FolderWrapper() {
        folders = new ArrayList<>();
    }

    public ArrayList<FolderDetails> getFolders() {
        return this.folders;
    }

    public void sort() {
        Collections.sort(folders);
    }
}

package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Folder;

import java.util.ArrayList;
import java.util.List;

/**
 * Folders that an entry is contained in
 *
 * @author Hector Plahar
 */
public class EntryFolders extends HasEntry {

    private final String userId;
    private final Entry entry;
    private final FolderAuthorization folderAuthorization;

    public EntryFolders(String userId, String entryId) {
        this.userId = userId;
        this.entry = getEntry(entryId);
        EntryAuthorization entryAuthorization = new EntryAuthorization();
        entryAuthorization.expectRead(this.userId, this.entry);
        this.folderAuthorization = new FolderAuthorization();
    }

    /**
     * @return list of folders that the specified required entry is contained in and
     * the optional specified user has read access to. If the <code>userId</code> value is not specified
     * only public folders that the entry is contained in is returned and the entry has to be a public entry
     */
    public List<FolderDetails> getFolders() {
        List<FolderDetails> folders = new ArrayList<>();
        for (Folder folder : this.entry.getFolders()) {
            if (!this.folderAuthorization.canRead(this.userId, folder))
                continue;

            folders.add(folder.toDataTransferObject());
        }
        return folders;
    }
}

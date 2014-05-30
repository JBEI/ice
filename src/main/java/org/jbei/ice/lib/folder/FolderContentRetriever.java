package org.jbei.ice.lib.folder;

import java.util.List;

import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.servlet.ModelToInfoFactory;

/**
 * Retriever for entries contained in a folder
 *
 * @author Hector Plahar
 */
public class FolderContentRetriever {

    private final EntryDAO entryDAO;

    public FolderContentRetriever() {
        entryDAO = DAOFactory.getEntryDAO();
    }

    // implicit folder (deleted)
    public FolderDetails getDeletedEntries(String userId, ColumnField sort, boolean asc, int start, int limit) {
        FolderDetails folderDetails = new FolderDetails();
        List<Entry> entries = entryDAO.getByVisibility(userId, Visibility.DELETED, sort, asc, start, limit);
        for (Entry entry : entries) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false);
            folderDetails.getEntries().add(info);
        }
        return folderDetails;
    }

    public FolderDetails getDraftEntries(String userId, ColumnField sort, boolean asc, int start, int limit) {
        FolderDetails folderDetails = new FolderDetails();
        List<Entry> entries = entryDAO.getByVisibility(userId, Visibility.DRAFT, sort, asc, start, limit);
        for (Entry entry : entries) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false);
            folderDetails.getEntries().add(info);
        }
        folderDetails.setCount(entryDAO.getByVisibilityCount(userId, Visibility.DRAFT));
        return folderDetails;
    }
}

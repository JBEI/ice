package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * Represents entry selection in a specific context and by type
 * (e.g. select all plasmids from a search result)
 *
 * @author Hector Plahar
 */
public class EntrySelection implements IDataTransferModel {

    private boolean all;                            // all entries in context selected
    private EntryType entryType;                    // type of entry selected. It is superseded by the all parameter.
    private EntrySelectionType selectionType;       // context selection type
    private SearchQuery searchQuery;                // search query if selection type is "SEARCH"
    private ArrayList<FolderDetails> destination;   // destination for entry selection
    private String folderId;                        // personal, available, shared, drafts, pending, actual folderId
    private ArrayList<Long> entries;                // if no context, then ad hoc selection

    public EntrySelection() {
        entries = new ArrayList<>();
        destination = new ArrayList<>();
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public EntrySelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(EntrySelectionType selectionType) {
        this.selectionType = selectionType;
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    public ArrayList<FolderDetails> getDestination() {
        return destination;
    }

    public void setDestination(ArrayList<FolderDetails> destination) {
        this.destination = destination;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public ArrayList<Long> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<Long> entries) {
        this.entries = entries;
    }
}

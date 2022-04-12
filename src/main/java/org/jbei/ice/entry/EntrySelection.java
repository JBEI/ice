package org.jbei.ice.entry;

import org.jbei.ice.dto.entry.EntryFieldLabel;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.folder.FolderDetails;
import org.jbei.ice.dto.search.SearchQuery;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents entry selection in a specific context and by type
 * (e.g. select all plasmids from a search result)
 * Can include an adhoc selection of local or remote entries
 *
 * @author Hector Plahar
 */
public class EntrySelection implements IDataTransferModel {

    private boolean all;                            // all entries in context selected
    private EntryType entryType;                    // type of entry selected. It is superseded by the all parameter.
    private EntrySelectionType selectionType;       // context selection type
    private SearchQuery searchQuery;                // search query if selection type is "SEARCH"
    private List<FolderDetails> destination;        // destination for entry selection
    private String folderId;                        // personal, available, shared, drafts, pending, actual folderId
    private List<Long> entries;                     // if no context, then ad hoc selection
    private List<PartData> remoteEntries;           // record Ids of adhoc remote entry selection
    private final List<EntryFieldLabel> fields;                // for functionality that allow customization .e.g. csv export

    public EntrySelection() {
        entries = new ArrayList<>();
        destination = new ArrayList<>();
        fields = new ArrayList<>();
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

    public List<FolderDetails> getDestination() {
        return destination;
    }

    public void setDestination(List<FolderDetails> destination) {
        this.destination = destination;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public List<Long> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<Long> entries) {
        this.entries = entries;
    }

    public List<PartData> getRemoteEntries() {
        return this.remoteEntries;
    }

    public List<EntryFieldLabel> getFields() {
        return fields;
    }
}

package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.collection.ICollectionView;
import org.jbei.ice.client.collection.event.FolderRetrieveEvent;
import org.jbei.ice.client.collection.event.FolderRetrieveEventHandler;
import org.jbei.ice.client.collection.event.SubmitEvent;
import org.jbei.ice.client.collection.event.SubmitHandler;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.model.CollectionsModel;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.entry.IHasEntryId;
import org.jbei.ice.shared.FolderDetails;

/**
 * Handler for adding to a collection
 * 
 * @author Hector Plahar
 */
public class AddToHandler implements SubmitHandler {

    private final ICollectionView view;
    private final IHasEntryId hasEntry;
    private final CollectionsModel model;
    private final CollectionDataTable table;

    public AddToHandler(ICollectionView view, IHasEntryId hasEntry, CollectionsModel model,
            CollectionDataTable collectionsDataTable) {
        this.view = view;
        this.hasEntry = hasEntry;
        this.model = model;
        this.table = collectionsDataTable;
    }

    @Override
    public void onSubmit(SubmitEvent event) {

        List<OptionSelect> selected = view.getSelectedOptions(true);
        Set<Long> destinationFolders = new HashSet<Long>();

        for (OptionSelect option : selected) {
            destinationFolders.add(option.getId());
        }

        final ArrayList<Long> entryIds = new ArrayList<Long>(hasEntry.getSelectedEntrySet());

        // validate
        if (destinationFolders.isEmpty() || entryIds.isEmpty())
            return;

        // TODO : should be able to pass List<OptionSelect> without
        view.setBusyIndicator(destinationFolders);

        // service call to actually add
        addEntriesToFolder(destinationFolders, entryIds);
    }

    public void addEntriesToFolder(final Set<Long> destinationFolders,
            final ArrayList<Long> entryIds) {

        model.addEntriesToFolder(new ArrayList<Long>(destinationFolders), entryIds,
            new FolderRetrieveEventHandler() {

                @Override
                public void onFolderRetrieve(FolderRetrieveEvent event) {
                    if (event == null || event.getItems() == null) {
                        view.showFeedbackMessage(
                            "An error occured while adding entries. Please try again.", true);
                        return;
                    }

                    ArrayList<FolderDetails> results = event.getItems();
                    ArrayList<MenuItem> items = new ArrayList<MenuItem>();
                    for (FolderDetails result : results) {
                        items.add(new MenuItem(result.getId(), result.getName(), result.getCount()
                                .longValue(), result.isSystemFolder()));
                    }
                    view.updateMenuItemCounts(items);
                    String entryDisp = (entryIds.size() == 1) ? "entry" : "entries";
                    String msg = "<b>" + entryIds.size() + "</b> " + entryDisp
                            + " successfully added to ";

                    int size = results.size();
                    if (size == 1) {
                        String name = results.get(0).getName();
                        if (name.length() > 24) {
                            name = "<abbr title=\"" + results.get(0).getName() + "\">"
                                    + name.substring(0, 21) + "...</abbr>";
                        }
                        msg += ("\"<b>" + name + "</b>\" collection.");
                    } else {
                        msg += ("\"<b>" + size + "</b> collections.");
                    }
                    view.showFeedbackMessage(msg, false);
                    table.clearSelection();
                }
            });
    }
}

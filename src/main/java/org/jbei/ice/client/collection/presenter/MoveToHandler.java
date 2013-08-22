package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.collection.ICollectionView;
import org.jbei.ice.client.collection.event.SubmitEvent;
import org.jbei.ice.client.collection.event.SubmitHandler;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.model.CollectionsModel;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.entry.IHasEntryId;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;

public abstract class MoveToHandler implements SubmitHandler {

    private final ICollectionView view;
    private final IHasEntryId hasEntry;
    private final CollectionsModel model;

    public MoveToHandler(CollectionsModel model, ICollectionView view, IHasEntryId hasEntry) {
        this.view = view;
        this.hasEntry = hasEntry;
        this.model = model;
    }

    @Override
    public void onSubmit(SubmitEvent event) {
        List<OptionSelect> selected = view.getSelectedOptions(false);

        Set<Long> destinationFolders = new HashSet<Long>();

        for (OptionSelect option : selected) {
            destinationFolders.add(option.getId());
        }

        final ArrayList<Long> entryIds = new ArrayList<Long>(hasEntry.getSelectedEntrySet());

        // validate
        if (destinationFolders.isEmpty() || entryIds.isEmpty())
            return;

        view.setBusyIndicator(destinationFolders, true);
        // show busy indicator for source folders also
        HashSet<Long> source = new HashSet<Long>();
        source.add(this.getSource());
        view.setBusyIndicator(source, true);

        // service call to actually move
        moveEntriesToFolder(destinationFolders, entryIds);
    }

    private void moveEntriesToFolder(final Set<Long> destinationFolders,
            final ArrayList<Long> entryIds) {
        model.moveEntriesToFolder(
                getSource(), new ArrayList<Long>(destinationFolders), entryIds,
                new Callback<ArrayList<FolderDetails>>() {

                    @Override
                    public void onSuccess(ArrayList<FolderDetails> results) {
                        ArrayList<MenuItem> items = new ArrayList<MenuItem>();
                        int size = 0;
                        String name = "";

                        for (FolderDetails result : results) {
                            items.add(new MenuItem(result.getId(), result.getName(), result.getCount()));
                            if (result.getId() != getSource()) {
                                size += 1;
                                name = result.getName();
                            }
                        }
                        view.updateMenuItemCounts(items);
                        String entryDisp = (entryIds.size() == 1) ? "entry" : "entries";
                        String msg = "<b>" + entryIds.size() + "</b> " + entryDisp + " successfully moved to ";

                        if (size == 1 && !name.isEmpty()) {
                            if (name.length() > 24) {
                                name = "<abbr title=\"" + results.get(0).getName() + "\">"
                                        + name.substring(0, 21) + "...</abbr>";
                            }
                            msg += ("\"<b>" + name + "</b>\" collection.");
                        } else {
                            msg += ("<b>" + size + "</b> collections.");
                        }

                        retrieveFolderEntries(getSource(), msg);
                        clearTableSelection();
                    }

                    @Override
                    public void onFailure() {
                        view.showFeedbackMessage("An error occured while moving entries. Please try again.", true);
                    }
                });
    }

    protected abstract long getSource();

    protected abstract void clearTableSelection();

    protected abstract void retrieveFolderEntries(long folder, String msg);
}

package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.collection.ICollectionEntriesView;
import org.jbei.ice.client.collection.event.SubmitEvent;
import org.jbei.ice.client.collection.event.SubmitHandler;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.entry.IHasEntry;
import org.jbei.ice.shared.dto.EntryInfo;

public abstract class AddToSubmitHandler implements SubmitHandler {

    private final ICollectionEntriesView view;
    private final IHasEntry<EntryInfo> hasEntry;

    public AddToSubmitHandler(ICollectionEntriesView view, IHasEntry<EntryInfo> hasEntry) {
        this.view = view;
        this.hasEntry = hasEntry;
    }

    @Override
    public void onSubmit(SubmitEvent event) {

        List<OptionSelect> selected = view.getSelectedOptions(true);
        Set<Long> destinationFolders = new HashSet<Long>();

        for (OptionSelect option : selected) {
            destinationFolders.add(option.getId());
        }

        final ArrayList<Long> entryIds = new ArrayList<Long>();
        for (EntryInfo info : hasEntry.getEntries()) {
            entryIds.add(info.getId());
        }

        // validate
        if (destinationFolders.isEmpty() || entryIds.isEmpty())
            return;

        // TODO : should be able to pass List<OptionSelect> without
        view.setBusyIndicator(destinationFolders);

        // service call to actually add
        addEntriesToFolder(destinationFolders, entryIds);
    }

    public abstract void addEntriesToFolder(Set<Long> destinationFolders, ArrayList<Long> entryIds);
}

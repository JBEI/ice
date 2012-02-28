package org.jbei.ice.client.collection.presenter;

import org.jbei.ice.client.common.entry.IHasEntry;
import org.jbei.ice.shared.dto.EntryInfo;

public class EntryContext {
    private IHasEntry<EntryInfo> hasEntry;

    public EntryContext(IHasEntry<EntryInfo> hasEntry) {
        this.hasEntry = hasEntry;
    }

    public IHasEntry<EntryInfo> getHasEntry() {
        return this.hasEntry;
    }
}

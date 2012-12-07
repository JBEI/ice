package org.jbei.ice.client.common;

import org.jbei.ice.shared.dto.entry.EntryInfo;

public interface IHasNavigableData {

    EntryInfo getCachedData(long id, String recordId);

    int indexOfCached(EntryInfo info);

    int getSize();

    EntryInfo getNext(EntryInfo info);

    EntryInfo getPrev(EntryInfo info);
}

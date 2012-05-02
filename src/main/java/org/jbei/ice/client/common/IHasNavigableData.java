package org.jbei.ice.client.common;

import org.jbei.ice.shared.dto.EntryInfo;

public interface IHasNavigableData {

    EntryInfo getCachedData(long id);

    int indexOfCached(EntryInfo info);

    int getSize();

    EntryInfo getNext(EntryInfo info);

    EntryInfo getPrev(EntryInfo info);
}

package org.jbei.ice.client.common;

import org.jbei.ice.lib.shared.dto.entry.PartData;

public interface IHasNavigableData {

    PartData getCachedData(long id, String recordId);

    int indexOfCached(PartData info);

    int getSize();

    PartData getNext(PartData info);

    PartData getPrev(PartData info);
}

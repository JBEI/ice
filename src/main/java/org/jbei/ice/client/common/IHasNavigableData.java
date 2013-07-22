package org.jbei.ice.client.common;

import org.jbei.ice.lib.shared.dto.entry.PartData;

/**
 * Interface for part data providers whose data can be navigated one at a time with next/prev
 *
 * @author Hector Plahar
 */
public interface IHasNavigableData {

    PartData getCachedData(long id, String recordId);

    int indexOfCached(PartData info);

    int getSize();

    PartData getNext(PartData info);

    PartData getPrev(PartData info);
}

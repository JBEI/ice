package org.jbei.ice.client.common.entry;

import org.jbei.ice.lib.shared.dto.entry.PartData;

/**
 * An Object that implements this interface contains a part data object
 * that can be retrieved and returned
 *
 * @author Hector Plahar
 */
public interface IHasPartData<T extends PartData> {

    T getPart();
}

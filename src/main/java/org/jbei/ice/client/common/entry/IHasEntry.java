package org.jbei.ice.client.common.entry;

import java.util.Set;

import org.jbei.ice.lib.shared.dto.entry.PartData;

/**
 * An Object that implements this interface contains unique PartData objects
 * that can be retrieved and returned
 *
 * @author Hector Plahar
 */
public interface IHasEntry<T extends PartData> {
    Set<T> getEntries();
}

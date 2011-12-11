package org.jbei.ice.client.common.entry;

import java.util.Set;

import org.jbei.ice.shared.dto.EntryInfo;

/**
 * An Object that implements this interface contains unique EntryInfo objects
 * that can be retrieved and returned
 * 
 * @author Hector Plahar
 */
public interface IHasEntry<T extends EntryInfo> {
    Set<T> getEntries();
}

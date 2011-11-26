package org.jbei.ice.client.common.entry;

import java.util.Set;

import org.jbei.ice.shared.EntryData;

/**
 * An Object that implements this interface contains unique EntryData objects
 * that can be selected by a user. (Typically using a selection model). The
 * selected entries can be retrieved.
 * 
 * @author Hector Plahar
 */
public interface IHasEntrySelection<T extends EntryData> {
    Set<T> getSelectedEntries();
}

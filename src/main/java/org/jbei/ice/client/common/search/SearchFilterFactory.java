package org.jbei.ice.client.common.search;

import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.search.EntrySearchFilter;

/**
 * @author Hector Plahar
 */
public class SearchFilterFactory {

    public static EntrySearchFilter getEntryFilterForType(EntryType type) {

        if (type == null)
            return new EntrySearchFilter();

        switch (type) {
            default:
                return new EntrySearchFilter();
        }
    }
}
